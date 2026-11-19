package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regra de negócio que decide se uma alteração é possível. A decisão combina tempo e produção:
 * não depende só de horas nem só de produtividade, mas de quanto já foi produzido.
 */
@Entity
@Getter
@Table(name = "politicas_alteracao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PoliticaAlteracaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    /** Horas após a aprovação em que, sem produção iniciada, a alteração não tem custo. */
    @Column(name = "horas_sem_custo", nullable = false)
    private int horasSemCusto;

    /** Percentual produzido acima do qual o pedido não aceita mais alteração. */
    @Column(name = "percentual_limite_com_custo", nullable = false)
    private int percentualLimiteComCusto;

    /** Horas antes da entrega em que o pedido fica fechado para alterações. */
    @Column(name = "horas_fechamento_antes_entrega", nullable = false)
    private int horasFechamentoAntesDaEntrega;

    @Column(nullable = false)
    private boolean ativa = true;

    public PoliticaAlteracaoEntity(String nome, int horasSemCusto, int percentualLimiteComCusto,
                                   int horasFechamentoAntesDaEntrega) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da política é obrigatório.");
        }
        validarJanelas(horasSemCusto, percentualLimiteComCusto, horasFechamentoAntesDaEntrega);
        this.nome = nome.trim();
        this.horasSemCusto = horasSemCusto;
        this.percentualLimiteComCusto = percentualLimiteComCusto;
        this.horasFechamentoAntesDaEntrega = horasFechamentoAntesDaEntrega;
        this.ativa = true;
    }

    public void ajustarJanelas(int horasSemCusto, int percentualLimiteComCusto, int horasFechamentoAntesDaEntrega) {
        validarJanelas(horasSemCusto, percentualLimiteComCusto, horasFechamentoAntesDaEntrega);
        this.horasSemCusto = horasSemCusto;
        this.percentualLimiteComCusto = percentualLimiteComCusto;
        this.horasFechamentoAntesDaEntrega = horasFechamentoAntesDaEntrega;
    }

    public void desativar() {
        this.ativa = false;
    }

    public void ativar() {
        this.ativa = true;
    }

    /**
     * VERMELHA: etapa irreversível concluída, pedido fechado perto da entrega ou produção além do limite.<br>
     * VERDE: nada produzido e ainda dentro da janela sem custo.<br>
     * AMARELA: os demais casos (possível, com custo e novo prazo).
     */
    public ZonaAlteracao classificar(LocalDateTime agora, LocalDateTime aprovadoEm, LocalDateTime entregaPrevista,
                                     int percentualProduzido, boolean etapaIrreversivelConcluida) {
        boolean pedidoFechado = !agora.isBefore(entregaPrevista.minusHours(horasFechamentoAntesDaEntrega));
        if (etapaIrreversivelConcluida || pedidoFechado || percentualProduzido > percentualLimiteComCusto) {
            return ZonaAlteracao.VERMELHA;
        }
        long horasDesdeAprovacao = Duration.between(aprovadoEm, agora).toHours();
        if (percentualProduzido == 0 && horasDesdeAprovacao <= horasSemCusto) {
            return ZonaAlteracao.VERDE;
        }
        return ZonaAlteracao.AMARELA;
    }

    private static void validarJanelas(int horasSemCusto, int percentualLimite, int horasFechamento) {
        if (horasSemCusto < 0 || horasFechamento < 0) {
            throw new RegraNegocioException("As janelas de tempo não podem ser negativas.");
        }
        if (percentualLimite < 0 || percentualLimite > 100) {
            throw new RegraNegocioException("O percentual limite deve estar entre 0 e 100.");
        }
    }
}

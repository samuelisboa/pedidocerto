package br.edu.uniesp.pedidocerto.internal.producao.entity;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Andamento de uma etapa de produção dentro de um pedido específico. */
@Entity
@Getter
@Table(name = "execucoes_etapa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExecucaoEtapaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etapa_id", nullable = false)
    private EtapaProducaoEntity etapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private FuncionarioEntity responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEtapa status = StatusEtapa.PENDENTE;

    @Column(name = "percentual_concluido", nullable = false)
    private int percentualConcluido;

    @Column(name = "iniciada_em")
    private LocalDateTime iniciadaEm;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    public ExecucaoEtapaEntity(PedidoEntity pedido, EtapaProducaoEntity etapa) {
        if (pedido == null || etapa == null) {
            throw new RegraNegocioException("Pedido e etapa são obrigatórios.");
        }
        this.pedido = pedido;
        this.etapa = etapa;
        this.status = StatusEtapa.PENDENTE;
        this.percentualConcluido = 0;
    }

    public void iniciar(FuncionarioEntity responsavel, LocalDateTime quando) {
        if (status != StatusEtapa.PENDENTE) {
            throw new RegraNegocioException("Somente etapas pendentes podem ser iniciadas.");
        }
        if (responsavel == null || !responsavel.isAtivo() || !responsavel.pertenceAo(SetorFuncionario.PRODUCAO)) {
            throw new RegraNegocioException("A etapa deve ser iniciada por um funcionário ativo da produção.");
        }
        this.responsavel = responsavel;
        this.iniciadaEm = quando;
        this.status = StatusEtapa.EM_ANDAMENTO;
    }

    public void atualizarProgresso(int novoPercentual) {
        if (status != StatusEtapa.EM_ANDAMENTO) {
            throw new RegraNegocioException("O progresso só pode ser atualizado em etapas em andamento.");
        }
        if (novoPercentual < percentualConcluido || novoPercentual > 100) {
            throw new RegraNegocioException("O progresso deve estar entre o valor atual e 100%.");
        }
        this.percentualConcluido = novoPercentual;
    }

    public void concluir(LocalDateTime quando) {
        if (status != StatusEtapa.EM_ANDAMENTO) {
            throw new RegraNegocioException("Somente etapas em andamento podem ser concluídas.");
        }
        this.percentualConcluido = 100;
        this.concluidaEm = quando;
        this.status = StatusEtapa.CONCLUIDA;
    }

    public boolean estaConcluida() {
        return status == StatusEtapa.CONCLUIDA;
    }
}

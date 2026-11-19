package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido de mudança feito pelo cliente após a aprovação. Registra quem pediu, quando, a zona
 * (verde/amarela/vermelha) e a confirmação de leitura pela produção.
 */
@Entity
@Getter
@Table(name = "alteracoes_pedido")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlteracaoPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitada_por_id", nullable = false)
    private FuncionarioEntity solicitadaPor;

    @Column(name = "solicitada_em", nullable = false)
    private LocalDateTime solicitadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ZonaAlteracao zona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAlteracao status;

    @Column(name = "custo_adicional", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoAdicional = BigDecimal.ZERO;

    @Column(name = "dias_adicionais", nullable = false)
    private int diasAdicionais;

    @Column(name = "aprovada_em")
    private LocalDateTime aprovadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmada_por_id")
    private FuncionarioEntity confirmadaPor;

    @Column(name = "confirmada_em")
    private LocalDateTime confirmadaEm;

    @Column(name = "motivo_recusa", length = 255)
    private String motivoRecusa;

    AlteracaoPedidoEntity(PedidoEntity pedido, String descricao, FuncionarioEntity solicitadaPor,
                          LocalDateTime solicitadaEm, ZonaAlteracao zona) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descreva a alteração solicitada pelo cliente.");
        }
        this.pedido = pedido;
        this.descricao = descricao;
        this.solicitadaPor = solicitadaPor;
        this.solicitadaEm = solicitadaEm;
        this.zona = zona;
        switch (zona) {
            case VERDE -> {
                this.status = StatusAlteracao.APROVADA_PELO_CLIENTE;
                this.aprovadaEm = solicitadaEm;
            }
            case AMARELA -> this.status = StatusAlteracao.SOLICITADA;
            case VERMELHA -> {
                this.status = StatusAlteracao.RECUSADA;
                this.motivoRecusa = "Ponto sem volta: só é possível como novo pedido.";
            }
        }
    }

    /** Informa o impacto da alteração em zona amarela para o cliente decidir. */
    public void proporCusto(BigDecimal custoAdicional, int diasAdicionais) {
        if (zona != ZonaAlteracao.AMARELA || status != StatusAlteracao.SOLICITADA) {
            throw new RegraNegocioException("Só alterações amarelas recém-solicitadas recebem proposta de custo.");
        }
        if (custoAdicional == null || custoAdicional.signum() < 0 || diasAdicionais < 0) {
            throw new RegraNegocioException("Custo e prazo adicionais não podem ser negativos.");
        }
        this.custoAdicional = custoAdicional;
        this.diasAdicionais = diasAdicionais;
        this.status = StatusAlteracao.AGUARDANDO_CLIENTE;
    }

    void aprovarPeloCliente(LocalDateTime quando) {
        if (status != StatusAlteracao.AGUARDANDO_CLIENTE) {
            throw new RegraNegocioException("A alteração não está aguardando a aprovação do cliente.");
        }
        this.status = StatusAlteracao.APROVADA_PELO_CLIENTE;
        this.aprovadaEm = quando;
    }

    public void recusar(String motivo) {
        if (status != StatusAlteracao.SOLICITADA && status != StatusAlteracao.AGUARDANDO_CLIENTE) {
            throw new RegraNegocioException("Esta alteração não pode mais ser recusada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo da recusa.");
        }
        this.status = StatusAlteracao.RECUSADA;
        this.motivoRecusa = motivo;
    }

    /** A produção confirma "li e entendi"; só então o pedido pode avançar de etapa. */
    void confirmarLeituraDaProducao(FuncionarioEntity funcionario, LocalDateTime quando) {
        if (status != StatusAlteracao.APROVADA_PELO_CLIENTE) {
            throw new RegraNegocioException("Só alterações aprovadas pelo cliente precisam de confirmação da produção.");
        }
        if (funcionario == null || !funcionario.isAtivo() || !funcionario.pertenceAo(SetorFuncionario.PRODUCAO)) {
            throw new RegraNegocioException("A leitura deve ser confirmada por um funcionário ativo da produção.");
        }
        this.confirmadaPor = funcionario;
        this.confirmadaEm = quando;
        this.status = StatusAlteracao.CONFIRMADA_PELA_PRODUCAO;
    }

    public boolean aguardaLeituraDaProducao() {
        return status == StatusAlteracao.APROVADA_PELO_CLIENTE;
    }
}

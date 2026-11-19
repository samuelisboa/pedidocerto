package br.edu.uniesp.pedidocerto.internal.pagamento.entity;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "pagamentos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPagamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "pago_em", nullable = false)
    private LocalDateTime pagoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private FuncionarioEntity registradoPor;

    @Column(nullable = false)
    private boolean conferido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conferido_por_id")
    private FuncionarioEntity conferidoPor;

    @Column(name = "conferido_em")
    private LocalDateTime conferidoEm;

    public PagamentoEntity(PedidoEntity pedido, TipoPagamento tipo, FormaPagamento forma, BigDecimal valor,
                           LocalDateTime pagoEm, FuncionarioEntity registradoPor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("O valor do pagamento deve ser maior que zero.");
        }
        if (registradoPor == null || !registradoPor.isAtivo()) {
            throw new RegraNegocioException("O pagamento deve ser registrado por um funcionário ativo.");
        }
        this.pedido = pedido;
        this.tipo = tipo;
        this.forma = forma;
        this.valor = valor;
        this.pagoEm = pagoEm;
        this.registradoPor = registradoPor;
        this.conferido = false;
    }

    public void conferir(FuncionarioEntity responsavel, LocalDateTime quando) {
        if (conferido) {
            throw new RegraNegocioException("Este pagamento já foi conferido.");
        }
        if (responsavel == null || !responsavel.isAtivo()) {
            throw new RegraNegocioException("A conferência deve ser feita por um funcionário ativo.");
        }
        this.conferido = true;
        this.conferidoPor = responsavel;
        this.conferidoEm = quando;
    }
}

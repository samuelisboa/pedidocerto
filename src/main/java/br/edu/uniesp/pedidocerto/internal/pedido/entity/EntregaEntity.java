package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entrega do pedido. Só é registrada com o saldo conferido (trava de entrega). */
@Entity
@Getter
@Table(name = "entregas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntregaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModalidadeEntrega modalidade = ModalidadeEntrega.RETIRADA;

    @Column(name = "endereco_entrega", length = 255)
    private String enderecoEntrega;

    @Column(name = "saldo_conferido", nullable = false)
    private boolean saldoConferido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private FuncionarioEntity responsavel;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    EntregaEntity(PedidoEntity pedido) {
        this.pedido = pedido;
    }

    public void definirModalidade(ModalidadeEntrega modalidade, String endereco) {
        if (entregueEm != null) {
            throw new RegraNegocioException("A entrega já foi realizada.");
        }
        if (modalidade == ModalidadeEntrega.DOMICILIO && (endereco == null || endereco.isBlank())) {
            throw new RegraNegocioException("Informe o endereço para entrega em domicílio.");
        }
        this.modalidade = modalidade;
        this.enderecoEntrega = modalidade == ModalidadeEntrega.DOMICILIO ? endereco : null;
    }

    void registrar(FuncionarioEntity responsavel, BigDecimal saldoPendente, LocalDateTime quando) {
        if (saldoPendente.signum() > 0) {
            throw new RegraNegocioException("Entrega bloqueada: há saldo pendente de R$ " + saldoPendente + ".");
        }
        if (responsavel == null || !responsavel.isAtivo()) {
            throw new RegraNegocioException("A entrega deve ser registrada por um funcionário ativo.");
        }
        this.saldoConferido = true;
        this.responsavel = responsavel;
        this.entregueEm = quando;
    }
}

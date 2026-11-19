package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Item do pedido. Só é alterado pelo próprio {@link PedidoEntity}, que mantém o valor total coerente. */
@Entity
@Getter
@Table(name = "itens_pedido")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(columnDefinition = "TEXT")
    private String personalizacao;

    ItemPedidoEntity(PedidoEntity pedido, ProdutoEntity produto, int quantidade, String personalizacao) {
        if (!produto.isAtivo()) {
            throw new RegraNegocioException("O produto " + produto.getNome() + " está inativo.");
        }
        exigirQuantidade(quantidade);
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoBase();
        this.personalizacao = personalizacao;
    }

    void alterarQuantidade(int novaQuantidade) {
        exigirQuantidade(novaQuantidade);
        this.quantidade = novaQuantidade;
    }

    void alterarPersonalizacao(String novaPersonalizacao) {
        this.personalizacao = novaPersonalizacao;
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    private static void exigirQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade do item deve ser maior que zero.");
        }
    }
}

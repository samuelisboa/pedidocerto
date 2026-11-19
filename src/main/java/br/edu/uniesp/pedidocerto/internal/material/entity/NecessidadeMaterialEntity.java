package br.edu.uniesp.pedidocerto.internal.material.entity;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Quanto de um material um pedido precisa. Resolve a relação muitos-para-muitos entre pedido e material. */
@Entity
@Getter
@Table(name = "necessidades_material",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "material_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NecessidadeMaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialEntity material;

    @Column(name = "quantidade_necessaria", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidadeNecessaria;

    @Column(name = "quantidade_consumida", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidadeConsumida = BigDecimal.ZERO;

    public NecessidadeMaterialEntity(PedidoEntity pedido, MaterialEntity material, BigDecimal quantidadeNecessaria) {
        if (material == null) {
            throw new RegraNegocioException("O material é obrigatório.");
        }
        exigirPositivo(quantidadeNecessaria);
        this.pedido = pedido;
        this.material = material;
        this.quantidadeNecessaria = quantidadeNecessaria;
        this.quantidadeConsumida = BigDecimal.ZERO;
    }

    public void ajustarQuantidadeNecessaria(BigDecimal novaQuantidade) {
        exigirPositivo(novaQuantidade);
        if (novaQuantidade.compareTo(quantidadeConsumida) < 0) {
            throw new RegraNegocioException("A nova quantidade é menor que a já consumida.");
        }
        this.quantidadeNecessaria = novaQuantidade;
    }

    /** Baixa do estoque o que foi usado na produção. */
    public void consumir(BigDecimal quantidade) {
        exigirPositivo(quantidade);
        if (quantidade.compareTo(quantidadePendente()) > 0) {
            throw new RegraNegocioException("Consumo acima do necessário para o pedido.");
        }
        material.baixar(quantidade);
        this.quantidadeConsumida = this.quantidadeConsumida.add(quantidade);
    }

    public BigDecimal quantidadePendente() {
        return quantidadeNecessaria.subtract(quantidadeConsumida);
    }

    /** Quanto falta comprar para concluir o pedido, considerando o estoque atual. */
    public BigDecimal quantidadeAComprar() {
        return material.quantidadeFaltante(quantidadePendente());
    }

    private static void exigirPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }
    }
}

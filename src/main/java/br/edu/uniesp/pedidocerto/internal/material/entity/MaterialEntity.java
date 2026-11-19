package br.edu.uniesp.pedidocerto.internal.material.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "materiais")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    @Column(name = "quantidade_estoque", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantidadeEstoque = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 2)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean ativo = true;

    public MaterialEntity(String nome, String unidadeMedida, BigDecimal quantidadeInicial, BigDecimal estoqueMinimo) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do material é obrigatório.");
        }
        if (unidadeMedida == null || unidadeMedida.isBlank()) {
            throw new RegraNegocioException("A unidade de medida do material é obrigatória.");
        }
        exigirNaoNegativo(quantidadeInicial, "A quantidade inicial");
        exigirNaoNegativo(estoqueMinimo, "O estoque mínimo");
        this.nome = nome.trim();
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque = quantidadeInicial;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = true;
    }

    public void repor(BigDecimal quantidade) {
        exigirPositivo(quantidade, "A quantidade de reposição");
        this.quantidadeEstoque = this.quantidadeEstoque.add(quantidade);
    }

    public void baixar(BigDecimal quantidade) {
        exigirPositivo(quantidade, "A quantidade de baixa");
        if (quantidade.compareTo(quantidadeEstoque) > 0) {
            throw new RegraNegocioException("Estoque insuficiente de " + nome + ": disponível "
                    + quantidadeEstoque + " " + unidadeMedida + ", solicitado " + quantidade + ".");
        }
        this.quantidadeEstoque = this.quantidadeEstoque.subtract(quantidade);
    }

    public void ajustarEstoqueMinimo(BigDecimal novoMinimo) {
        exigirNaoNegativo(novoMinimo, "O estoque mínimo");
        this.estoqueMinimo = novoMinimo;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public boolean estaAbaixoDoMinimo() {
        return quantidadeEstoque.compareTo(estoqueMinimo) < 0;
    }

    /** Quanto falta comprar para atender a quantidade necessária (zero se o estoque basta). */
    public BigDecimal quantidadeFaltante(BigDecimal necessaria) {
        BigDecimal falta = necessaria.subtract(quantidadeEstoque);
        return falta.signum() > 0 ? falta : BigDecimal.ZERO;
    }

    private static void exigirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException(campo + " deve ser maior que zero.");
        }
    }

    private static void exigirNaoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException(campo + " não pode ser negativo.");
        }
    }
}

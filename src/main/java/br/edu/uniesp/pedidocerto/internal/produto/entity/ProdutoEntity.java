package br.edu.uniesp.pedidocerto.internal.produto.entity;

import br.edu.uniesp.pedidocerto.internal.material.entity.MaterialEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Table(name = "produtos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoBase;

    @Column(name = "horas_producao_por_unidade", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasProducaoPorUnidade;

    @Column(nullable = false)
    private boolean ativo = true;

    @Getter(AccessLevel.NONE)
    @ManyToMany
    @JoinTable(name = "produto_material",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private List<MaterialEntity> materiaisPadrao = new ArrayList<>();

    public ProdutoEntity(String nome, String descricao, BigDecimal precoBase, BigDecimal horasProducaoPorUnidade) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do produto é obrigatório.");
        }
        exigirPositivo(precoBase, "O preço base");
        exigirPositivo(horasProducaoPorUnidade, "O tempo de produção por unidade");
        this.nome = nome.trim();
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.horasProducaoPorUnidade = horasProducaoPorUnidade;
        this.ativo = true;
    }

    public List<MaterialEntity> getMateriaisPadrao() {
        return Collections.unmodifiableList(materiaisPadrao);
    }

    public void reajustarPreco(BigDecimal novoPreco) {
        exigirPositivo(novoPreco, "O preço base");
        this.precoBase = novoPreco;
    }

    public void ajustarTempoDeProducao(BigDecimal horasPorUnidade) {
        exigirPositivo(horasPorUnidade, "O tempo de produção por unidade");
        this.horasProducaoPorUnidade = horasPorUnidade;
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = novaDescricao;
    }

    public void vincularMaterial(MaterialEntity material) {
        if (material == null) {
            throw new RegraNegocioException("O material é obrigatório.");
        }
        if (!materiaisPadrao.contains(material)) {
            materiaisPadrao.add(material);
        }
    }

    public void desvincularMaterial(MaterialEntity material) {
        materiaisPadrao.remove(material);
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public BigDecimal calcularHorasDeProducao(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }
        return horasProducaoPorUnidade.multiply(BigDecimal.valueOf(quantidade));
    }

    private static void exigirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException(campo + " deve ser maior que zero.");
        }
    }
}

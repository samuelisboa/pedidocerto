package br.edu.uniesp.pedidocerto.internal.producao.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Etapa do processo produtivo (ex.: corte, impressão, acabamento). Define o "ponto sem volta". */
@Entity
@Getter
@Table(name = "etapas_producao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EtapaProducaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private boolean irreversivel;

    public EtapaProducaoEntity(String nome, int ordem, boolean irreversivel) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da etapa é obrigatório.");
        }
        if (ordem <= 0) {
            throw new RegraNegocioException("A ordem da etapa deve ser maior que zero.");
        }
        this.nome = nome.trim();
        this.ordem = ordem;
        this.irreversivel = irreversivel;
    }

    public void reordenar(int novaOrdem) {
        if (novaOrdem <= 0) {
            throw new RegraNegocioException("A ordem da etapa deve ser maior que zero.");
        }
        this.ordem = novaOrdem;
    }

    public void marcarComoIrreversivel() {
        this.irreversivel = true;
    }

    public void marcarComoReversivel() {
        this.irreversivel = false;
    }
}

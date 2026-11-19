package br.edu.uniesp.pedidocerto.internal.funcionario.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "funcionarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FuncionarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SetorFuncionario setor;

    @Column(nullable = false)
    private boolean ativo = true;

    public FuncionarioEntity(String nome, SetorFuncionario setor) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do funcionário é obrigatório.");
        }
        if (setor == null) {
            throw new RegraNegocioException("O setor do funcionário é obrigatório.");
        }
        this.nome = nome.trim();
        this.setor = setor;
        this.ativo = true;
    }

    public boolean pertenceAo(SetorFuncionario setor) {
        return this.setor == setor;
    }

    public void transferirParaSetor(SetorFuncionario novoSetor) {
        if (novoSetor == null) {
            throw new RegraNegocioException("O novo setor é obrigatório.");
        }
        this.setor = novoSetor;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }
}

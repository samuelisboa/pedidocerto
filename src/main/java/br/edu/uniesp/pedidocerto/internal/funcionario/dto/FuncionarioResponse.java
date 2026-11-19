package br.edu.uniesp.pedidocerto.internal.funcionario.dto;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;

public record FuncionarioResponse(Long id, String nome, SetorFuncionario setor, boolean ativo) {

    public static FuncionarioResponse de(FuncionarioEntity f) {
        return new FuncionarioResponse(f.getId(), f.getNome(), f.getSetor(), f.isAtivo());
    }
}

package br.edu.uniesp.pedidocerto.internal.funcionario.dto;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FuncionarioRequest(@NotBlank String nome, @NotNull SetorFuncionario setor) {
}

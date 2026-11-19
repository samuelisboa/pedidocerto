package br.edu.uniesp.pedidocerto.internal.cliente.dto;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClienteRequest(
        @NotBlank String nome,
        String telefone,
        String email,
        @NotNull CanalContato canalPreferencial) {
}

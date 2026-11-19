package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitarAlteracaoRequest(@NotBlank String descricao, @NotNull Long solicitadaPorId) {
}

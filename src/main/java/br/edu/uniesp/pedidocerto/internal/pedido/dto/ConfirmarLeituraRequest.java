package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarLeituraRequest(@NotNull Long funcionarioId) {
}

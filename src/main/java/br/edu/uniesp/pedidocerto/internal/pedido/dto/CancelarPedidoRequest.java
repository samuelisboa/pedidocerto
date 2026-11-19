package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelarPedidoRequest(@NotBlank String motivo) {
}

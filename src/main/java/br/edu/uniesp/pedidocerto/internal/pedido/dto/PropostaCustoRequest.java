package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PropostaCustoRequest(@NotNull @PositiveOrZero BigDecimal custoAdicional,
                                   @PositiveOrZero int diasAdicionais) {
}

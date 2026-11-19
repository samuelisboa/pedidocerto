package br.edu.uniesp.pedidocerto.internal.produto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReajustePrecoRequest(@NotNull @Positive BigDecimal preco) {
}

package br.edu.uniesp.pedidocerto.internal.pagamento.dto;

import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarPagamentoRequest(
        @NotNull TipoPagamento tipo,
        @NotNull FormaPagamento forma,
        @NotNull @Positive BigDecimal valor,
        @NotNull Long registradoPorId) {
}

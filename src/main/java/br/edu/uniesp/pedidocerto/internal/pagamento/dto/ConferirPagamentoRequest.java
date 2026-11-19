package br.edu.uniesp.pedidocerto.internal.pagamento.dto;

import jakarta.validation.constraints.NotNull;

public record ConferirPagamentoRequest(@NotNull Long funcionarioId) {
}

package br.edu.uniesp.pedidocerto.internal.producao.dto;

import jakarta.validation.constraints.NotNull;

public record PlanejarEtapaRequest(@NotNull Long etapaId) {
}

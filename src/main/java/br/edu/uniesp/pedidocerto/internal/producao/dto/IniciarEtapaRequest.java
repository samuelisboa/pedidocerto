package br.edu.uniesp.pedidocerto.internal.producao.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarEtapaRequest(@NotNull Long responsavelId) {
}

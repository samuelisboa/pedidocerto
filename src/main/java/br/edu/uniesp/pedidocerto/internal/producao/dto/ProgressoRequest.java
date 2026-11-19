package br.edu.uniesp.pedidocerto.internal.producao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProgressoRequest(@Min(0) @Max(100) int percentual) {
}

package br.edu.uniesp.pedidocerto.internal.pagamento.dto;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.ModalidadeEntrega;
import jakarta.validation.constraints.NotNull;

public record ModalidadeEntregaRequest(@NotNull ModalidadeEntrega modalidade, String endereco) {
}

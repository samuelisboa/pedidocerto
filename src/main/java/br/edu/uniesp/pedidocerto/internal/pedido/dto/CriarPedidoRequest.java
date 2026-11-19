package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CriarPedidoRequest(
        @NotNull Long clienteId,
        @NotNull Long atendenteId,
        @NotNull CanalContato canalOrigem,
        @NotNull @Future LocalDateTime dataEntregaPrevista,
        @NotEmpty @Valid List<ItemPedidoRequest> itens) {
}

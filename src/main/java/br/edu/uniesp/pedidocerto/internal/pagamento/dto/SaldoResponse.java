package br.edu.uniesp.pedidocerto.internal.pagamento.dto;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;

import java.math.BigDecimal;

public record SaldoResponse(Long pedidoId, BigDecimal valorTotal, BigDecimal valorPago, BigDecimal saldoPendente,
                            boolean quitado) {

    public static SaldoResponse de(PedidoEntity pedido) {
        return new SaldoResponse(pedido.getId(), pedido.getValorTotal(), pedido.valorPago(),
                pedido.saldoPendente(), pedido.estaQuitado());
    }
}

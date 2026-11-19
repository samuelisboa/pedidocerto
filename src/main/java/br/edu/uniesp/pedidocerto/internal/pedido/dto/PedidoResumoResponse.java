package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da listagem de pedidos: o suficiente para o atendente responder "fica pronto no dia?". */
public record PedidoResumoResponse(
        Long id,
        String numero,
        StatusPedido status,
        String clienteNome,
        LocalDateTime dataEntregaPrevista,
        BigDecimal valorTotal,
        BigDecimal saldoPendente,
        boolean atrasado,
        boolean alteracaoAguardandoLeitura) {

    public static PedidoResumoResponse de(PedidoEntity pedido, LocalDateTime agora) {
        return new PedidoResumoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getStatus(),
                pedido.getCliente().getNome(),
                pedido.getDataEntregaPrevista(),
                pedido.getValorTotal(),
                pedido.saldoPendente(),
                pedido.estaAtrasado(agora),
                pedido.getAlteracoes().stream().anyMatch(a -> a.aguardaLeituraDaProducao()));
    }
}

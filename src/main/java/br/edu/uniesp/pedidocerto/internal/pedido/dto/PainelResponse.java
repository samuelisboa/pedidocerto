package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;

import java.math.BigDecimal;
import java.util.Map;

/** Painel do proprietário: o que exige atenção, sem precisar perguntar à equipe. */
public record PainelResponse(
        long emProducao,
        long atrasados,
        BigDecimal aReceber,
        long alteracoesAguardandoLeitura,
        Map<StatusPedido, Long> pedidosPorStatus) {
}

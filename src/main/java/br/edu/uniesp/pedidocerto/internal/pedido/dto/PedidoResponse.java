package br.edu.uniesp.pedidocerto.internal.pedido.dto;

import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.PagamentoEntity;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Visão completa de um pedido, compartilhada por todos os endpoints que o devolvem. */
public record PedidoResponse(
        Long id,
        String numero,
        StatusPedido status,
        Long clienteId,
        String clienteNome,
        LocalDateTime dataEntregaPrevista,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal saldoPendente,
        int versao,
        List<ItemResponse> itens,
        List<PagamentoResumo> pagamentos,
        List<AlteracaoResumo> alteracoes) {

    public record ItemResponse(Long id, Long produtoId, String produto, int quantidade,
                               BigDecimal precoUnitario, BigDecimal subtotal, String personalizacao) {
    }

    public record PagamentoResumo(Long id, TipoPagamento tipo, FormaPagamento forma, BigDecimal valor,
                                  boolean conferido) {
    }

    public record AlteracaoResumo(Long id, String descricao, ZonaAlteracao zona, StatusAlteracao status,
                                  BigDecimal custoAdicional, int diasAdicionais) {
    }

    /** Deve ser chamado dentro de uma transação, pois lê coleções carregadas sob demanda. */
    public static PedidoResponse de(PedidoEntity pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getStatus(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getDataEntregaPrevista(),
                pedido.getValorTotal(),
                pedido.valorPago(),
                pedido.saldoPendente(),
                pedido.getVersao(),
                pedido.getItens().stream().map(PedidoResponse::item).toList(),
                pedido.getPagamentos().stream().map(PedidoResponse::pagamento).toList(),
                pedido.getAlteracoes().stream().map(PedidoResponse::alteracao).toList());
    }

    private static ItemResponse item(ItemPedidoEntity i) {
        return new ItemResponse(i.getId(), i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(),
                i.getPrecoUnitario(), i.subtotal(), i.getPersonalizacao());
    }

    private static PagamentoResumo pagamento(PagamentoEntity p) {
        return new PagamentoResumo(p.getId(), p.getTipo(), p.getForma(), p.getValor(), p.isConferido());
    }

    private static AlteracaoResumo alteracao(AlteracaoPedidoEntity a) {
        return new AlteracaoResumo(a.getId(), a.getDescricao(), a.getZona(), a.getStatus(),
                a.getCustoAdicional(), a.getDiasAdicionais());
    }
}

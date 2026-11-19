package br.edu.uniesp.pedidocerto.internal.producao.dto;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.pedidocerto.internal.producao.entity.ExecucaoEtapaEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.StatusEtapa;

import java.util.List;

/** Andamento da produção de um pedido: base da decisão de aceitar ou não uma alteração. */
public record ProducaoResponse(
        Long pedidoId,
        StatusPedido statusPedido,
        int percentualProduzido,
        boolean etapaIrreversivelConcluida,
        List<EtapaResponse> etapas) {

    public record EtapaResponse(Long id, Long etapaId, String nome, boolean irreversivel, StatusEtapa status,
                                int percentualConcluido, String responsavel) {
    }

    public static ProducaoResponse de(PedidoEntity pedido) {
        return new ProducaoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.percentualProduzido(),
                pedido.existeEtapaIrreversivelConcluida(),
                pedido.getExecucoesEtapa().stream().map(ProducaoResponse::etapa).toList());
    }

    private static EtapaResponse etapa(ExecucaoEtapaEntity e) {
        return new EtapaResponse(e.getId(), e.getEtapa().getId(), e.getEtapa().getNome(),
                e.getEtapa().isIrreversivel(), e.getStatus(), e.getPercentualConcluido(),
                e.getResponsavel() == null ? null : e.getResponsavel().getNome());
    }
}

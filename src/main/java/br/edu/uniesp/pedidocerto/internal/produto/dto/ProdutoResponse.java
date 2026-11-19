package br.edu.uniesp.pedidocerto.internal.produto.dto;

import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, String descricao, BigDecimal precoBase,
                              BigDecimal horasProducaoPorUnidade, boolean ativo) {

    public static ProdutoResponse de(ProdutoEntity p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPrecoBase(),
                p.getHorasProducaoPorUnidade(), p.isAtivo());
    }
}

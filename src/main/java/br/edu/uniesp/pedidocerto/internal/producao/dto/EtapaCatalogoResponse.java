package br.edu.uniesp.pedidocerto.internal.producao.dto;

import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;

public record EtapaCatalogoResponse(Long id, String nome, int ordem, boolean irreversivel) {

    public static EtapaCatalogoResponse de(EtapaProducaoEntity e) {
        return new EtapaCatalogoResponse(e.getId(), e.getNome(), e.getOrdem(), e.isIrreversivel());
    }
}

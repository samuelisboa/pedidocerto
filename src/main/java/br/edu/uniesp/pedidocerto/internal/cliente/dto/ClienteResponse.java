package br.edu.uniesp.pedidocerto.internal.cliente.dto;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;

public record ClienteResponse(Long id, String nome, String telefone, String email,
                              CanalContato canalPreferencial, boolean ativo) {

    public static ClienteResponse de(ClienteEntity c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getTelefone(), c.getEmail(),
                c.getCanalPreferencial(), c.isAtivo());
    }
}

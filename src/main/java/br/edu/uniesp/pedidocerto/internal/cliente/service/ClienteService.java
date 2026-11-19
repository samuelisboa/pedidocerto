package br.edu.uniesp.pedidocerto.internal.cliente.service;

import br.edu.uniesp.pedidocerto.internal.cliente.dto.AtualizarContatoRequest;
import br.edu.uniesp.pedidocerto.internal.cliente.dto.ClienteRequest;
import br.edu.uniesp.pedidocerto.internal.cliente.dto.ClienteResponse;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public ClienteResponse criar(ClienteRequest request) {
        ClienteEntity cliente = new ClienteEntity(request.nome(), request.telefone(), request.email(),
                request.canalPreferencial());
        return ClienteResponse.de(clientes.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(clientes.buscarOuFalhar(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clientes.findAll().stream().map(ClienteResponse::de).toList();
    }

    public ClienteResponse atualizarContato(Long id, AtualizarContatoRequest request) {
        ClienteEntity cliente = clientes.buscarOuFalhar(id);
        cliente.atualizarContato(request.telefone(), request.email());
        return ClienteResponse.de(cliente);
    }

    public ClienteResponse inativar(Long id) {
        ClienteEntity cliente = clientes.buscarOuFalhar(id);
        cliente.inativar();
        return ClienteResponse.de(cliente);
    }

    public ClienteResponse reativar(Long id) {
        ClienteEntity cliente = clientes.buscarOuFalhar(id);
        cliente.reativar();
        return ClienteResponse.de(cliente);
    }
}

package br.edu.uniesp.pedidocerto.internal.cliente.controller;

import br.edu.uniesp.pedidocerto.internal.cliente.dto.AtualizarContatoRequest;
import br.edu.uniesp.pedidocerto.internal.cliente.dto.ClienteRequest;
import br.edu.uniesp.pedidocerto.internal.cliente.dto.ClienteResponse;
import br.edu.uniesp.pedidocerto.internal.cliente.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/contato")
    public ClienteResponse atualizarContato(@PathVariable Long id, @RequestBody AtualizarContatoRequest request) {
        return service.atualizarContato(id, request);
    }

    @PostMapping("/{id}/inativar")
    public ClienteResponse inativar(@PathVariable Long id) {
        return service.inativar(id);
    }

    @PostMapping("/{id}/reativar")
    public ClienteResponse reativar(@PathVariable Long id) {
        return service.reativar(id);
    }
}

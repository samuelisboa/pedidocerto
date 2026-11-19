package br.edu.uniesp.pedidocerto.internal.pedido.controller;

import br.edu.uniesp.pedidocerto.internal.pedido.dto.CancelarPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.CriarPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.ItemPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.service.PedidoCadastroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/pedidos")
public class PedidoCadastroController {

    private final PedidoCadastroService service;

    public PedidoCadastroController(PedidoCadastroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody CriarPedidoRequest request) {
        PedidoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/pedidos/" + criado.id())).body(criado);
    }

    @PostMapping("/{id}/itens")
    public PedidoResponse adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemPedidoRequest request) {
        return service.adicionarItem(id, request);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public PedidoResponse removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        return service.removerItem(id, itemId);
    }

    @PostMapping("/{id}/aprovar")
    public PedidoResponse aprovar(@PathVariable Long id) {
        return service.aprovar(id);
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarPedidoRequest request) {
        return service.cancelar(id, request);
    }
}

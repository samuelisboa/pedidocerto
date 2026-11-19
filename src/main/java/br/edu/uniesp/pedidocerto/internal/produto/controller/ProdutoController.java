package br.edu.uniesp.pedidocerto.internal.produto.controller;

import br.edu.uniesp.pedidocerto.internal.produto.dto.ProdutoRequest;
import br.edu.uniesp.pedidocerto.internal.produto.dto.ProdutoResponse;
import br.edu.uniesp.pedidocerto.internal.produto.dto.ReajustePrecoRequest;
import br.edu.uniesp.pedidocerto.internal.produto.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/produtos/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/preco")
    public ProdutoResponse reajustarPreco(@PathVariable Long id, @Valid @RequestBody ReajustePrecoRequest request) {
        return service.reajustarPreco(id, request);
    }

    @PostMapping("/{id}/inativar")
    public ProdutoResponse inativar(@PathVariable Long id) {
        return service.inativar(id);
    }
}

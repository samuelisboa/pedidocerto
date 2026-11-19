package br.edu.uniesp.pedidocerto.internal.funcionario.controller;

import br.edu.uniesp.pedidocerto.internal.funcionario.dto.FuncionarioRequest;
import br.edu.uniesp.pedidocerto.internal.funcionario.dto.FuncionarioResponse;
import br.edu.uniesp.pedidocerto.internal.funcionario.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService service;

    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> criar(@Valid @RequestBody FuncionarioRequest request) {
        FuncionarioResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/funcionarios/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<FuncionarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/inativar")
    public FuncionarioResponse inativar(@PathVariable Long id) {
        return service.inativar(id);
    }
}

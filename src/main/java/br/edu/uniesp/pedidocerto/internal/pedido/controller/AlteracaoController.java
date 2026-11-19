package br.edu.uniesp.pedidocerto.internal.pedido.controller;

import br.edu.uniesp.pedidocerto.internal.pedido.dto.*;
import br.edu.uniesp.pedidocerto.internal.pedido.service.AlteracaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos/{pedidoId}/alteracoes")
public class AlteracaoController {

    private final AlteracaoService service;

    public AlteracaoController(AlteracaoService service) {
        this.service = service;
    }

    @PostMapping
    public PedidoResponse solicitar(@PathVariable Long pedidoId, @Valid @RequestBody SolicitarAlteracaoRequest request) {
        return service.solicitar(pedidoId, request);
    }

    @PostMapping("/{alteracaoId}/proposta")
    public PedidoResponse proporCusto(@PathVariable Long pedidoId, @PathVariable Long alteracaoId,
                                      @Valid @RequestBody PropostaCustoRequest request) {
        return service.proporCusto(pedidoId, alteracaoId, request);
    }

    @PostMapping("/{alteracaoId}/aprovar")
    public PedidoResponse aprovar(@PathVariable Long pedidoId, @PathVariable Long alteracaoId) {
        return service.aprovarPeloCliente(pedidoId, alteracaoId);
    }

    @PostMapping("/{alteracaoId}/recusar")
    public PedidoResponse recusar(@PathVariable Long pedidoId, @PathVariable Long alteracaoId,
                                  @Valid @RequestBody RecusarAlteracaoRequest request) {
        return service.recusar(pedidoId, alteracaoId, request);
    }

    @PostMapping("/{alteracaoId}/confirmar-leitura")
    public PedidoResponse confirmarLeitura(@PathVariable Long pedidoId, @PathVariable Long alteracaoId,
                                           @Valid @RequestBody ConfirmarLeituraRequest request) {
        return service.confirmarLeitura(pedidoId, alteracaoId, request);
    }
}

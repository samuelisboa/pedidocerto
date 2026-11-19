package br.edu.uniesp.pedidocerto.internal.pagamento.controller;

import br.edu.uniesp.pedidocerto.internal.pagamento.dto.*;
import br.edu.uniesp.pedidocerto.internal.pagamento.service.PagamentoService;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos/{pedidoId}")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/pagamentos")
    public PedidoResponse registrar(@PathVariable Long pedidoId, @Valid @RequestBody RegistrarPagamentoRequest request) {
        return service.registrar(pedidoId, request);
    }

    @PostMapping("/pagamentos/{pagamentoId}/conferir")
    public PedidoResponse conferir(@PathVariable Long pedidoId, @PathVariable Long pagamentoId,
                                   @Valid @RequestBody ConferirPagamentoRequest request) {
        return service.conferir(pedidoId, pagamentoId, request);
    }

    @GetMapping("/saldo")
    public SaldoResponse saldo(@PathVariable Long pedidoId) {
        return service.saldo(pedidoId);
    }

    @PutMapping("/entrega/modalidade")
    public PedidoResponse definirModalidade(@PathVariable Long pedidoId,
                                            @Valid @RequestBody ModalidadeEntregaRequest request) {
        return service.definirModalidadeDeEntrega(pedidoId, request);
    }

    @PostMapping("/entrega")
    public PedidoResponse registrarEntrega(@PathVariable Long pedidoId,
                                           @Valid @RequestBody RegistrarEntregaRequest request) {
        return service.registrarEntrega(pedidoId, request);
    }
}

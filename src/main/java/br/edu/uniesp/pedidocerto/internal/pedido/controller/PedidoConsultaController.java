package br.edu.uniesp.pedidocerto.internal.pedido.controller;

import br.edu.uniesp.pedidocerto.internal.pedido.dto.PainelResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResumoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.pedidocerto.internal.pedido.service.PedidoConsultaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoConsultaController {

    private final PedidoConsultaService service;

    public PedidoConsultaController(PedidoConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PedidoResumoResponse> listar(@RequestParam(required = false) StatusPedido status,
                                             @RequestParam(required = false) Long clienteId,
                                             @RequestParam(defaultValue = "false") boolean atrasados) {
        return service.listar(status, clienteId, atrasados);
    }

    @GetMapping("/painel")
    public PainelResponse painel() {
        return service.painel();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}

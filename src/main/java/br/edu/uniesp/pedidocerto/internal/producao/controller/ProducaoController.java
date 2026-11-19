package br.edu.uniesp.pedidocerto.internal.producao.controller;

import br.edu.uniesp.pedidocerto.internal.producao.dto.EtapaCatalogoResponse;
import br.edu.uniesp.pedidocerto.internal.producao.dto.IniciarEtapaRequest;
import br.edu.uniesp.pedidocerto.internal.producao.dto.PlanejarEtapaRequest;
import br.edu.uniesp.pedidocerto.internal.producao.dto.ProducaoResponse;
import br.edu.uniesp.pedidocerto.internal.producao.dto.ProgressoRequest;
import br.edu.uniesp.pedidocerto.internal.producao.service.ProducaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProducaoController {

    private final ProducaoService service;

    public ProducaoController(ProducaoService service) {
        this.service = service;
    }

    @GetMapping("/etapas-producao")
    public List<EtapaCatalogoResponse> catalogo() {
        return service.catalogoDeEtapas();
    }

    @GetMapping("/pedidos/{pedidoId}/producao")
    public ProducaoResponse consultar(@PathVariable Long pedidoId) {
        return service.consultar(pedidoId);
    }

    @PostMapping("/pedidos/{pedidoId}/producao/etapas")
    public ProducaoResponse planejarEtapa(@PathVariable Long pedidoId,
                                          @Valid @RequestBody PlanejarEtapaRequest request) {
        return service.planejarEtapa(pedidoId, request);
    }

    @PostMapping("/pedidos/{pedidoId}/producao/iniciar")
    public ProducaoResponse iniciarProducao(@PathVariable Long pedidoId) {
        return service.iniciarProducao(pedidoId);
    }

    @PostMapping("/pedidos/{pedidoId}/producao/etapas/{execucaoId}/iniciar")
    public ProducaoResponse iniciarEtapa(@PathVariable Long pedidoId, @PathVariable Long execucaoId,
                                         @Valid @RequestBody IniciarEtapaRequest request) {
        return service.iniciarEtapa(pedidoId, execucaoId, request);
    }

    @PatchMapping("/pedidos/{pedidoId}/producao/etapas/{execucaoId}/progresso")
    public ProducaoResponse atualizarProgresso(@PathVariable Long pedidoId, @PathVariable Long execucaoId,
                                               @Valid @RequestBody ProgressoRequest request) {
        return service.atualizarProgresso(pedidoId, execucaoId, request);
    }

    @PostMapping("/pedidos/{pedidoId}/producao/etapas/{execucaoId}/concluir")
    public ProducaoResponse concluirEtapa(@PathVariable Long pedidoId, @PathVariable Long execucaoId) {
        return service.concluirEtapa(pedidoId, execucaoId);
    }

    @PostMapping("/pedidos/{pedidoId}/producao/pronto")
    public ProducaoResponse marcarComoPronto(@PathVariable Long pedidoId) {
        return service.marcarComoPronto(pedidoId);
    }
}

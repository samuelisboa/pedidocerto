package br.edu.uniesp.pedidocerto.internal.producao.service;

import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.internal.producao.dto.EtapaCatalogoResponse;
import br.edu.uniesp.pedidocerto.internal.producao.dto.IniciarEtapaRequest;
import br.edu.uniesp.pedidocerto.internal.producao.dto.PlanejarEtapaRequest;
import br.edu.uniesp.pedidocerto.internal.producao.dto.ProducaoResponse;
import br.edu.uniesp.pedidocerto.internal.producao.dto.ProgressoRequest;
import br.edu.uniesp.pedidocerto.internal.producao.entity.ExecucaoEtapaEntity;
import br.edu.uniesp.pedidocerto.internal.producao.repository.EtapaProducaoRepository;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProducaoService {

    private final PedidoRepository pedidos;
    private final EtapaProducaoRepository etapas;
    private final FuncionarioRepository funcionarios;

    public ProducaoService(PedidoRepository pedidos, EtapaProducaoRepository etapas,
                           FuncionarioRepository funcionarios) {
        this.pedidos = pedidos;
        this.etapas = etapas;
        this.funcionarios = funcionarios;
    }

    @Transactional(readOnly = true)
    public List<EtapaCatalogoResponse> catalogoDeEtapas() {
        return etapas.findAllByOrderByOrdemAsc().stream().map(EtapaCatalogoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProducaoResponse consultar(Long pedidoId) {
        return ProducaoResponse.de(pedidos.buscarOuFalhar(pedidoId));
    }

    public ProducaoResponse planejarEtapa(Long pedidoId, PlanejarEtapaRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.planejarEtapa(etapas.buscarOuFalhar(request.etapaId()));
        pedidos.flush(); // gera o id da execução antes de montar a resposta
        return ProducaoResponse.de(pedido);
    }

    public ProducaoResponse iniciarProducao(Long pedidoId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.iniciarProducao();
        return ProducaoResponse.de(pedido);
    }

    public ProducaoResponse iniciarEtapa(Long pedidoId, Long execucaoId, IniciarEtapaRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        buscar(pedido, execucaoId).iniciar(funcionarios.buscarOuFalhar(request.responsavelId()),
                LocalDateTime.now());
        return ProducaoResponse.de(pedido);
    }

    public ProducaoResponse atualizarProgresso(Long pedidoId, Long execucaoId, ProgressoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        buscar(pedido, execucaoId).atualizarProgresso(request.percentual());
        return ProducaoResponse.de(pedido);
    }

    public ProducaoResponse concluirEtapa(Long pedidoId, Long execucaoId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        buscar(pedido, execucaoId).concluir(LocalDateTime.now());
        return ProducaoResponse.de(pedido);
    }

    public ProducaoResponse marcarComoPronto(Long pedidoId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.marcarComoPronto();
        return ProducaoResponse.de(pedido);
    }

    private ExecucaoEtapaEntity buscar(PedidoEntity pedido, Long execucaoId) {
        return pedido.getExecucoesEtapa().stream()
                .filter(e -> e.getId().equals(execucaoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Etapa do pedido", execucaoId));
    }
}

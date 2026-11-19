package br.edu.uniesp.pedidocerto.internal.pedido.service;

import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.*;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.AlteracaoPedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Alterações pedidas pelo cliente depois da aprovação. A classificação em verde, amarela ou vermelha
 * é feita pela política de alteração dentro do domínio; este serviço só orquestra o caso de uso.
 */
@Service
@Transactional
public class AlteracaoService {

    private final PedidoRepository pedidos;
    private final FuncionarioRepository funcionarios;

    public AlteracaoService(PedidoRepository pedidos, FuncionarioRepository funcionarios) {
        this.pedidos = pedidos;
        this.funcionarios = funcionarios;
    }

    public PedidoResponse solicitar(Long pedidoId, SolicitarAlteracaoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.solicitarAlteracao(request.descricao(), funcionarios.buscarOuFalhar(request.solicitadaPorId()),
                LocalDateTime.now());
        pedidos.flush(); // gera o id da alteração antes de montar a resposta
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse proporCusto(Long pedidoId, Long alteracaoId, PropostaCustoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        buscar(pedido, alteracaoId).proporCusto(request.custoAdicional(), request.diasAdicionais());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse aprovarPeloCliente(Long pedidoId, Long alteracaoId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.aprovarAlteracao(buscar(pedido, alteracaoId), LocalDateTime.now());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse recusar(Long pedidoId, Long alteracaoId, RecusarAlteracaoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        buscar(pedido, alteracaoId).recusar(request.motivo());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse confirmarLeitura(Long pedidoId, Long alteracaoId, ConfirmarLeituraRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.confirmarLeituraDaAlteracao(buscar(pedido, alteracaoId),
                funcionarios.buscarOuFalhar(request.funcionarioId()), LocalDateTime.now());
        return PedidoResponse.de(pedido);
    }

    private AlteracaoPedidoEntity buscar(PedidoEntity pedido, Long alteracaoId) {
        return pedido.getAlteracoes().stream()
                .filter(a -> a.getId().equals(alteracaoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alteração", alteracaoId));
    }
}

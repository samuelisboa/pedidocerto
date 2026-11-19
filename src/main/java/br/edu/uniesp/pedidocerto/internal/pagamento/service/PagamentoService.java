package br.edu.uniesp.pedidocerto.internal.pagamento.service;

import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pagamento.dto.*;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.PagamentoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Controle de sinal, parcelas e saldo, e a trava que impede entregar pedido com saldo pendente. */
@Service
@Transactional
public class PagamentoService {

    private final PedidoRepository pedidos;
    private final FuncionarioRepository funcionarios;

    public PagamentoService(PedidoRepository pedidos, FuncionarioRepository funcionarios) {
        this.pedidos = pedidos;
        this.funcionarios = funcionarios;
    }

    public PedidoResponse registrar(Long pedidoId, RegistrarPagamentoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.registrarPagamento(request.tipo(), request.forma(), request.valor(), LocalDateTime.now(),
                funcionarios.buscarOuFalhar(request.registradoPorId()));
        pedidos.flush(); // gera o id do pagamento antes de montar a resposta
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse conferir(Long pedidoId, Long pagamentoId, ConferirPagamentoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        PagamentoEntity pagamento = pedido.getPagamentos().stream()
                .filter(p -> p.getId().equals(pagamentoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento", pagamentoId));
        pagamento.conferir(funcionarios.buscarOuFalhar(request.funcionarioId()), LocalDateTime.now());
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public SaldoResponse saldo(Long pedidoId) {
        return SaldoResponse.de(pedidos.buscarOuFalhar(pedidoId));
    }

    public PedidoResponse definirModalidadeDeEntrega(Long pedidoId, ModalidadeEntregaRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        if (pedido.getEntrega() == null) {
            throw new RegraNegocioException("A entrega só é definida depois que o pedido é aprovado.");
        }
        pedido.getEntrega().definirModalidade(request.modalidade(), request.endereco());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse registrarEntrega(Long pedidoId, RegistrarEntregaRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.confirmarEntrega(funcionarios.buscarOuFalhar(request.responsavelId()), LocalDateTime.now());
        return PedidoResponse.de(pedido);
    }
}

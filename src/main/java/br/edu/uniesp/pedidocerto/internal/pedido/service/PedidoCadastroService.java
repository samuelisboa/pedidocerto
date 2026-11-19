package br.edu.uniesp.pedidocerto.internal.pedido.service;

import br.edu.uniesp.pedidocerto.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.CancelarPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.CriarPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.ItemPedidoRequest;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.ItemPedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Casos de uso de criação e ciclo de vida inicial do pedido (orçamento, aprovação e cancelamento). */
@Service
@Transactional
public class PedidoCadastroService {

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final FuncionarioRepository funcionarios;
    private final ProdutoRepository produtos;
    private final PoliticaAlteracaoRepository politicas;

    public PedidoCadastroService(PedidoRepository pedidos, ClienteRepository clientes,
                                 FuncionarioRepository funcionarios, ProdutoRepository produtos,
                                 PoliticaAlteracaoRepository politicas) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.produtos = produtos;
        this.politicas = politicas;
    }

    public PedidoResponse criar(CriarPedidoRequest request) {
        PedidoEntity pedido = new PedidoEntity(
                pedidos.proximoNumero(),
                clientes.buscarOuFalhar(request.clienteId()),
                funcionarios.buscarOuFalhar(request.atendenteId()),
                request.canalOrigem(),
                politicas.politicaAtiva(),
                request.dataEntregaPrevista(),
                LocalDateTime.now());
        request.itens().forEach(item -> adicionar(pedido, item));
        return PedidoResponse.de(pedidos.save(pedido));
    }

    public PedidoResponse adicionarItem(Long pedidoId, ItemPedidoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        adicionar(pedido, request);
        pedidos.flush(); // gera o id do novo item antes de montar a resposta
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse removerItem(Long pedidoId, Long itemId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        ItemPedidoEntity item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do pedido", itemId));
        pedido.removerItem(item);
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse aprovar(Long pedidoId) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.aprovar(LocalDateTime.now());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse cancelar(Long pedidoId, CancelarPedidoRequest request) {
        PedidoEntity pedido = pedidos.buscarOuFalhar(pedidoId);
        pedido.cancelar(request.motivo());
        return PedidoResponse.de(pedido);
    }

    private void adicionar(PedidoEntity pedido, ItemPedidoRequest item) {
        pedido.adicionarItem(produtos.buscarOuFalhar(item.produtoId()), item.quantidade(), item.personalizacao());
    }
}

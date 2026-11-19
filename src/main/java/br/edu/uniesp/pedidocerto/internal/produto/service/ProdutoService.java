package br.edu.uniesp.pedidocerto.internal.produto.service;

import br.edu.uniesp.pedidocerto.internal.produto.dto.ProdutoRequest;
import br.edu.uniesp.pedidocerto.internal.produto.dto.ProdutoResponse;
import br.edu.uniesp.pedidocerto.internal.produto.dto.ReajustePrecoRequest;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtos;

    public ProdutoService(ProdutoRepository produtos) {
        this.produtos = produtos;
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        ProdutoEntity produto = new ProdutoEntity(request.nome(), request.descricao(), request.precoBase(),
                request.horasProducaoPorUnidade());
        return ProdutoResponse.de(produtos.save(produto));
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoResponse.de(produtos.buscarOuFalhar(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtos.findAll().stream().map(ProdutoResponse::de).toList();
    }

    public ProdutoResponse reajustarPreco(Long id, ReajustePrecoRequest request) {
        ProdutoEntity produto = produtos.buscarOuFalhar(id);
        produto.reajustarPreco(request.preco());
        return ProdutoResponse.de(produto);
    }

    public ProdutoResponse inativar(Long id) {
        ProdutoEntity produto = produtos.buscarOuFalhar(id);
        produto.inativar();
        return ProdutoResponse.de(produto);
    }
}

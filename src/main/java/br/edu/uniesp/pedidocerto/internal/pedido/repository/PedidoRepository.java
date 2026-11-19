package br.edu.uniesp.pedidocerto.internal.pedido.repository;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    default PedidoEntity buscarOuFalhar(Long id) {
        return findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    default String proximoNumero() {
        return String.format("PED-%06d", count() + 1);
    }
}

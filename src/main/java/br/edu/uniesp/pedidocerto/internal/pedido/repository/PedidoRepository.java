package br.edu.uniesp.pedidocerto.internal.pedido.repository;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    List<PedidoEntity> findAllByOrderByDataEntregaPrevistaAsc();

    List<PedidoEntity> findByStatusOrderByDataEntregaPrevistaAsc(StatusPedido status);

    default PedidoEntity buscarOuFalhar(Long id) {
        return findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    default String proximoNumero() {
        return String.format("PED-%06d", count() + 1);
    }
}

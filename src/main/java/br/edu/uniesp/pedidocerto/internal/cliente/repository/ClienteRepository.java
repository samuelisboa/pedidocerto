package br.edu.uniesp.pedidocerto.internal.cliente.repository;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    default ClienteEntity buscarOuFalhar(Long id) {
        return findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}

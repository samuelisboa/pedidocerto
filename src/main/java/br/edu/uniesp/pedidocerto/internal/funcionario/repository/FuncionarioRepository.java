package br.edu.uniesp.pedidocerto.internal.funcionario.repository;

import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<FuncionarioEntity, Long> {

    default FuncionarioEntity buscarOuFalhar(Long id) {
        return findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário", id));
    }
}

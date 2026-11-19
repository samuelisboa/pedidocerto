package br.edu.uniesp.pedidocerto.internal.producao.repository;

import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtapaProducaoRepository extends JpaRepository<EtapaProducaoEntity, Long> {

    List<EtapaProducaoEntity> findAllByOrderByOrdemAsc();

    default EtapaProducaoEntity buscarOuFalhar(Long id) {
        return findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Etapa de produção", id));
    }
}

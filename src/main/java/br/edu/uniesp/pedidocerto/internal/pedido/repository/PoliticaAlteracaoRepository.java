package br.edu.uniesp.pedidocerto.internal.pedido.repository;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliticaAlteracaoRepository extends JpaRepository<PoliticaAlteracaoEntity, Long> {

    Optional<PoliticaAlteracaoEntity> findFirstByAtivaTrue();

    default PoliticaAlteracaoEntity politicaAtiva() {
        return findFirstByAtivaTrue()
                .orElseThrow(() -> new RegraNegocioException("Não há política de alteração ativa cadastrada."));
    }
}

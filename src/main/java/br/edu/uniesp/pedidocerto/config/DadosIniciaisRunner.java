package br.edu.uniesp.pedidocerto.config;

import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.repository.EtapaProducaoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Cria a política de alteração padrão e as etapas de produção na primeira execução. */
@Component
@ConditionalOnProperty(name = "pedidocerto.dados-iniciais", havingValue = "true", matchIfMissing = true)
public class DadosIniciaisRunner implements ApplicationRunner {

    private final PoliticaAlteracaoRepository politicas;
    private final EtapaProducaoRepository etapas;

    public DadosIniciaisRunner(PoliticaAlteracaoRepository politicas, EtapaProducaoRepository etapas) {
        this.politicas = politicas;
        this.etapas = etapas;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (politicas.count() == 0) {
            politicas.save(new PoliticaAlteracaoEntity("Padrão", 48, 70, 72));
        }
        if (etapas.count() == 0) {
            etapas.save(new EtapaProducaoEntity("Corte", 1, false));
            etapas.save(new EtapaProducaoEntity("Impressão", 2, true));
            etapas.save(new EtapaProducaoEntity("Acabamento", 3, false));
        }
    }
}

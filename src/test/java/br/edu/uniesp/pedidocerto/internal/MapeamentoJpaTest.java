package br.edu.uniesp.pedidocerto.internal;

import br.edu.uniesp.pedidocerto.internal.agenda.entity.CapacidadeDiaEntity;
import br.edu.uniesp.pedidocerto.internal.atendimento.entity.ContatoEntity;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.material.entity.MaterialEntity;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Garante que todos os mapeamentos JPA geram um esquema válido e persistem o agregado completo. */
@DataJpaTest
class MapeamentoJpaTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    void persisteORelacionamentoCompletoDeUmPedido() {
        LocalDateTime agora = LocalDateTime.of(2026, 12, 1, 10, 0);

        ClienteEntity cliente = new ClienteEntity("Empresa Alfa", "83999990000", null, CanalContato.WHATSAPP);
        FuncionarioEntity atendente = new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO);
        MaterialEntity papel = new MaterialEntity("Papel", "folha", new BigDecimal("100"), new BigDecimal("20"));
        ProdutoEntity caneca = new ProdutoEntity("Caneca", null, new BigDecimal("8.00"), new BigDecimal("0.10"));
        caneca.vincularMaterial(papel);
        PoliticaAlteracaoEntity politica = new PoliticaAlteracaoEntity("Padrão", 48, 70, 72);
        EtapaProducaoEntity impressao = new EtapaProducaoEntity("Impressão", 1, true);
        em.persist(cliente);
        em.persist(atendente);
        em.persist(papel);
        em.persist(caneca);
        em.persist(politica);
        em.persist(impressao);
        em.persist(new CapacidadeDiaEntity(LocalDate.of(2026, 12, 12), new BigDecimal("40")));

        PedidoEntity pedido = new PedidoEntity("PED-0001", cliente, atendente, CanalContato.WHATSAPP, politica,
                agora.plusDays(11), agora);
        pedido.adicionarItem(caneca, 300, "Logo nova");
        pedido.aprovar(agora);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), agora, atendente);
        pedido.planejarEtapa(impressao);
        pedido.definirNecessidadeDeMaterial(papel, new BigDecimal("300"));
        em.persist(pedido);

        ContatoEntity contato = new ContatoEntity(cliente, CanalContato.WHATSAPP, "Quero 300 canecas",
                agora.minusHours(12), true);
        contato.vincularAoPedido(pedido);
        em.persist(contato);

        em.flush();
        em.clear();

        PedidoEntity lido = em.find(PedidoEntity.class, pedido.getId());
        assertEquals(1, lido.getItens().size());
        assertEquals(1, lido.getPagamentos().size());
        assertEquals(1, lido.getExecucoesEtapa().size());
        assertEquals(1, lido.getNecessidadesMaterial().size());
        assertNotNull(lido.getEntrega());
        assertEquals(new BigDecimal("2400.00"), lido.getValorTotal());
        assertEquals(1, em.find(ProdutoEntity.class, caneca.getId()).getMateriaisPadrao().size());
    }
}

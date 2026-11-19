package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.material.entity.MaterialEntity;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.ExecucaoEtapaEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoEntityTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 1, 10, 0);

    private ClienteEntity cliente;
    private FuncionarioEntity atendente;
    private FuncionarioEntity operador;
    private ProdutoEntity caneca;
    private PoliticaAlteracaoEntity politica;
    private PedidoEntity pedido;

    @BeforeEach
    void preparar() {
        cliente = new ClienteEntity("Empresa Alfa", "83999990000", null, CanalContato.WHATSAPP);
        atendente = new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO);
        operador = new FuncionarioEntity("Bruno", SetorFuncionario.PRODUCAO);
        caneca = new ProdutoEntity("Caneca personalizada", null, new BigDecimal("8.00"), new BigDecimal("0.10"));
        politica = new PoliticaAlteracaoEntity("Padrão", 48, 70, 72);
        pedido = new PedidoEntity("PED-0482", cliente, atendente, CanalContato.WHATSAPP, politica,
                AGORA.plusDays(11), AGORA);
        pedido.adicionarItem(caneca, 300, "Logo antiga");
    }

    @Test
    void valorTotalSegueOsItensDoOrcamento() {
        assertEquals(new BigDecimal("2400.00"), pedido.getValorTotal());
        pedido.alterarQuantidadeDoItem(pedido.getItens().get(0), 100);
        assertEquals(new BigDecimal("800.00"), pedido.getValorTotal());
    }

    @Test
    void naoAprovaPedidoSemItens() {
        PedidoEntity vazio = new PedidoEntity("PED-1", cliente, atendente, CanalContato.PRESENCIAL, politica,
                AGORA.plusDays(3), AGORA);
        assertThrows(RegraNegocioException.class, () -> vazio.aprovar(AGORA));
    }

    @Test
    void producaoSoComecaComSinalRegistrado() {
        pedido.aprovar(AGORA);
        assertThrows(RegraNegocioException.class, pedido::iniciarProducao);

        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), AGORA, atendente);
        pedido.iniciarProducao();
        assertEquals(StatusPedido.EM_PRODUCAO, pedido.getStatus());
    }

    @Test
    void entregaFicaBloqueadaEnquantoHouverSaldoPendente() {
        pedido.aprovar(AGORA);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), AGORA, atendente);
        pedido.iniciarProducao();
        pedido.marcarComoPronto();

        assertEquals(new BigDecimal("1680.00"), pedido.saldoPendente());
        assertThrows(RegraNegocioException.class, () -> pedido.confirmarEntrega(atendente, AGORA.plusDays(10)));

        pedido.registrarPagamento(TipoPagamento.SALDO, FormaPagamento.DINHEIRO, new BigDecimal("1680.00"),
                AGORA.plusDays(10), atendente);
        pedido.confirmarEntrega(atendente, AGORA.plusDays(10));
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
        assertTrue(pedido.getEntrega().isSaldoConferido());
    }

    @Test
    void pagamentoNaoPodeExcederOSaldo() {
        pedido.aprovar(AGORA);
        assertThrows(RegraNegocioException.class, () -> pedido.registrarPagamento(
                TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("2500.00"), AGORA, atendente));
    }

    @Test
    void alteracaoAntesDeProduzirEhVerdeESemCusto() {
        pedido.aprovar(AGORA);
        AlteracaoPedidoEntity alteracao = pedido.solicitarAlteracao("Trocar a logo", atendente, AGORA.plusHours(2));

        assertEquals(ZonaAlteracao.VERDE, alteracao.getZona());
        assertEquals(StatusAlteracao.APROVADA_PELO_CLIENTE, alteracao.getStatus());
        assertEquals(2, pedido.getVersao());
        assertEquals(new BigDecimal("2400.00"), pedido.getValorTotal());
    }

    @Test
    void alteracaoComProducaoIniciadaEhAmarelaECobraCustoENovoPrazo() {
        EtapaProducaoEntity impressao = new EtapaProducaoEntity("Impressão", 1, false);
        pedido.aprovar(AGORA);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), AGORA, atendente);
        ExecucaoEtapaEntity execucao = pedido.planejarEtapa(impressao);
        pedido.iniciarProducao();
        execucao.iniciar(operador, AGORA.plusHours(1));
        execucao.atualizarProgresso(40);

        LocalDateTime pedidoEm = AGORA.plusHours(2);
        AlteracaoPedidoEntity alteracao = pedido.solicitarAlteracao("Logo nova", atendente, pedidoEm);
        assertEquals(ZonaAlteracao.AMARELA, alteracao.getZona());
        assertEquals(StatusAlteracao.SOLICITADA, alteracao.getStatus());

        alteracao.proporCusto(new BigDecimal("180.00"), 1);
        pedido.aprovarAlteracao(alteracao, pedidoEm.plusMinutes(5));

        assertEquals(new BigDecimal("2580.00"), pedido.getValorTotal());
        assertEquals(AGORA.plusDays(12), pedido.getDataEntregaPrevista());
        assertEquals(2, pedido.getVersao());
    }

    @Test
    void producaoPrecisaConfirmarALeituraAntesDeAvancar() {
        EtapaProducaoEntity impressao = new EtapaProducaoEntity("Impressão", 1, false);
        EtapaProducaoEntity acabamento = new EtapaProducaoEntity("Acabamento", 2, false);
        pedido.aprovar(AGORA);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), AGORA, atendente);
        ExecucaoEtapaEntity primeira = pedido.planejarEtapa(impressao);
        ExecucaoEtapaEntity segunda = pedido.planejarEtapa(acabamento);
        pedido.iniciarProducao();
        primeira.iniciar(operador, AGORA.plusHours(1));
        primeira.concluir(AGORA.plusHours(5));

        // metade do pedido produzida (50%): abaixo do limite de 70%, logo a alteração é amarela
        AlteracaoPedidoEntity alteracao = pedido.solicitarAlteracao("Mudar cor", atendente, AGORA.plusHours(6));
        assertEquals(ZonaAlteracao.AMARELA, alteracao.getZona());
        alteracao.proporCusto(BigDecimal.ZERO, 0);
        pedido.aprovarAlteracao(alteracao, AGORA.plusHours(6));

        segunda.iniciar(operador, AGORA.plusHours(7));
        segunda.concluir(AGORA.plusHours(8));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, pedido::marcarComoPronto);
        assertTrue(erro.getMessage().contains("confirmar a leitura"));

        pedido.confirmarLeituraDaAlteracao(alteracao, operador, AGORA.plusHours(9));
        pedido.marcarComoPronto();
        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
    }

    @Test
    void alteracaoAposEtapaIrreversivelEhVermelhaERecusada() {
        EtapaProducaoEntity impressao = new EtapaProducaoEntity("Impressão", 1, true);
        pedido.aprovar(AGORA);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), AGORA, atendente);
        ExecucaoEtapaEntity execucao = pedido.planejarEtapa(impressao);
        pedido.iniciarProducao();
        execucao.iniciar(operador, AGORA.plusHours(1));
        execucao.concluir(AGORA.plusHours(3));

        AlteracaoPedidoEntity alteracao = pedido.solicitarAlteracao("Trocar tudo", atendente, AGORA.plusHours(4));
        assertEquals(ZonaAlteracao.VERMELHA, alteracao.getZona());
        assertEquals(StatusAlteracao.RECUSADA, alteracao.getStatus());
        assertEquals(1, pedido.getVersao());
    }

    @Test
    void pedidoFechadoPertoDaEntregaNaoAceitaAlteracao() {
        pedido.aprovar(AGORA);
        LocalDateTime proximoDaEntrega = pedido.getDataEntregaPrevista().minusHours(10);
        AlteracaoPedidoEntity alteracao = pedido.solicitarAlteracao("Mudar", atendente, proximoDaEntrega);
        assertEquals(ZonaAlteracao.VERMELHA, alteracao.getZona());
    }

    @Test
    void materialFaltanteApareceNaListaDeCompras() {
        MaterialEntity papel = new MaterialEntity("Papel de transferência", "folha", new BigDecimal("120"), new BigDecimal("50"));
        pedido.aprovar(AGORA);
        pedido.definirNecessidadeDeMaterial(papel, new BigDecimal("300"));

        assertEquals(1, pedido.materiaisAComprar().size());
        assertEquals(new BigDecimal("180"), pedido.materiaisAComprar().get(0).quantidadeAComprar());
    }

    @Test
    void clienteInativoNaoAbrePedido() {
        cliente.inativar();
        assertThrows(RegraNegocioException.class, () -> new PedidoEntity("PED-2", cliente, atendente,
                CanalContato.WHATSAPP, politica, AGORA.plusDays(2), AGORA));
    }
}

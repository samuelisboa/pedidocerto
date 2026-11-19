package br.edu.uniesp.pedidocerto.internal.alteracoes;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.repository.EtapaProducaoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "pedidocerto.dados-iniciais=false")
@AutoConfigureMockMvc
@Transactional
class AlteracaoProducaoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClienteRepository clientes;
    @Autowired
    private FuncionarioRepository funcionarios;
    @Autowired
    private ProdutoRepository produtos;
    @Autowired
    private PoliticaAlteracaoRepository politicas;
    @Autowired
    private EtapaProducaoRepository etapas;
    @Autowired
    private PedidoRepository pedidos;

    private FuncionarioEntity atendente;
    private FuncionarioEntity operador;
    private EtapaProducaoEntity corte;
    private EtapaProducaoEntity impressao;
    private PedidoEntity pedido;
    private String base;

    @BeforeEach
    void preparar() {
        ClienteEntity cliente = clientes.save(new ClienteEntity("Empresa Alfa", "83999990000", null,
                CanalContato.WHATSAPP));
        atendente = funcionarios.save(new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO));
        operador = funcionarios.save(new FuncionarioEntity("Bruno", SetorFuncionario.PRODUCAO));
        ProdutoEntity caneca = produtos.save(new ProdutoEntity("Caneca", null, new BigDecimal("8.00"),
                new BigDecimal("0.10")));
        PoliticaAlteracaoEntity politica = politicas.save(new PoliticaAlteracaoEntity("Padrão", 48, 70, 72));
        corte = etapas.save(new EtapaProducaoEntity("Corte", 1, false));
        impressao = etapas.save(new EtapaProducaoEntity("Impressão", 2, true));

        LocalDateTime agora = LocalDateTime.now();
        pedido = new PedidoEntity("PED-000001", cliente, atendente, CanalContato.WHATSAPP, politica,
                agora.plusDays(10), agora);
        pedido.adicionarItem(caneca, 300, "Logo antiga"); // R$ 2.400,00
        pedido.aprovar(agora);
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"), agora, atendente);
        pedidos.save(pedido);
        base = "/pedidos/" + pedido.getId();
    }

    private String json(String modelo, Object... args) {
        return modelo.formatted(args);
    }

    private String enviar(String caminho, String corpo) throws Exception {
        return mvc.perform(post(base + caminho)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andReturn().getResponse().getContentAsString();
    }

    private Integer planejar(EtapaProducaoEntity etapa, int posicao) throws Exception {
        String corpo = enviar("/producao/etapas", json("{\"etapaId\":%d}", etapa.getId()));
        return JsonPath.read(corpo, "$.etapas[" + posicao + "].id");
    }

    private void iniciarEtapa(Integer execucaoId) throws Exception {
        mvc.perform(post(base + "/producao/etapas/" + execucaoId + "/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"responsavelId\":%d}", operador.getId())))
                .andExpect(status().isOk());
    }

    private String solicitar() throws Exception {
        return enviar("/alteracoes", json("{\"descricao\":\"Trocar a logo\",\"solicitadaPorId\":%d}", atendente.getId()));
    }

    @Test
    void alteracaoSemProducaoIniciadaEhVerdeESemCusto() throws Exception {
        mvc.perform(post(base + "/alteracoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"descricao\":\"Trocar a logo\",\"solicitadaPorId\":%d}", atendente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].zona").value("VERDE"))
                .andExpect(jsonPath("$.alteracoes[0].status").value("APROVADA_PELO_CLIENTE"))
                .andExpect(jsonPath("$.versao").value(2))
                .andExpect(jsonPath("$.valorTotal").value(2400.00));
    }

    @Test
    void producaoSoAvancaDepoisDeConfirmarALeitura() throws Exception {
        String corpo = solicitar();
        Integer alteracaoId = JsonPath.read(corpo, "$.alteracoes[0].id");

        // sem a confirmação de leitura, a produção não pode começar
        mvc.perform(post(base + "/producao/iniciar"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(post(base + "/alteracoes/" + alteracaoId + "/confirmar-leitura")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"funcionarioId\":%d}", operador.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].status").value("CONFIRMADA_PELA_PRODUCAO"));

        mvc.perform(post(base + "/producao/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPedido").value("EM_PRODUCAO"));
    }

    @Test
    void somenteAProducaoConfirmaALeitura() throws Exception {
        Integer alteracaoId = JsonPath.read(solicitar(), "$.alteracoes[0].id");

        mvc.perform(post(base + "/alteracoes/" + alteracaoId + "/confirmar-leitura")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"funcionarioId\":%d}", atendente.getId())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void alteracaoComProducaoEmAndamentoEhAmarelaECobraCustoENovoPrazo() throws Exception {
        Integer execCorte = planejar(corte, 0);
        planejar(impressao, 1);
        mvc.perform(post(base + "/producao/iniciar"))
                .andExpect(status().isOk());
        iniciarEtapa(execCorte);
        mvc.perform(patch(base + "/producao/etapas/" + execCorte + "/progresso")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"percentual\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualProduzido").value(30));

        Integer alteracaoId = JsonPath.read(solicitar(), "$.alteracoes[0].id");
        mvc.perform(post(base + "/alteracoes/" + alteracaoId + "/proposta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"custoAdicional\":180.00,\"diasAdicionais\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].zona").value("AMARELA"))
                .andExpect(jsonPath("$.alteracoes[0].status").value("AGUARDANDO_CLIENTE"));

        mvc.perform(post(base + "/alteracoes/" + alteracaoId + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].status").value("APROVADA_PELO_CLIENTE"))
                .andExpect(jsonPath("$.valorTotal").value(2580.00))
                .andExpect(jsonPath("$.versao").value(2));
    }

    @Test
    void alteracaoAposEtapaIrreversivelEhVermelhaERecusada() throws Exception {
        planejar(corte, 0);
        Integer execImpressao = planejar(impressao, 1);
        mvc.perform(post(base + "/producao/iniciar"))
                .andExpect(status().isOk());
        iniciarEtapa(execImpressao);
        mvc.perform(post(base + "/producao/etapas/" + execImpressao + "/concluir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapaIrreversivelConcluida").value(true));

        mvc.perform(post(base + "/alteracoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"descricao\":\"Trocar tudo\",\"solicitadaPorId\":%d}", atendente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].zona").value("VERMELHA"))
                .andExpect(jsonPath("$.alteracoes[0].status").value("RECUSADA"))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void recusaAlteracaoAguardandoOCliente() throws Exception {
        Integer execCorte = planejar(corte, 0);
        planejar(impressao, 1);
        mvc.perform(post(base + "/producao/iniciar"));
        iniciarEtapa(execCorte);
        mvc.perform(patch(base + "/producao/etapas/" + execCorte + "/progresso")
                .contentType(MediaType.APPLICATION_JSON).content("{\"percentual\":20}"));
        Integer alteracaoId = JsonPath.read(solicitar(), "$.alteracoes[0].id");

        mvc.perform(post(base + "/alteracoes/" + alteracaoId + "/recusar")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Cliente não aceitou o custo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].status").value("RECUSADA"));
    }

    @Test
    void pedidoSoFicaProntoComTodasAsEtapasConcluidas() throws Exception {
        Integer execCorte = planejar(corte, 0);
        planejar(impressao, 1);
        mvc.perform(post(base + "/producao/iniciar"));
        iniciarEtapa(execCorte);
        mvc.perform(post(base + "/producao/etapas/" + execCorte + "/concluir"));

        mvc.perform(post(base + "/producao/pronto"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaProgressoInvalido() throws Exception {
        Integer execCorte = planejar(corte, 0);
        mvc.perform(patch(base + "/producao/etapas/" + execCorte + "/progresso")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"percentual\":150}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaOCatalogoDeEtapas() throws Exception {
        mvc.perform(get("/etapas-producao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Corte"))
                .andExpect(jsonPath("$[1].irreversivel").value(true));
    }

    @Test
    void consultaOAndamentoDaProducao() throws Exception {
        planejar(corte, 0);
        mvc.perform(get(base + "/producao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualProduzido").value(0))
                .andExpect(jsonPath("$.etapas[0].nome").value("Corte"));
    }
}

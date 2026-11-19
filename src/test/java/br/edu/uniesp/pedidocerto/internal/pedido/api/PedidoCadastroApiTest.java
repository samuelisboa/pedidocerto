package br.edu.uniesp.pedidocerto.internal.pedido.api;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import com.jayway.jsonpath.JsonPath;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "pedidocerto.dados-iniciais=false")
@AutoConfigureMockMvc
@Transactional
class PedidoCadastroApiTest {

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

    private ClienteEntity cliente;
    private FuncionarioEntity atendente;
    private ProdutoEntity caneca;

    @BeforeEach
    void preparar() {
        cliente = clientes.save(new ClienteEntity("Empresa Alfa", "83999990000", null, CanalContato.WHATSAPP));
        atendente = funcionarios.save(new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO));
        caneca = produtos.save(new ProdutoEntity("Caneca", null, new BigDecimal("8.00"), new BigDecimal("0.10")));
        politicas.save(new PoliticaAlteracaoEntity("Padrão", 48, 70, 72));
    }

    private String corpoPedido(Long clienteId, int quantidade) {
        String entrega = LocalDateTime.now().plusDays(10).withNano(0).toString();
        return """
                {"clienteId":%d,"atendenteId":%d,"canalOrigem":"WHATSAPP","dataEntregaPrevista":"%s",
                 "itens":[{"produtoId":%d,"quantidade":%d,"personalizacao":"Logo da empresa"}]}"""
                .formatted(clienteId, atendente.getId(), entrega, caneca.getId(), quantidade);
    }

    private String criarPedido() throws Exception {
        return mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(cliente.getId(), 300)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String idDe(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    @Test
    void criaPedidoEmOrcamentoComValorTotal() throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(cliente.getId(), 300)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORCAMENTO"))
                .andExpect(jsonPath("$.numero").value("PED-000001"))
                .andExpect(jsonPath("$.valorTotal").value(2400.00))
                .andExpect(jsonPath("$.itens[0].subtotal").value(2400.00));
    }

    @Test
    void aprovaPedido() throws Exception {
        String id = idDe(criarPedido());
        mvc.perform(post("/pedidos/" + id + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void adicionaERemoveItemNoOrcamento() throws Exception {
        String id = idDe(criarPedido());
        String comItem = mvc.perform(post("/pedidos/" + id + "/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":%d,\"quantidade\":10}".formatted(caneca.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(2480.00))
                .andReturn().getResponse().getContentAsString();
        Integer itemId = JsonPath.read(comItem, "$.itens[1].id");

        mvc.perform(delete("/pedidos/" + id + "/itens/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(2400.00));
    }

    @Test
    void naoAdicionaItemDepoisDeAprovado() throws Exception {
        String id = idDe(criarPedido());
        mvc.perform(post("/pedidos/" + id + "/aprovar")).andExpect(status().isOk());

        mvc.perform(post("/pedidos/" + id + "/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":%d,\"quantidade\":10}".formatted(caneca.getId())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoAbrePedidoParaClienteInativo() throws Exception {
        cliente.inativar();
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(cliente.getId(), 10)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(9999L, 10)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaPedidoSemItens() throws Exception {
        String entrega = LocalDateTime.now().plusDays(10).withNano(0).toString();
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":%d,"atendenteId":%d,"canalOrigem":"WHATSAPP",
                                 "dataEntregaPrevista":"%s","itens":[]}""".formatted(cliente.getId(), atendente.getId(), entrega)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaDataDeEntregaNoPassado() throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":%d,"atendenteId":%d,"canalOrigem":"WHATSAPP",
                                 "dataEntregaPrevista":"2020-01-01T10:00:00",
                                 "itens":[{"produtoId":%d,"quantidade":1}]}""".formatted(cliente.getId(), atendente.getId(), caneca.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelaPedidoComMotivo() throws Exception {
        String id = idDe(criarPedido());
        mvc.perform(post("/pedidos/" + id + "/cancelar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cliente desistiu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}

package br.edu.uniesp.pedidocerto.internal.pagamento;

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
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "pedidocerto.dados-iniciais=false")
@AutoConfigureMockMvc
@Transactional
class PagamentoApiTest {

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
    private PedidoRepository pedidos;

    private FuncionarioEntity atendente;
    private PedidoEntity pedido;

    @BeforeEach
    void preparar() {
        ClienteEntity cliente = clientes.save(new ClienteEntity("Empresa Alfa", "83999990000", null,
                CanalContato.WHATSAPP));
        atendente = funcionarios.save(new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO));
        ProdutoEntity caneca = produtos.save(new ProdutoEntity("Caneca", null, new BigDecimal("8.00"),
                new BigDecimal("0.10")));
        PoliticaAlteracaoEntity politica = politicas.save(new PoliticaAlteracaoEntity("Padrão", 48, 70, 72));

        LocalDateTime agora = LocalDateTime.now();
        pedido = new PedidoEntity("PED-000001", cliente, atendente, CanalContato.WHATSAPP, politica,
                agora.plusDays(10), agora);
        pedido.adicionarItem(caneca, 300, null); // R$ 2.400,00
        pedido.aprovar(agora);
        pedidos.save(pedido);
    }

    private String pagamento(String tipo, String valor) {
        return """
                {"tipo":"%s","forma":"PIX","valor":%s,"registradoPorId":%d}""".formatted(tipo, valor, atendente.getId());
    }

    private void deixarPronto() {
        pedido.registrarPagamento(TipoPagamento.SINAL, FormaPagamento.PIX, new BigDecimal("720.00"),
                LocalDateTime.now(), atendente);
        pedido.iniciarProducao();
        pedido.marcarComoPronto();
        pedidos.flush();
    }

    @Test
    void registraSinalEAtualizaOSaldo() throws Exception {
        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento("SINAL", "720.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorPago").value(720.00))
                .andExpect(jsonPath("$.saldoPendente").value(1680.00))
                .andExpect(jsonPath("$.pagamentos[0].id").isNumber());

        mvc.perform(get("/pedidos/" + pedido.getId() + "/saldo"))
                .andExpect(jsonPath("$.quitado").value(false))
                .andExpect(jsonPath("$.saldoPendente").value(1680.00));
    }

    @Test
    void rejeitaPagamentoMaiorQueOSaldo() throws Exception {
        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento("SINAL", "2500.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void pagamentoDoSaldoPrecisaQuitarOValorPendente() throws Exception {
        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento("SALDO", "100.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaValorInvalido() throws Exception {
        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento("SINAL", "0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void conferePagamento() throws Exception {
        String corpo = mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON).content(pagamento("SINAL", "720.00")))
                .andReturn().getResponse().getContentAsString();
        Integer pagamentoId = JsonPath.read(corpo, "$.pagamentos[0].id");

        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos/" + pagamentoId + "/conferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"funcionarioId\":" + atendente.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagamentos[0].conferido").value(true));
    }

    @Test
    void entregaFicaBloqueadaComSaldoPendente() throws Exception {
        deixarPronto();

        mvc.perform(post("/pedidos/" + pedido.getId() + "/entrega").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsavelId\":" + atendente.getId() + "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("saldo pendente")));

        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
    }

    @Test
    void entregaLiberadaDepoisDeQuitarOSaldo() throws Exception {
        deixarPronto();

        mvc.perform(post("/pedidos/" + pedido.getId() + "/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento("SALDO", "1680.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoPendente").value(0));

        mvc.perform(post("/pedidos/" + pedido.getId() + "/entrega").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsavelId\":" + atendente.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void entregaEmDomicilioExigeEndereco() throws Exception {
        mvc.perform(put("/pedidos/" + pedido.getId() + "/entrega/modalidade")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"modalidade\":\"DOMICILIO\"}"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(put("/pedidos/" + pedido.getId() + "/entrega/modalidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modalidade\":\"DOMICILIO\",\"endereco\":\"Rua das Flores, 100\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pedidos/9999/saldo")).andExpect(status().isNotFound());
    }
}

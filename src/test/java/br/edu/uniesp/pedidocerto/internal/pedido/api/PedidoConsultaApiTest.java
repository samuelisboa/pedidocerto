package br.edu.uniesp.pedidocerto.internal.pedido.api;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.SetorFuncionario;
import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PoliticaAlteracaoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PoliticaAlteracaoRepository;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "pedidocerto.dados-iniciais=false")
@AutoConfigureMockMvc
@Transactional
class PedidoConsultaApiTest {

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

    private ClienteEntity alfa;
    private ClienteEntity beta;
    private FuncionarioEntity atendente;
    private ProdutoEntity caneca;
    private PoliticaAlteracaoEntity politica;
    private PedidoEntity emDia;
    private PedidoEntity atrasado;

    @BeforeEach
    void preparar() {
        alfa = clientes.save(new ClienteEntity("Empresa Alfa", "83999990000", null, CanalContato.WHATSAPP));
        beta = clientes.save(new ClienteEntity("Beta Eventos", null, "beta@email.com", CanalContato.INSTAGRAM));
        atendente = funcionarios.save(new FuncionarioEntity("Ana", SetorFuncionario.ATENDIMENTO));
        caneca = produtos.save(new ProdutoEntity("Caneca", null, new BigDecimal("8.00"), new BigDecimal("0.10")));
        politica = politicas.save(new PoliticaAlteracaoEntity("Padrão", 48, 70, 72));

        LocalDateTime agora = LocalDateTime.now();
        emDia = pedido("PED-000001", alfa, agora, agora.plusDays(10));
        // pedido criado há 10 dias com entrega prevista para ontem
        atrasado = pedido("PED-000002", beta, agora.minusDays(10), agora.minusDays(1));
        atrasado.aprovar(agora.minusDays(9));
    }

    private PedidoEntity pedido(String numero, ClienteEntity cliente, LocalDateTime criadoEm, LocalDateTime entrega) {
        PedidoEntity pedido = new PedidoEntity(numero, cliente, atendente, CanalContato.WHATSAPP, politica,
                entrega, criadoEm);
        pedido.adicionarItem(caneca, 100, null);
        return pedidos.save(pedido);
    }

    @Test
    void buscaPedidoPeloId() throws Exception {
        mvc.perform(get("/pedidos/" + emDia.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("PED-000001"))
                .andExpect(jsonPath("$.clienteNome").value("Empresa Alfa"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.saldoPendente").value(800.00));
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pedidos/9999")).andExpect(status().isNotFound());
    }

    @Test
    void listaTodosOrdenadosPelaDataDeEntrega() throws Exception {
        mvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numero").value("PED-000002"))
                .andExpect(jsonPath("$[1].numero").value("PED-000001"));
    }

    @Test
    void filtraPorStatus() throws Exception {
        mvc.perform(get("/pedidos").param("status", "APROVADO"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value("PED-000002"));
    }

    @Test
    void filtraPorCliente() throws Exception {
        mvc.perform(get("/pedidos").param("clienteId", alfa.getId().toString()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Empresa Alfa"));
    }

    @Test
    void listaSomenteAtrasados() throws Exception {
        mvc.perform(get("/pedidos").param("atrasados", "true"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value("PED-000002"))
                .andExpect(jsonPath("$[0].atrasado").value(true));
    }

    @Test
    void painelResumeAOperacao() throws Exception {
        mvc.perform(get("/pedidos/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atrasados").value(1))
                .andExpect(jsonPath("$.emProducao").value(0))
                .andExpect(jsonPath("$.aReceber").value(800.00))
                .andExpect(jsonPath("$.pedidosPorStatus.ORCAMENTO").value(1))
                .andExpect(jsonPath("$.pedidosPorStatus.APROVADO").value(1));
    }
}

package br.edu.uniesp.pedidocerto.internal.cadastros;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "pedidocerto.dados-iniciais=false")
@AutoConfigureMockMvc
@Transactional
class CadastrosApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cadastraEConsultaCliente() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Empresa Alfa","telefone":"83999990000","canalPreferencial":"WHATSAPP"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Empresa Alfa"))
                .andExpect(jsonPath("$.ativo").value(true));

        mvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].canalPreferencial").value("WHATSAPP"));
    }

    @Test
    void rejeitaClienteSemNome() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","telefone":"83999990000","canalPreferencial":"WHATSAPP"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").exists());
    }

    @Test
    void rejeitaClienteSemTelefoneNemEmail() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Sem contato","canalPreferencial":"WHATSAPP"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        mvc.perform(get("/clientes/9999")).andExpect(status().isNotFound());
    }

    @Test
    void inativaCliente() throws Exception {
        String corpo = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Beta","email":"beta@email.com","canalPreferencial":"INSTAGRAM"}"""))
                .andReturn().getResponse().getContentAsString();
        String id = corpo.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(post("/clientes/" + id + "/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void cadastraProdutoEReajustaPreco() throws Exception {
        String corpo = mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Caneca","precoBase":8.00,"horasProducaoPorUnidade":0.10}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = corpo.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(patch("/produtos/" + id + "/preco").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preco\":9.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBase").value(9.50));
    }

    @Test
    void rejeitaProdutoComPrecoInvalido() throws Exception {
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Caneca","precoBase":-1,"horasProducaoPorUnidade":0.10}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastraFuncionario() throws Exception {
        mvc.perform(post("/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","setor":"ATENDIMENTO"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setor").value("ATENDIMENTO"));
    }
}

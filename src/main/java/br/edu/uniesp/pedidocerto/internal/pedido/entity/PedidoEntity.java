package br.edu.uniesp.pedidocerto.internal.pedido.entity;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.material.entity.MaterialEntity;
import br.edu.uniesp.pedidocerto.internal.material.entity.NecessidadeMaterialEntity;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.FormaPagamento;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.PagamentoEntity;
import br.edu.uniesp.pedidocerto.internal.pagamento.entity.TipoPagamento;
import br.edu.uniesp.pedidocerto.internal.produto.entity.ProdutoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.EtapaProducaoEntity;
import br.edu.uniesp.pedidocerto.internal.producao.entity.ExecucaoEtapaEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raiz do agregado: concentra as regras de itens, pagamentos, alterações, etapas e materiais.
 * Não há setters; cada mudança de estado é um método que protege uma regra do negócio.
 */
@Entity
@Getter
@Table(name = "pedidos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendente_id", nullable = false)
    private FuncionarioEntity atendente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "politica_alteracao_id", nullable = false)
    private PoliticaAlteracaoEntity politicaAlteracao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.ORCAMENTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false, length = 20)
    private CanalContato canalOrigem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    @Column(name = "data_entrega_prevista", nullable = false)
    private LocalDateTime dataEntregaPrevista;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /** Começa em 1 e cresce a cada alteração incorporada; a produção trabalha sempre na versão vigente. */
    @Column(nullable = false)
    private int versao = 1;

    @Column(name = "motivo_cancelamento", length = 255)
    private String motivoCancelamento;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoEntity> itens = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoEntity> pagamentos = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlteracaoPedidoEntity> alteracoes = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecucaoEtapaEntity> execucoesEtapa = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NecessidadeMaterialEntity> necessidadesMaterial = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EntregaEntity entrega;

    public PedidoEntity(String numero, ClienteEntity cliente, FuncionarioEntity atendente, CanalContato canalOrigem,
                        PoliticaAlteracaoEntity politicaAlteracao, LocalDateTime dataEntregaPrevista,
                        LocalDateTime agora) {
        if (numero == null || numero.isBlank()) {
            throw new RegraNegocioException("O número do pedido é obrigatório.");
        }
        if (cliente == null || !cliente.isAtivo()) {
            throw new RegraNegocioException("Não é permitido abrir pedido para um cliente inativo.");
        }
        if (atendente == null || !atendente.isAtivo()) {
            throw new RegraNegocioException("O pedido deve ser aberto por um funcionário ativo.");
        }
        if (politicaAlteracao == null || !politicaAlteracao.isAtiva()) {
            throw new RegraNegocioException("É necessário uma política de alteração ativa.");
        }
        if (canalOrigem == null) {
            throw new RegraNegocioException("O canal de origem do pedido é obrigatório.");
        }
        if (dataEntregaPrevista == null || !dataEntregaPrevista.isAfter(agora)) {
            throw new RegraNegocioException("A data de entrega deve ser futura.");
        }
        this.numero = numero;
        this.cliente = cliente;
        this.atendente = atendente;
        this.canalOrigem = canalOrigem;
        this.politicaAlteracao = politicaAlteracao;
        this.dataEntregaPrevista = dataEntregaPrevista;
        this.criadoEm = agora;
        this.status = StatusPedido.ORCAMENTO;
        this.valorTotal = BigDecimal.ZERO;
        this.versao = 1;
    }

    // ---- consultas (coleções somente leitura) ----

    public List<ItemPedidoEntity> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<PagamentoEntity> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    public List<AlteracaoPedidoEntity> getAlteracoes() {
        return Collections.unmodifiableList(alteracoes);
    }

    public List<ExecucaoEtapaEntity> getExecucoesEtapa() {
        return Collections.unmodifiableList(execucoesEtapa);
    }

    public List<NecessidadeMaterialEntity> getNecessidadesMaterial() {
        return Collections.unmodifiableList(necessidadesMaterial);
    }

    // ---- orçamento ----

    public ItemPedidoEntity adicionarItem(ProdutoEntity produto, int quantidade, String personalizacao) {
        exigirStatus("adicionar itens", StatusPedido.ORCAMENTO);
        if (produto == null) {
            throw new RegraNegocioException("O produto é obrigatório.");
        }
        ItemPedidoEntity item = new ItemPedidoEntity(this, produto, quantidade, personalizacao);
        itens.add(item);
        recalcularValorTotal();
        return item;
    }

    public void removerItem(ItemPedidoEntity item) {
        exigirStatus("remover itens", StatusPedido.ORCAMENTO);
        if (!itens.remove(item)) {
            throw new RegraNegocioException("O item não pertence a este pedido.");
        }
        recalcularValorTotal();
    }

    public void alterarQuantidadeDoItem(ItemPedidoEntity item, int novaQuantidade) {
        exigirStatus("alterar itens", StatusPedido.ORCAMENTO);
        exigirItem(item).alterarQuantidade(novaQuantidade);
        recalcularValorTotal();
    }

    public void alterarPersonalizacaoDoItem(ItemPedidoEntity item, String novaPersonalizacao) {
        exigirStatus("alterar itens", StatusPedido.ORCAMENTO);
        exigirItem(item).alterarPersonalizacao(novaPersonalizacao);
    }

    public void reagendarEntrega(LocalDateTime novaData, LocalDateTime agora) {
        exigirStatus("reagendar a entrega", StatusPedido.ORCAMENTO, StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO);
        if (novaData == null || !novaData.isAfter(agora)) {
            throw new RegraNegocioException("A nova data de entrega deve ser futura.");
        }
        this.dataEntregaPrevista = novaData;
    }

    // ---- ciclo de vida ----

    public void aprovar(LocalDateTime agora) {
        exigirStatus("aprovar o pedido", StatusPedido.ORCAMENTO);
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Não é possível aprovar um pedido sem itens.");
        }
        this.status = StatusPedido.APROVADO;
        this.aprovadoEm = agora;
        this.entrega = new EntregaEntity(this);
    }

    public PagamentoEntity registrarPagamento(TipoPagamento tipo, FormaPagamento forma, BigDecimal valor,
                                              LocalDateTime pagoEm, FuncionarioEntity registradoPor) {
        exigirStatus("registrar pagamento", StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO, StatusPedido.PRONTO);
        if (valor == null || valor.compareTo(saldoPendente()) > 0) {
            throw new RegraNegocioException("O pagamento excede o saldo pendente de R$ " + saldoPendente() + ".");
        }
        if (tipo == TipoPagamento.SALDO && valor.compareTo(saldoPendente()) != 0) {
            throw new RegraNegocioException("O pagamento do saldo deve quitar o valor pendente.");
        }
        PagamentoEntity pagamento = new PagamentoEntity(this, tipo, forma, valor, pagoEm, registradoPor);
        pagamentos.add(pagamento);
        return pagamento;
    }

    public ExecucaoEtapaEntity planejarEtapa(EtapaProducaoEntity etapa) {
        exigirStatus("planejar etapas", StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO);
        if (etapa == null) {
            throw new RegraNegocioException("A etapa é obrigatória.");
        }
        if (execucoesEtapa.stream().anyMatch(e -> e.getEtapa().equals(etapa))) {
            throw new RegraNegocioException("A etapa " + etapa.getNome() + " já foi planejada para este pedido.");
        }
        ExecucaoEtapaEntity execucao = new ExecucaoEtapaEntity(this, etapa);
        execucoesEtapa.add(execucao);
        return execucao;
    }

    public NecessidadeMaterialEntity definirNecessidadeDeMaterial(MaterialEntity material, BigDecimal quantidade) {
        exigirStatus("definir materiais", StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO);
        for (NecessidadeMaterialEntity existente : necessidadesMaterial) {
            if (existente.getMaterial().equals(material)) {
                existente.ajustarQuantidadeNecessaria(quantidade);
                return existente;
            }
        }
        NecessidadeMaterialEntity necessidade = new NecessidadeMaterialEntity(this, material, quantidade);
        necessidadesMaterial.add(necessidade);
        return necessidade;
    }

    public void iniciarProducao() {
        exigirStatus("iniciar a produção", StatusPedido.APROVADO);
        boolean sinalRecebido = pagamentos.stream().anyMatch(p -> p.getTipo() == TipoPagamento.SINAL);
        if (!sinalRecebido) {
            throw new RegraNegocioException("A produção só começa após o registro do sinal.");
        }
        exigirAlteracoesLidasPelaProducao();
        this.status = StatusPedido.EM_PRODUCAO;
    }

    public void marcarComoPronto() {
        exigirStatus("marcar como pronto", StatusPedido.EM_PRODUCAO);
        exigirAlteracoesLidasPelaProducao();
        if (!execucoesEtapa.stream().allMatch(ExecucaoEtapaEntity::estaConcluida)) {
            throw new RegraNegocioException("Há etapas de produção ainda não concluídas.");
        }
        this.status = StatusPedido.PRONTO;
    }

    public void confirmarEntrega(FuncionarioEntity responsavel, LocalDateTime quando) {
        exigirStatus("entregar o pedido", StatusPedido.PRONTO);
        entrega.registrar(responsavel, saldoPendente(), quando);
        this.status = StatusPedido.ENTREGUE;
    }

    public void cancelar(String motivo) {
        if (status == StatusPedido.ENTREGUE || status == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Pedidos entregues ou já cancelados não podem ser cancelados.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo do cancelamento.");
        }
        this.status = StatusPedido.CANCELADO;
        this.motivoCancelamento = motivo;
    }

    // ---- alterações pedidas pelo cliente ----

    public AlteracaoPedidoEntity solicitarAlteracao(String descricao, FuncionarioEntity solicitadaPor,
                                                    LocalDateTime agora) {
        exigirStatus("solicitar alteração", StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO);
        ZonaAlteracao zona = politicaAlteracao.classificar(agora, aprovadoEm, dataEntregaPrevista,
                percentualProduzido(), existeEtapaIrreversivelConcluida());
        AlteracaoPedidoEntity alteracao = new AlteracaoPedidoEntity(this, descricao, solicitadaPor, agora, zona);
        alteracoes.add(alteracao);
        if (alteracao.getStatus() == StatusAlteracao.APROVADA_PELO_CLIENTE) {
            incorporar(alteracao);
        }
        return alteracao;
    }

    public void aprovarAlteracao(AlteracaoPedidoEntity alteracao, LocalDateTime quando) {
        exigirAlteracao(alteracao).aprovarPeloCliente(quando);
        incorporar(alteracao);
    }

    public void confirmarLeituraDaAlteracao(AlteracaoPedidoEntity alteracao, FuncionarioEntity funcionario,
                                            LocalDateTime quando) {
        exigirAlteracao(alteracao).confirmarLeituraDaProducao(funcionario, quando);
    }

    // ---- consultas de domínio ----

    public BigDecimal valorPago() {
        return pagamentos.stream().map(PagamentoEntity::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldoPendente() {
        return valorTotal.subtract(valorPago());
    }

    public boolean estaQuitado() {
        return saldoPendente().signum() <= 0;
    }

    public boolean estaAtrasado(LocalDateTime agora) {
        boolean emAberto = status != StatusPedido.ENTREGUE && status != StatusPedido.CANCELADO;
        return emAberto && agora.isAfter(dataEntregaPrevista);
    }

    public int percentualProduzido() {
        if (execucoesEtapa.isEmpty()) {
            return 0;
        }
        int soma = execucoesEtapa.stream().mapToInt(ExecucaoEtapaEntity::getPercentualConcluido).sum();
        return soma / execucoesEtapa.size();
    }

    public boolean existeEtapaIrreversivelConcluida() {
        return execucoesEtapa.stream().anyMatch(e -> e.estaConcluida() && e.getEtapa().isIrreversivel());
    }

    public List<NecessidadeMaterialEntity> materiaisAComprar() {
        return necessidadesMaterial.stream()
                .filter(n -> n.quantidadeAComprar().signum() > 0)
                .toList();
    }

    // ---- regras internas ----

    private void incorporar(AlteracaoPedidoEntity alteracao) {
        this.valorTotal = this.valorTotal.add(alteracao.getCustoAdicional());
        this.dataEntregaPrevista = this.dataEntregaPrevista.plusDays(alteracao.getDiasAdicionais());
        this.versao++;
    }

    private void recalcularValorTotal() {
        this.valorTotal = itens.stream().map(ItemPedidoEntity::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void exigirAlteracoesLidasPelaProducao() {
        if (alteracoes.stream().anyMatch(AlteracaoPedidoEntity::aguardaLeituraDaProducao)) {
            throw new RegraNegocioException("A produção precisa confirmar a leitura das alterações antes de avançar.");
        }
    }

    private ItemPedidoEntity exigirItem(ItemPedidoEntity item) {
        if (item == null || !itens.contains(item)) {
            throw new RegraNegocioException("O item não pertence a este pedido.");
        }
        return item;
    }

    private AlteracaoPedidoEntity exigirAlteracao(AlteracaoPedidoEntity alteracao) {
        if (alteracao == null || !alteracoes.contains(alteracao)) {
            throw new RegraNegocioException("A alteração não pertence a este pedido.");
        }
        return alteracao;
    }

    private void exigirStatus(String acao, StatusPedido... permitidos) {
        for (StatusPedido permitido : permitidos) {
            if (status == permitido) {
                return;
            }
        }
        throw new RegraNegocioException("Não é possível " + acao + " com o pedido em " + status + ".");
    }
}

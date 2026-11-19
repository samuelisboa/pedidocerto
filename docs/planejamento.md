# Planejamento da Etapa 3: primeiras funcionalidades

## 1. Planejamento em equipe

### Quais endpoints são mais relevantes para a primeira versão?

Os que atacam os erros da semana de pico contada pelo proprietário:

| Problema do cliente | Endpoint que o resolve |
|---|---|
| Informação espalhada em celulares e cadernos | `POST /pedidos` (ficha única do pedido) |
| Dono precisa perguntar o andamento aos funcionários | `GET /pedidos`, `GET /pedidos/{id}`, `GET /pedidos/painel` |
| Produto entregue sem conferir o saldo | `POST /pedidos/{id}/pagamentos`, `POST /pedidos/{id}/entrega` |
| Alteração que não chega à produção | `POST /pedidos/{id}/alteracoes` e confirmação de leitura |

### Prioridade e dependências

```
cadastro-clientes-produtos ─┐
                            ├─► cadastro-encomendas ─┬─► consulta-pedidos
                            │                        ├─► controle-pagamentos ─► (entrega)
                            │                        └─► alteracoes-producao
```

1. **Base (já na `main`):** repositórios, tratamento de erros HTTP, `PedidoResponse` compartilhado e dados iniciais
   (política de alteração e etapas de produção).
2. **Cadastros** (clientes, produtos, funcionários) e **cadastro de encomendas** são a fundação: todo o resto precisa
   de um pedido existente.
3. **Consulta, pagamentos/entrega e alterações/produção** dependem do pedido, mas **não dependem entre si**, então podem
   ser desenvolvidas em paralelo sem conflito: cada uma tem seus próprios `service`, `controller` e `dto`.

Nos testes, cada branch cria os dados de que precisa direto pelos repositórios, por isso nenhuma depende de outra
para ser validada.

## 2. Divisão de responsabilidades

| Integrante | Branch | Endpoints |
|---|---|---|
| Lucas Lucena | `feature/cadastro-clientes-produtos` | `/clientes`, `/produtos`, `/funcionarios` |
| Samuel Rodrigues Marques | `feature/cadastro-encomendas` | `POST /pedidos`, `/pedidos/{id}/itens`, `/aprovar`, `/cancelar` |
| Leticia Sette | `feature/consulta-pedidos` | `GET /pedidos`, `GET /pedidos/{id}`, `GET /pedidos/painel` |
| Davi Sprakel | `feature/controle-pagamentos` | `/pedidos/{id}/pagamentos`, `/pedidos/{id}/entrega` |
| Flavio Roberto | `feature/alteracoes-producao` | `/pedidos/{id}/alteracoes`, `/pedidos/{id}/producao/*` |

Cada integrante cria só os arquivos do próprio pacote de serviço (`PedidoCadastroService`, `PedidoConsultaService`,
`PagamentoService`, `AlteracaoService`...), o que evita conflitos de merge.

## 3. Fluxo com Git e GitHub

```bash
git checkout main && git pull
git checkout -b feature/nome-da-funcionalidade
# implementar e testar: ./mvnw test
git add . && git commit -m "feat: descrição clara do que foi feito"
git push -u origin feature/nome-da-funcionalidade
# abrir o Pull Request para a main no GitHub
```

Ordem sugerida de merge dos Pull Requests: `cadastro-clientes-produtos` e `cadastro-encomendas` primeiro; depois as
três restantes, em qualquer ordem.

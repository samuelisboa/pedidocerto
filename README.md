# PedidoCerto

Sistema de gestão de encomendas personalizadas para uma pequena empresa que recebe pedidos por WhatsApp, Instagram e balcão.
Projeto em equipe da disciplina de desenvolvimento de software (Uniesp).

> **Integrantes:** Samuel Rodrigues Marques · Lucas Lucena · Leticia Sette · Davi Sprakel · Flavio Roberto

## Problema

A empresa perde dinheiro com quatro falhas que se repetem: alteração de pedido que não chega à produção, prazo prometido sem
enxergar a capacidade, entrega sem conferir o saldo e material que falta (ou sobra). A causa comum é que a informação fica
espalhada em celulares, cadernos e planilhas.

O PedidoCerto cria uma **ficha única por pedido** e coloca as regras do negócio dentro do domínio:

| Regra | Onde está no código |
|---|---|
| A produção só começa com o **sinal** registrado | `PedidoEntity.iniciarProducao()` |
| **Entrega bloqueada** com saldo pendente | `EntregaEntity.registrar()` / `PedidoEntity.confirmarEntrega()` |
| Alteração classificada em **verde, amarela ou vermelha** (quanto já foi produzido, ponto sem volta, proximidade da entrega) | `PoliticaAlteracaoEntity.classificar()` |
| A etapa só avança depois que a **produção confirma a leitura** da alteração | `PedidoEntity.marcarComoPronto()` e `AlteracaoPedidoEntity.confirmarLeituraDaProducao()` |
| Alteração aprovada atualiza **valor, prazo e versão** do pedido | `PedidoEntity.aprovarAlteracao()` |
| Só promete data com **capacidade** disponível | `CapacidadeDiaEntity.reservar()` |
| Material que falta aparece na **lista de compras** | `PedidoEntity.materiaisAComprar()` |
| Contato fora do horário fica registrado com hora e pendente de tratamento | `ContatoEntity` |

## Etapa 2: modelagem do domínio

A Etapa 2 entregou **o domínio**: entidades JPA, atributos, relacionamentos e métodos modificadores de estado.

- **Entidades (15):** `Cliente`, `Funcionario`, `Produto`, `Material`, `NecessidadeMaterial`, `CapacidadeDia`, `EtapaProducao`,
  `ExecucaoEtapa`, `Pedido`, `ItemPedido`, `AlteracaoPedido`, `Entrega`, `PoliticaAlteracao`, `Pagamento`, `Contato`,
  mais 9 enumerações.
- **Relacionamentos JPA:** `@OneToOne` (Pedido ↔ Entrega), `@OneToMany`/`@ManyToOne` (Pedido e seus itens, pagamentos, alterações,
  etapas e materiais) e `@ManyToMany` (Produto ↔ Material).
- **Encapsulamento:** nenhuma entidade tem setters. O estado muda por métodos com nome do domínio
  (`aprovar`, `registrarPagamento`, `solicitarAlteracao`, `baixar`, `reservar`…), que validam a regra e lançam
  `RegraNegocioException` quando ela é violada. Os construtores validam o estado inicial, o construtor sem argumentos é
  `protected` (exigência do JPA) e as coleções são expostas como listas somente leitura.
- **Diagrama de classes UML:** [`docs/diagrama-classes.md`](docs/diagrama-classes.md) (desenhado pelo GitHub) e
  [`docs/diagrama-classes.puml`](docs/diagrama-classes.puml) (PlantUML). Mostra atributos, métodos modificadores,
  relacionamentos e multiplicidades.

## Arquitetura escolhida

**Arquitetura em camadas (Layered Architecture), organizada como monólito modular por funcionalidade** (package by feature).
É a mesma abordagem do projeto anterior da equipe (`assistencia-uniesp`), com Spring Boot.

```
Controller  →  Service  →  Repository  →  Banco de dados
 (HTTP/DTO)   (casos de uso)  (Spring Data JPA)
                     ↘ Entity (domínio: regras do negócio)
```

### Justificativa

| Critério | Por que camadas |
|---|---|
| **Necessidade do negócio** | O cliente quer algo simples, de baixo custo e sem sistema complicado. Um único projeto implantável (monólito) atende uma empresa de 8 pessoas sem a complexidade operacional de microsserviços. |
| **Complexidade do sistema** | As regras são ricas (alteração em zonas, trava de entrega, capacidade), mas moram nas entidades. Não há integrações externas pesadas nem múltiplos canais de entrada que justifiquem portas e adaptadores (Hexagonal) ou a cerimônia da Clean Architecture. |
| **Características da equipe** | Equipe de estudantes que já usou Spring Boot em camadas. Reaproveitar o padrão reduz a curva de aprendizado e deixa as responsabilidades fáceis de dividir entre os integrantes. |
| **Evolução** | Cada funcionalidade é um pacote independente (`pedido`, `pagamento`, `producao`…). Se uma delas crescer, pode ser extraída depois sem reescrever as demais. |

**Alternativas descartadas:** *Hexagonal* e *Clean Architecture* isolam melhor o domínio, mas exigem interfaces e mapeamentos
extras que não se justificam hoje; *MVC puro* mistura regra de negócio com a camada web; *microsserviços* seriam excessivos
e caros para o porte da empresa.

**Regras de dependência:** `controller` só chama `service`; `service` usa `repository` e as entidades; as entidades não
conhecem as camadas acima delas; DTOs não vazam para dentro do domínio. A regra de negócio fica na entidade (modelo de
domínio rico), e o service só orquestra o caso de uso e a transação.

### Organização do código

```
src/main/java/br/edu/uniesp/pedidocerto
├── PedidoCertoApplication.java
├── shared/exception/          RegraNegocioException
└── internal/
    ├── cliente/entity/        ClienteEntity, CanalContato
    ├── funcionario/entity/    FuncionarioEntity, SetorFuncionario
    ├── produto/entity/        ProdutoEntity
    ├── material/entity/       MaterialEntity, NecessidadeMaterialEntity
    ├── agenda/entity/         CapacidadeDiaEntity
    ├── producao/entity/       EtapaProducaoEntity, ExecucaoEtapaEntity, StatusEtapa
    ├── pedido/entity/         PedidoEntity, ItemPedidoEntity, AlteracaoPedidoEntity, EntregaEntity,
    │                          PoliticaAlteracaoEntity e enumerações
    ├── pagamento/entity/      PagamentoEntity, TipoPagamento, FormaPagamento
    └── atendimento/entity/    ContatoEntity
```

Na Etapa 3, cada pacote de funcionalidade ganhou as demais camadas ao lado de `entity`:
`repository/`, `service/`, `controller/` e `dto/` (por exemplo `internal/pedido/service/PedidoCadastroService.java`).

## Como executar

Requisitos: **Java 21**. O Maven já vem no projeto (`mvnw`).

```bash
# compilar
./mvnw compile

# rodar os testes (regras do domínio + validação dos mapeamentos JPA em banco H2 em memória)
./mvnw test

# subir a aplicação (cria o esquema no H2 em memória a partir das entidades)
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

### Testes

- `PedidoEntityTest`: 12 cenários das regras do pedido (sinal obrigatório, entrega bloqueada, alteração verde/amarela/vermelha,
  confirmação de leitura pela produção, material faltante, cliente inativo).
- `MapeamentoJpaTest`: persiste e relê o agregado completo no H2, o que valida todos os mapeamentos e o esquema gerado.

## Tecnologias

Java 21 · Spring Boot 4.1.1 · Spring Data JPA (Hibernate) · H2 (desenvolvimento) · Lombok · JUnit 5 · Maven

## Etapa 3: primeiras funcionalidades (API REST)

O planejamento, as dependências e a divisão entre os integrantes estão em [`docs/planejamento.md`](docs/planejamento.md).
Cada funcionalidade foi desenvolvida em uma branch própria e integrada por Pull Request, seguindo a arquitetura em camadas
(`controller → service → repository → entity`, com DTOs nas bordas).

| Integrante | Branch | Endpoints |
|---|---|---|
| Lucas Lucena | `feature/cadastro-clientes-produtos` | `/clientes`, `/produtos`, `/funcionarios` |
| Samuel Rodrigues Marques | `feature/cadastro-encomendas` | `POST /pedidos`, `/pedidos/{id}/itens`, `/aprovar`, `/cancelar` |
| Leticia Sette | `feature/consulta-pedidos` | `GET /pedidos`, `GET /pedidos/{id}`, `GET /pedidos/painel` |
| Davi Sprakel | `feature/controle-pagamentos` | `/pedidos/{id}/pagamentos`, `/pedidos/{id}/saldo`, `/pedidos/{id}/entrega` |
| Flavio Roberto | `feature/alteracoes-producao` | `/pedidos/{id}/alteracoes/*`, `/pedidos/{id}/producao/*`, `GET /etapas-producao` |

Erros de regra de negócio retornam **422**, recurso inexistente **404** e dados inválidos **400**, em um corpo padronizado
(`status`, `mensagem`, `detalhes`, `em`).

## Fora do escopo por enquanto

Autenticação, integração com WhatsApp, controle de estoque e agenda de capacidade pela API, e migrações Flyway
(o esquema ainda é gerado pelo Hibernate no H2 em memória).

# MarketHub

Plataforma de e-commerce baseada em microsservicos com comunicacao assincrona via Apache Kafka, cache distribuido com Redis e autenticacao stateless com JWT.

---

## Stack

**Backend**
- Java 17
- Spring Boot 3.x (Web, Security, Data JPA, Data Redis, Kafka)
- PostgreSQL 16 (banco isolado por servico)
- Apache Kafka + Zookeeper (mensageria)
- Redis 7 (cache e blacklist de tokens)
- Flyway (migracao de schema)

**Frontend**
- Angular 17 (standalone components, signals)
- Tailwind CSS

**Infraestrutura**
- Docker e Docker Compose
- NGINX (API gateway, proxy reverso, servidor de arquivos estaticos)

---

## Arquitetura

O sistema e dividido em quatro microsservicos independentes. Cada servico tem seu proprio banco de dados PostgreSQL, o que garante isolamento total de schema e permite que cada servico evolua, escale e seja deployado de forma independente.

```
                        +----------+
                        |  NGINX   |  :80
                        | gateway  |
                        +----+-----+
                             |
           +-----------------+-----------------+
           |                 |                 |
    /api/auth         /api/orders        /api/products
    /api/login        /api/items         /api/categories
           |          /api/payments             |
           v                 |                 v
   +---------------+         v        +------------------+
   | auth-service  |  +-------------+ | inventory-service|
   |    :8081      |  |order-service| |      :8083       |
   +-------+-------+  |   :8082     | +--------+---------+
           |          +------+------+          |
           |                 |                 |
       users_db          orders_db        inventory_db
```

Nenhum servico acessa o banco de outro. A comunicacao entre servicos e feita de duas formas: chamadas REST sincronas via Feign Client (quando o dado e necessario imediatamente, como buscar o preco de um produto antes de criar o pedido) e eventos assincronos via Kafka (quando a operacao pode ser processada de forma eventual).

---

## Comunicacao via Kafka

O Kafka e o backbone de comunicacao assincrona do sistema. A escolha por eventos em vez de REST para certas operacoes tem uma razao direta: desacoplamento de disponibilidade.

Quando um pedido e criado, o `order-service` nao pode ficar bloqueado esperando o `inventory-service` confirmar o estoque. O cliente ja recebeu `201 CREATED`. A reserva de estoque acontece nos bastidores. Se o `inventory-service` estiver indisponivel por alguns minutos, o Kafka segura a mensagem e ela e processada quando o servico voltar — sem perda de dados e sem falha visivel ao usuario.

### Fluxo de criacao de pedido

```
POST /api/orders
       |
       v
order-service
  1. chama inventory-service via Feign para buscar preco de cada item
  2. calcula totalAmount
  3. cria Payment com status PENDING
  4. salva Order com status PENDING
  5. publica evento [order-created] no Kafka
       |
       v
inventory-service consome [order-created]
  - para cada item do pedido:
      disponivel = stockQuantity - reservedQuantity
      se disponivel >= quantidade solicitada: continua
      se nao: publica [stock-failed] e para
  - se todos os itens passaram:
      incrementa reservedQuantity de cada produto
      registra StockMovement tipo RESERVED
      publica [stock-reserved]
       |
       +---> order-service consome [stock-reserved]
               muda Order.status para CONFIRMED

       +---> order-service consome [stock-failed]
               muda Order.status para CANCELLED
```

### Topics e responsabilidades

| Topic | Quem publica | Quem consome |
|---|---|---|
| order-created | order-service | inventory-service |
| stock-reserved | inventory-service | order-service |
| stock-failed | inventory-service | order-service |
| user-registered | auth-service | notification-service (futuro) |

### Por que reservedQuantity em vez de baixar o estoque direto

O `stockQuantity` so e decrementado quando o pedido e entregue. Antes disso, o produto fica com `reservedQuantity` incrementado. Isso resolve o problema de dois pedidos concorrentes tentando comprar o ultimo item em estoque:

```
Pedido A chega -> stockQuantity=1, reservedQuantity=0 -> disponivel=1 -> reserva
Pedido B chega -> stockQuantity=1, reservedQuantity=1 -> disponivel=0 -> falha
```

O `inventory-service` processa os eventos em sequencia dentro da mesma particao Kafka (usando o `orderId` como chave de particao), o que garante ordenacao por pedido sem necessidade de lock no banco.

---

## Autenticacao e Autorizacao

O `auth-service` e o unico servico responsavel por emitir tokens JWT. Os demais servicos (`order-service`, `inventory-service`) validam o token localmente usando a mesma chave secreta compartilhada via variavel de ambiente — sem nenhuma chamada ao `auth-service` a cada request.

O token carrega as claims necessarias para autorizacao: `sub` (email do usuario), `role` (CUSTOMER, SELLER, ADMIN, MODERATOR) e `jti` (ID unico do token para blacklist).

### Fluxo de logout

O JWT e stateless por natureza — uma vez emitido, e valido ate expirar. Para invalidar um token antes do vencimento (logout, suspensao de conta), o `jti` do token e salvo no Redis com TTL igual ao tempo restante de validade do token. O `SecurityFilter` checa a blacklist a cada request. Quando o TTL expira, a entrada some automaticamente do Redis sem necessidade de limpeza manual.

### Roles e permissoes

| Role | Permissoes |
|---|---|
| CUSTOMER | criar pedidos, ver proprios pedidos, navegar catalogo |
| SELLER | criar e editar proprios produtos, ver pedidos dos seus produtos |
| MODERATOR | aprovar/rejeitar produtos, moderar conteudo |
| ADMIN | acesso total |

---

## Redis

O Redis tem tres papeis distintos no sistema, cada um com semantica diferente:

**Blacklist de tokens (auth-service)**
Estrutura de chave simples com TTL. Chave: `blacklist:{jti}`, valor: `"revoked"`, TTL: tempo restante do token. Nao e cache — e estado.

**Cache de usuarios (auth-service)**
O `SecurityFilter` precisa carregar o usuario a cada request autenticado. Com `@Cacheable` no `AuthorizationService.loadUserByUsername`, o SELECT no banco so ocorre no primeiro acesso ou apos 30 minutos de inatividade.

**Cache de precos de produtos (order-service)**
O `order-service` chama o `inventory-service` via Feign para buscar o preco de cada item no momento da criacao do pedido. Com cache Redis de 5 minutos, pedidos com os mesmos produtos em sequencia nao geram chamadas HTTP repetidas ao `inventory-service`.

---

## NGINX

O NGINX e o unico ponto de entrada do sistema. Ele serve o Angular como arquivos estaticos e faz proxy das chamadas de API para os microsservicos na rede interna Docker. Os microsservicos nao ficam expostos diretamente ao host em producao.

```
localhost:80/             -> arquivos estaticos do Angular (dist/browser)
localhost:80/api/auth/*   -> auth-service:8081
localhost:80/api/orders/* -> order-service:8082
localhost:80/api/items/*  -> order-service:8082
localhost:80/api/payments/* -> order-service:8082
localhost:80/api/products/* -> inventory-service:8083
localhost:80/api/categories/* -> inventory-service:8083
```

---

## Redes Docker

O compose define duas redes para separar responsabilidades:

`private_network`: todos os bancos de dados, Redis, Kafka e Zookeeper. Inacessiveis de fora.

`public_network`: NGINX, microsservicos Spring Boot e frontend. O NGINX e o unico container nas duas redes — ele e a ponte entre o exterior e a rede privada.

---

## Como rodar

**Pre-requisitos**: Docker e Docker Compose instalados.

```bash
# clonar o repositorio
git clone <url>
cd ecomerce_platform

# build e subida de todos os containers
docker compose up -d --build

# aguardar inicializacao (~40 segundos para os servicos Spring Boot)
docker compose logs -f auth-service
```

O sistema estara disponivel em `http://localhost`.

Para desenvolvimento local do frontend sem Docker:

```bash
cd ecommerce-frontend
npm install
npm start   # sobe em localhost:4200 com proxy para os servicos locais
```

---

## Estrutura do repositorio

```
ecomerce_platform/
├── auth-service/          Spring Boot — autenticacao e emissao de JWT
├── order-service/         Spring Boot — pedidos, itens e pagamentos
├── inventory-service/     Spring Boot — produtos, categorias e estoque
├── ecommerce-frontend/    Angular 17 — SPA do marketplace
├── nginx/
│   └── default.conf       configuracao do gateway
└── docker-compose.yml
```

---

## Decisoes de arquitetura

**Banco por servico**: cada microsservico tem seu proprio PostgreSQL. Isso elimina acoplamento de schema — um servico nao pode fazer JOIN ou UPDATE no banco de outro. A comunicacao entre dados de servicos diferentes passa sempre pela API ou por eventos Kafka.

**Saga via Kafka**: o fluxo de criacao de pedido segue o padrao Saga coreografado. Nao ha um coordenador central — cada servico reage aos eventos que recebe e publica o proximo evento da cadeia. Isso mantem os servicos desacoplados e tolerantes a falhas parciais.

**JWT stateless com blacklist seletiva**: tokens de vida curta (2 horas) reduzem o impacto de tokens comprometidos. A blacklist via Redis cobre os casos onde invalidacao imediata e necessaria (logout, suspensao de conta) sem transformar a validacao em uma operacao com estado completo.

**Cache de preco no order-service**: o preco de um produto e buscado no momento da criacao do pedido, nao no momento da exibicao no catalogo. Isso garante que o `priceAtPurchase` registrado no `OrderItem` reflete o preco real no momento da compra, mesmo que o preco mude depois.
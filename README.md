# 🐾 VetFacility — Backend

<p>
  <img src="https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger"/>
</p>

### 👋 Sobre o projeto

Plataforma **SaaS multi-tenant** de gestão para pequenos prestadores de serviço (piloto: banho e tosa),
desenvolvida no âmbito do **Projeto de Extensão V** do curso de Tecnologia em Análise e Desenvolvimento de
Sistemas — a partir de um caso real: o MEI **Centro Estético Pet Smack**, de Francisco Reinaldo Campos
Bezerra (Messejana, Fortaleza/CE).

📦 Este repositório contém **apenas o backend** (API REST). O frontend é um projeto separado:
[`vetfacility-frontend`](../vetfacility-frontend).

Juntos, os dois projetos implementam a **execução preliminar (piloto)** descrita no relatório do Projeto V:
os três módulos apontados como prioritários pelo Sr. Francisco Reinaldo Campos Bezerra — **login/controle
de acesso**, **agendamento** e **controle de estoque**.

## 🧱 Stack técnica

| Camada | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot, API REST |
| Segurança | Spring Security + OAuth2 Resource Server (Bearer JWT, RS256) |
| Banco de dados | PostgreSQL, versionado com Flyway |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Infraestrutura | Docker + Docker Compose |

## 🏗️ Arquitetura (resumo)

- **Multi-tenant (SEG-03):** cada organização cadastrada é uma `empresa` (tenant). Toda tabela de negócio
  carrega `empresa_id`, e todo acesso é filtrado pela empresa do usuário autenticado
  (`SecurityUtils.currentEmpresaId()`), extraída da claim `empresaId` do JWT.
- **Autenticação (SEG-01/SEG-02/SEG-04):** login emite um JWT assinado com chave RSA (RS256). A validação
  do token nas demais rotas é feita pelo Spring Security OAuth2 Resource Server (Bearer Token, RFC 6750).
  Senhas são armazenadas com hash BCrypt.
- **Agendamento (AG-01 a AG-06):** calendário compartilhado entre os usuários da mesma empresa, com
  checagem de conflito de horário considerando a duração do serviço.
- **Estoque (EST-01 a EST-04):** cadastro de produtos com quantidade mínima; ao concluir um atendimento, o
  estoque dos produtos utilizados é baixado automaticamente.
- **Permissões e perfis de acesso (RBAC):** cada funcionalidade do sistema corresponde a uma permissão
  (ex.: `AGENDAMENTO_CRIAR`, `PRODUTO_GERENCIAR`). Cada empresa tem seus próprios perfis, que agrupam um
  conjunto de permissões e são atribuídos aos usuários. Dois perfis são criados automaticamente no
  cadastro da empresa — **Proprietário** (acesso completo) e **Auxiliar** (sem gerenciar usuários/perfis)
  — e não podem ser editados nem removidos; a empresa pode criar perfis próprios (ex.: "Tosador",
  "Recepção") escolhendo exatamente quais permissões cada um tem. A autorização é aplicada por
  `@PreAuthorize` em cada endpoint, a partir das permissões contidas no token JWT.
- **ROOT (administrador da plataforma):** o cadastro de novas empresas (`POST /api/auth/register`) é
  restrito a um usuário especial, sem empresa vinculada, criado automaticamente no primeiro start
  (`RootUserSeeder`) com as credenciais definidas em `ROOT_EMAIL`/`ROOT_PASSWORD` (veja `.env.example`).

O esquema completo do banco está em
[`src/main/resources/db/migration/V1__schema_inicial.sql`](src/main/resources/db/migration/V1__schema_inicial.sql)
e [`V2__perfis_e_permissoes.sql`](src/main/resources/db/migration/V2__perfis_e_permissoes.sql).

## 🚀 Como rodar (Docker)

Pré-requisitos: Docker e Docker Compose.

```bash
cp .env.example .env
docker compose up -d --build
```

Isso sobe dois serviços, na rede Docker `vetfacility_net` (compartilhada com o frontend):

- `db` — PostgreSQL 16 (porta `5432`)
- `backend` — API Spring Boot (porta `8080`), aplica as migrations do Flyway automaticamente ao subir

> O frontend é outro projeto/repositório e se conecta a este backend pela mesma rede Docker. Suba o
> backend primeiro — ele é quem cria a rede `vetfacility_net` — e depois o frontend
> (veja o README de [`vetfacility-frontend`](../vetfacility-frontend)).

Documentação interativa da API (Swagger UI): **http://localhost:8080/swagger-ui/index.html**

Após o primeiro start, já existe um usuário **ROOT** pronto para uso (login em `/api/auth/login`):
- E-mail: o valor de `ROOT_EMAIL` no `.env` (padrão: `root@vetfacility.local`)
- Senha: o valor de `ROOT_PASSWORD` no `.env` (padrão: `TrocarSenha123!` — **troque em qualquer ambiente real**)

É esse usuário quem deve chamar `POST /api/auth/register` (via Swagger UI, com o token dele) para cadastrar
cada nova empresa e o seu primeiro usuário (proprietário).

Para derrubar o ambiente:

```bash
docker compose down          # mantém os dados
docker compose down -v       # remove também o volume do banco
```

## 🛠️ Rodando fora do Docker (desenvolvimento)

Requer JDK 17 e um PostgreSQL local (ou o container `db` isolado via `docker compose up -d db`).

```bash
./mvnw spring-boot:run
```

As variáveis de conexão (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) têm valores padrão em
`application.yml` apontando para `localhost:5432`.

## 📡 Principais endpoints da API

| Método | Rota | Descrição | Permissão exigida |
|---|---|---|---|
| POST | `/api/auth/register` | Cadastra a empresa e o primeiro usuário (proprietário) | ROOT |
| POST | `/api/auth/login` | Autentica e retorna o JWT | — (público) |
| POST | `/api/auth/usuarios` | Cadastra um novo usuário na empresa autenticada | `USUARIO_GERENCIAR` |
| GET | `/api/usuarios` | Lista os usuários da empresa | `USUARIO_VISUALIZAR` |
| GET | `/api/permissoes` | Lista o catálogo de permissões do sistema | autenticado |
| GET/POST/PUT/DELETE | `/api/perfis` | CRUD dos perfis de acesso customizados da empresa | `PERFIL_GERENCIAR` |
| GET/POST | `/api/clientes` | Clientes | `CLIENTE_VISUALIZAR` / `CLIENTE_GERENCIAR` |
| GET/POST | `/api/animais` | Animais | `ANIMAL_VISUALIZAR` / `ANIMAL_GERENCIAR` |
| GET | `/api/servicos` | Serviços (seed automático: Banho, Banho e Tosa, Banho Terapêutico) | `SERVICO_VISUALIZAR` |
| GET/POST | `/api/produtos` | Produtos / estoque | `PRODUTO_VISUALIZAR` / `PRODUTO_GERENCIAR` |
| GET/POST | `/api/agendamentos` | Agendamentos (filtro `?de=YYYY-MM-DD&ate=YYYY-MM-DD`) | `AGENDAMENTO_VISUALIZAR` / `AGENDAMENTO_CRIAR` |
| POST | `/api/agendamentos/{id}/concluir` | Conclui o atendimento e dá baixa nos produtos utilizados | `AGENDAMENTO_CONCLUIR` |
| POST | `/api/agendamentos/{id}/cancelar` | Cancela o agendamento | `AGENDAMENTO_CANCELAR` |

Todas as rotas, exceto o login e a documentação Swagger, exigem o header `Authorization: Bearer <token>`.
A lista completa, com todas as permissões e testável diretamente, está no Swagger UI.

## 🔐 Nota sobre as chaves de segurança

O par de chaves RSA em `src/main/resources/certs/` é usado para assinar e validar os tokens JWT. Ele está
versionado apenas para viabilizar a execução imediata deste piloto acadêmico (`docker compose up` já
funciona "out of the box"). **Em produção real essas chaves nunca devem ser reaproveitadas** — desde a
versão atual, os caminhos das chaves são configuráveis via `JWT_PRIVATE_KEY_PATH`/`JWT_PUBLIC_KEY_PATH`,
para apontar a um par gerado separadamente. Veja [`DEPLOY.md`](DEPLOY.md) para o passo a passo completo de
deploy em VPS via Docker, incluindo a geração das chaves de produção.

## 🎯 Escopo e limitações do piloto

Conforme descrito no relatório do Projeto de Extensão V, este piloto cobre os três módulos prioritários
(login, agendamento, estoque) — e, no decorrer da implementação, antecipou também o isolamento completo
de dados entre empresas (multi-tenant, cartão SEG-03), incluindo uma tela de administração da plataforma
(usuário ROOT) para cadastrar novas empresas e permissões. Ficam para as próximas etapas: módulo
financeiro e personalização visual por conta (upload de logotipo).

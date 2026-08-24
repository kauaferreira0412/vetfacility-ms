# Deploy na VPS (Docker)

Passo a passo pra subir o VetFacility (backend + frontend + Postgres) na **mesma VPS que já
roda o discord-clone**, reaproveitando o Caddy que já está lá em vez de subir um novo (evita
brigar pelas portas 80/443).

Arquitetura: dois repositórios separados (`vetfecility` = backend, `vetfacility-frontend` =
frontend), cada um com seu próprio `docker-compose.yml` (dev local) e `docker-compose.prod.yml`
(overlay de produção) — mesmo padrão dos dois projetos localmente, só que agora "ligados" ao
Caddy do discord-clone por uma rede Docker externa chamada `webproxy`.

## 0. Pré-requisito: atualizar o discord-clone na VPS

Esta sessão já editou dois arquivos do **discord-clone** pra ele passar a servir também o
VetFacility (novo site block no Caddy + rede `webproxy` compartilhada):
- `discord-clone/docker-compose.prod.yml` (service `gateway` ganhou a rede `webproxy`)
- `discord-clone/frontend/Caddyfile` (novo bloco `vetfacility.{$DOMAIN} { ... }`)

Faça o commit/push desses dois arquivos no repositório do discord-clone e, na VPS:

```bash
cd ~/discord-clone
git pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build gateway
```

Isso recria só o `gateway` (Caddy), sem mexer no resto do discord-clone (Postgres, LiveKit,
backend, bot de música continuam rodando do jeito que estavam).

## 1. Levar o código do VetFacility pra VPS

```bash
cd ~
git clone <url-do-repo-vetfecility> vetfecility
git clone <url-do-repo-vetfacility-frontend> vetfacility-frontend
```

## 2. Gerar as chaves JWT de produção

**Nunca reaproveite** o par de chaves que está versionado no repositório (`src/main/resources/certs/`)
— esse serve só para desenvolvimento local. Gere um par novo, exclusivo desta VPS:

```bash
cd ~/vetfecility
mkdir -p deploy/prod-secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out deploy/prod-secrets/app.key
openssl rsa -pubout -in deploy/prod-secrets/app.key -out deploy/prod-secrets/app.pub
```

(Essa pasta já está no `.gitignore` — as chaves ficam só na VPS, nunca vão pro Git.)

## 3. Configurar as variáveis de produção

```bash
cp .env.prod.example .env.prod
nano .env.prod
```

Preencha:
- `POSTGRES_PASSWORD` / `DB_PASSWORD`: a mesma senha forte nos dois (é a mesma senha do banco).
- `CORS_ALLOWED_ORIGINS`: o domínio do VetFacility nessa VPS — `https://vetfacility.SEU-DOMINIO`
  (mesmo `DOMAIN` do `.env.prod` do discord-clone, com o prefixo `vetfacility.` na frente e
  `https://` — ex.: se o discord-clone usa `187-127-37-101.sslip.io`, aqui fica
  `https://vetfacility.187-127-37-101.sslip.io`).
- `ROOT_EMAIL` / `ROOT_PASSWORD`: **troque os dois** — os valores do README (`root@vetfacility.local`
  / `TrocarSenha123!`) são públicos, usados só no piloto local.

## 4. Subir o backend

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Isso cria a rede `vetfacility_net` (usada pelo frontend a seguir) e sobe `db` + `backend`.
Acompanhe os logs até o Flyway terminar as migrations e o Spring Boot subir:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f backend
```

## 5. Subir o frontend

```bash
cd ~/vetfacility-frontend
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Esse `frontend` entra em duas redes: `vetfacility_net` (pra falar com o backend, como já fazia
localmente) e `webproxy` (pra o Caddy do discord-clone conseguir alcançá-lo).

## 6. Testar

Abra `https://vetfacility.SEU-DOMINIO` (ex.: `https://vetfacility.187-127-37-101.sslip.io`).
O Caddy busca o certificado HTTPS sozinho na primeira requisição a esse domínio — pode levar
alguns segundos a mais na primeira vez. Faça login com o `ROOT_EMAIL`/`ROOT_PASSWORD` que você
definiu no passo 3.

## Atualizando depois de mudar código

```bash
cd ~/vetfecility && git pull && docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up -d --build
cd ~/vetfacility-frontend && git pull && docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

## Nota sobre trocar senha de usuário

Ainda não existe uma tela de "trocar senha" no sistema (nem autoatendimento, nem administrativa).
Se alguém esquecer a senha em produção, a única forma de trocar hoje é direto no banco:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml exec db psql -U vetfacility -d vetfacility
```

e atualizar a coluna `senha_hash` da tabela `usuario` com um hash BCrypt gerado à parte (não dá
pra simplesmente digitar a senha nova ali, precisa estar já criptografada).

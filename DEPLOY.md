# Deploy na VPS (Docker)

Passo a passo pra subir o VetFacility (frontend + backend + Postgres) numa **VPS dedicada só a
este projeto** (ex.: VPS da Hostinger), com HTTPS automático via Caddy.

Arquitetura: dois repositórios separados (`vetfacility` = backend, `vetfacility-frontend` =
frontend), cada um com seu `docker-compose.yml`. O backend "dono" da rede Docker
(`vetfacility_net`) também sobe um Caddy (via overlay `docker-compose.caddy.yml`), que expõe as
portas 80/443 e encaminha tudo pro `frontend` (cujo Nginx interno já repassa `/api/*` pro
`backend`) — não precisa configurar Nginx nem certificado à mão.

## 0. Provisionar a VPS

1. Crie a VPS na Hostinger com **Ubuntu 22.04 (ou mais novo)**.
2. Acesse por SSH: `ssh root@SEU-IP-DA-VPS`.
3. Instale o Docker (script oficial, já traz o plugin `docker compose`):
   ```bash
   curl -fsSL https://get.docker.com | sh
   ```
4. (Opcional, mas recomendado) crie um usuário próprio em vez de usar `root` direto:
   ```bash
   adduser deploy
   usermod -aG docker deploy
   su - deploy
   ```

## 1. Domínio (obrigatório para o HTTPS automático funcionar)

O Caddy precisa que um domínio resolva pro IP da VPS **antes** de subir os containers, senão a
emissão do certificado Let's Encrypt falha.

- **Se você já tem um domínio**: crie um registro `A` (ex.: `vetfacility.seudominio.com`)
  apontando pro IP da VPS.
- **Se ainda não tem domínio**: use o [sslip.io](https://sslip.io), que resolve
  `<IP-COM-TRAÇOS>.sslip.io` automaticamente pro próprio IP, sem precisar configurar nada. Ex.:
  VPS no IP `187.127.37.101` → domínio `187-127-37-101.sslip.io`.

Guarde esse domínio — ele vai no `.env.prod` (passo 4).

## 2. Levar o código pra VPS

```bash
cd ~
git clone https://github.com/kauaferreira0412/vetfacility-ms.git vetfacility
git clone https://github.com/kauaferreira0412/vetfacility-fe.git vetfacility-frontend
```

## 3. Gerar as chaves JWT de produção

**Nunca reaproveite** o par de chaves versionado no repositório (`src/main/resources/certs/`) —
ele é só para desenvolvimento local. Gere um par novo, exclusivo desta VPS:

```bash
cd ~/vetfacility
mkdir -p deploy/prod-secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out deploy/prod-secrets/app.key
openssl rsa -pubout -in deploy/prod-secrets/app.key -out deploy/prod-secrets/app.pub
```

(Essa pasta já está no `.gitignore` — as chaves ficam só na VPS, nunca vão pro Git.)

## 4. Configurar as variáveis de produção

```bash
cp .env.prod.example .env.prod
nano .env.prod
```

Preencha:
- `POSTGRES_PASSWORD` / `DB_PASSWORD`: a mesma senha forte nos dois (é a senha do banco).
- `DOMAIN`: o domínio do passo 1 (ex.: `vetfacility.seudominio.com` ou `187-127-37-101.sslip.io`).
- `CORS_ALLOWED_ORIGINS`: `https://` + o mesmo domínio (ex.:
  `https://vetfacility.seudominio.com`).
- `ROOT_EMAIL` / `ROOT_PASSWORD`: **troque os dois** — os valores do README
  (`root@vetfacility.local` / `TrocarSenha123!`) são públicos, usados só no piloto local.

## 5. Subir o backend + banco + Caddy

```bash
docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.caddy.yml up -d --build
```

Isso cria a rede `vetfacility_net` e sobe três serviços: `db` (Postgres), `backend` (Spring Boot)
e `caddy` (proxy HTTPS, nas portas 80/443 da VPS). Acompanhe os logs até o Flyway terminar as
migrations e o Spring Boot subir:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.caddy.yml logs -f backend
```

## 6. Subir o frontend

```bash
cd ~/vetfacility-frontend
docker compose up -d --build
```

O `frontend` entra na rede externa `vetfacility_net` (criada pelo backend no passo 5) e passa a
ser alcançado pelo Caddy internamente como `frontend:80` — não precisa expor porta nenhuma pro
host, o Caddy é a única porta de entrada externa.

## 7. Testar

Abra `https://SEU-DOMINIO` no navegador. O Caddy busca o certificado HTTPS sozinho na primeira
requisição — pode levar alguns segundos a mais na primeira vez. Faça login com o
`ROOT_EMAIL`/`ROOT_PASSWORD` definidos no passo 4, cadastre a empresa do Sr. Francisco (ou já
migre os dados, se estiver vindo do piloto local) e valide o fluxo completo (login, agendamento,
estoque, financeiro, personalização).

## Atualizando depois de mudar código

```bash
cd ~/vetfacility && git pull && docker compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.caddy.yml up -d --build
cd ~/vetfacility-frontend && git pull && docker compose up -d --build
```

## Backup do banco

Com o banco rodando em container, o backup é um `pg_dump` de dentro do container `db`:

```bash
cd ~/vetfacility
docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.caddy.yml exec db \
  pg_dump -U vetfacility vetfacility > backup-$(date +%F).sql
```

Vale automatizar isso num `cron` semanal, guardando os `.sql` fora da VPS (ex.: baixando pro seu
computador ou subindo pra um storage externo).

## Nota sobre trocar senha de usuário

Ainda não existe uma tela de "trocar senha" no sistema (nem autoatendimento, nem administrativa).
Se alguém esquecer a senha em produção, a única forma de trocar hoje é direto no banco:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.caddy.yml exec db \
  psql -U vetfacility -d vetfacility
```

e atualizar a coluna `senha_hash` da tabela `usuario` com um hash BCrypt gerado à parte (não dá
pra simplesmente digitar a senha nova ali, precisa estar já criptografada).

## Cenário alternativo: VPS compartilhada com outro projeto

Se um dia essa VPS passar a hospedar mais de um sistema (ex.: outro projeto seu que já tenha o
próprio Caddy/reverse proxy), não suba o `docker-compose.caddy.yml` deste projeto — nesse caso o
frontend deve entrar na rede `webproxy` externa do outro Caddy em vez de subir um Caddy próprio.
Avise se chegar nesse cenário que a gente adapta o compose.

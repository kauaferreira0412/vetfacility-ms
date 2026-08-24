CREATE TABLE empresa (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150)  NOT NULL,
    logotipo_url  VARCHAR(500),
    criado_em     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150)  NOT NULL,
    email         VARCHAR(180)  NOT NULL,
    senha_hash    VARCHAR(255)  NOT NULL,
    papel         VARCHAR(30)   NOT NULL DEFAULT 'PROPRIETARIO',
    ativo         BOOLEAN       NOT NULL DEFAULT TRUE,
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE cliente (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150)  NOT NULL,
    telefone      VARCHAR(30),
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE animal (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(120)  NOT NULL,
    especie       VARCHAR(60),
    porte         VARCHAR(20),  -- PEQUENO | MEDIO | GRANDE
    cliente_id    BIGINT        NOT NULL REFERENCES cliente (id) ON DELETE CASCADE,
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE
);

CREATE TABLE servico (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(100)  NOT NULL,
    duracao_min   INTEGER       NOT NULL DEFAULT 60,
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE
);

CREATE TABLE produto (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(120)  NOT NULL,
    quantidade_estoque  NUMERIC(10,2) NOT NULL DEFAULT 0,
    quantidade_minima   NUMERIC(10,2) NOT NULL DEFAULT 0,
    unidade             VARCHAR(20)   NOT NULL DEFAULT 'un',
    empresa_id          BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE
);

CREATE TABLE agendamento (
    id            BIGSERIAL PRIMARY KEY,
    animal_id     BIGINT        NOT NULL REFERENCES animal (id) ON DELETE CASCADE,
    servico_id    BIGINT        NOT NULL REFERENCES servico (id),
    usuario_id    BIGINT        NOT NULL REFERENCES usuario (id),
    data_hora     TIMESTAMP     NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'AGENDADO',
    observacao    VARCHAR(500),
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE agendamento_produto (
    agendamento_id  BIGINT        NOT NULL REFERENCES agendamento (id) ON DELETE CASCADE,
    produto_id      BIGINT        NOT NULL REFERENCES produto (id),
    quantidade      NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (agendamento_id, produto_id)
);

CREATE INDEX idx_usuario_empresa      ON usuario (empresa_id);
CREATE INDEX idx_cliente_empresa      ON cliente (empresa_id);
CREATE INDEX idx_animal_empresa       ON animal (empresa_id);
CREATE INDEX idx_produto_empresa      ON produto (empresa_id);
CREATE INDEX idx_agendamento_empresa  ON agendamento (empresa_id);
CREATE INDEX idx_agendamento_data     ON agendamento (empresa_id, data_hora);

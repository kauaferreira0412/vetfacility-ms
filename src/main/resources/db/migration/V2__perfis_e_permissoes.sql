-- Introduz o modelo de permissões e perfis de acesso customizáveis por empresa (RBAC), e o ROOT
-- (administrador da plataforma, sem empresa), único habilitado a cadastrar novas empresas.

CREATE TABLE permissao (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(60)  NOT NULL UNIQUE,
    descricao   VARCHAR(200) NOT NULL,
    modulo      VARCHAR(40)  NOT NULL
);

CREATE TABLE perfil (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    empresa_id  BIGINT       REFERENCES empresa (id) ON DELETE CASCADE,
    sistema     BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE perfil_permissao (
    perfil_id    BIGINT NOT NULL REFERENCES perfil (id) ON DELETE CASCADE,
    permissao_id BIGINT NOT NULL REFERENCES permissao (id) ON DELETE CASCADE,
    PRIMARY KEY (perfil_id, permissao_id)
);

CREATE INDEX idx_perfil_empresa ON perfil (empresa_id);

-- ROOT deixa de pertencer obrigatoriamente a uma empresa.
ALTER TABLE usuario ALTER COLUMN empresa_id DROP NOT NULL;

-- O papel fixo (PROPRIETARIO/AUXILIAR) é substituído pelo perfil (customizável por empresa).
ALTER TABLE usuario ADD COLUMN perfil_id BIGINT REFERENCES perfil (id);
ALTER TABLE usuario DROP COLUMN papel;

-- Catálogo de permissões do sistema, uma por funcionalidade. Não editável via API.
INSERT INTO permissao (codigo, descricao, modulo) VALUES
    ('EMPRESA_GERENCIAR',      'Cadastrar novas empresas na plataforma',                 'PLATAFORMA'),
    ('PERFIL_GERENCIAR',       'Criar, editar e remover perfis de acesso da empresa',    'ACESSO'),
    ('USUARIO_VISUALIZAR',     'Visualizar os usuários da empresa',                      'ACESSO'),
    ('USUARIO_GERENCIAR',      'Convidar e remover usuários da empresa',                 'ACESSO'),
    ('CLIENTE_VISUALIZAR',     'Visualizar clientes',                                    'CLIENTES'),
    ('CLIENTE_GERENCIAR',      'Cadastrar, editar e remover clientes',                   'CLIENTES'),
    ('ANIMAL_VISUALIZAR',      'Visualizar animais',                                     'CLIENTES'),
    ('ANIMAL_GERENCIAR',       'Cadastrar, editar e remover animais',                    'CLIENTES'),
    ('SERVICO_VISUALIZAR',     'Visualizar os tipos de serviço',                         'AGENDAMENTO'),
    ('AGENDAMENTO_VISUALIZAR', 'Visualizar o calendário de agendamentos',                'AGENDAMENTO'),
    ('AGENDAMENTO_CRIAR',      'Criar e reagendar agendamentos',                         'AGENDAMENTO'),
    ('AGENDAMENTO_CANCELAR',   'Cancelar agendamentos',                                  'AGENDAMENTO'),
    ('AGENDAMENTO_CONCLUIR',   'Concluir atendimentos e dar baixa em produtos',          'AGENDAMENTO'),
    ('PRODUTO_VISUALIZAR',     'Visualizar produtos e níveis de estoque',                'ESTOQUE'),
    ('PRODUTO_GERENCIAR',      'Cadastrar, editar e remover produtos',                   'ESTOQUE');

-- Perfil global (sem empresa) do administrador da plataforma (ROOT).
INSERT INTO perfil (id, nome, empresa_id, sistema) VALUES (1, 'Administrador da Plataforma', NULL, TRUE);
INSERT INTO perfil_permissao (perfil_id, permissao_id)
    SELECT 1, id FROM permissao WHERE codigo = 'EMPRESA_GERENCIAR';

-- Ajusta a sequência do perfil após a inserção manual do id=1 acima.
SELECT setval('perfil_id_seq', (SELECT MAX(id) FROM perfil));

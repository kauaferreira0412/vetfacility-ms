-- Módulo Financeiro (FIN-01 a FIN-05): registro de ganhos e gastos do negócio,
-- com vínculo opcional ao agendamento que originou o ganho.

CREATE TABLE movimentacao_financeira (
    id            BIGSERIAL     PRIMARY KEY,
    tipo          VARCHAR(10)   NOT NULL, -- GANHO | GASTO
    valor         NUMERIC(10,2) NOT NULL,
    data          DATE          NOT NULL,
    descricao     VARCHAR(200),
    agendamento_id BIGINT       REFERENCES agendamento (id) ON DELETE SET NULL,
    empresa_id    BIGINT        NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimentacao_empresa      ON movimentacao_financeira (empresa_id);
CREATE INDEX idx_movimentacao_empresa_data ON movimentacao_financeira (empresa_id, data);
CREATE INDEX idx_movimentacao_tipo         ON movimentacao_financeira (tipo);
CREATE INDEX idx_movimentacao_valor        ON movimentacao_financeira (valor);
CREATE INDEX idx_movimentacao_data         ON movimentacao_financeira (data);
CREATE INDEX idx_movimentacao_descricao    ON movimentacao_financeira (descricao);
CREATE INDEX idx_movimentacao_agendamento  ON movimentacao_financeira (agendamento_id);
CREATE INDEX idx_movimentacao_criado_em    ON movimentacao_financeira (criado_em);

-- Catálogo de permissões do novo módulo.
INSERT INTO permissao (codigo, descricao, modulo) VALUES
    ('FINANCEIRO_VISUALIZAR', 'Visualizar lançamentos e relatórios financeiros', 'FINANCEIRO'),
    ('FINANCEIRO_GERENCIAR',  'Lançar gastos e editar movimentações financeiras', 'FINANCEIRO');

-- Concede as novas permissões aos perfis "Proprietário" já existentes (sistema=true), para que
-- empresas cadastradas antes deste módulo ganhem acesso automaticamente, sem intervenção manual.
INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM perfil p
JOIN permissao perm ON perm.codigo IN ('FINANCEIRO_VISUALIZAR', 'FINANCEIRO_GERENCIAR')
WHERE p.nome = 'Proprietário' AND p.sistema = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );

-- O perfil "Auxiliar" ganha apenas a visualização, mantendo o lançamento de gastos e a
-- visão consolidada sob controle exclusivo do proprietário do negócio.
INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM perfil p
JOIN permissao perm ON perm.codigo = 'FINANCEIRO_VISUALIZAR'
WHERE p.nome = 'Auxiliar' AND p.sistema = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );

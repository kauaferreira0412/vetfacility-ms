-- Registra o instante em que o atendimento foi efetivamente iniciado (status EM_ATENDIMENTO), usado
-- pelo front-end para exibir um contador de tempo decorrido durante o atendimento.
ALTER TABLE agendamento ADD COLUMN iniciado_em TIMESTAMP;

-- Produtos que o executor sinaliza que pretende usar já no início do atendimento - só informativo
-- (exibido durante o "Em atendimento"), sem efeito no estoque. A baixa real continua acontecendo na
-- conclusão (tabela agendamento_produto), que pode reaproveitar esta lista como sugestão automática.
CREATE TABLE agendamento_produto_planejado (
    agendamento_id  BIGINT        NOT NULL REFERENCES agendamento (id) ON DELETE CASCADE,
    produto_id      BIGINT        NOT NULL,
    produto_nome    VARCHAR(120)  NOT NULL,
    quantidade      NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (agendamento_id, produto_id)
);

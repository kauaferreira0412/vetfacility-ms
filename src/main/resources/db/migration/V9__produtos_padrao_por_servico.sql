-- Vínculo produto-serviço (EST-02/EST-03): cada serviço pode ter uma lista padrão de produtos e
-- quantidades, usada para pré-preencher a seleção ao iniciar um atendimento desse serviço.

CREATE TABLE servico_produto (
    servico_id        BIGINT        NOT NULL REFERENCES servico (id) ON DELETE CASCADE,
    produto_id         BIGINT        NOT NULL REFERENCES produto (id) ON DELETE CASCADE,
    quantidade_padrao NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (servico_id, produto_id)
);

CREATE INDEX idx_servico_produto_produto ON servico_produto (produto_id);
CREATE INDEX idx_servico_produto_quantidade ON servico_produto (quantidade_padrao);

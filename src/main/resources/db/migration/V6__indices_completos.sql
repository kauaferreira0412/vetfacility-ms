-- Índices em todos os atributos de todas as tabelas, para acelerar filtros, ordenações e buscas em
-- qualquer coluna (não só empresa_id, que já era indexado desde a V1). Colunas que já têm um índice
-- implícito - chave primária (id) ou UNIQUE (usuario.email, permissao.codigo) - não são repetidas
-- aqui, e (empresa_id) de cada tabela também não, já que a V1/V2 já criaram esses índices.

-- empresa
CREATE INDEX idx_empresa_nome         ON empresa (nome);
CREATE INDEX idx_empresa_logotipo_url ON empresa (logotipo_url);
CREATE INDEX idx_empresa_criado_em    ON empresa (criado_em);

-- usuario
CREATE INDEX idx_usuario_nome        ON usuario (nome);
CREATE INDEX idx_usuario_senha_hash  ON usuario (senha_hash);
CREATE INDEX idx_usuario_ativo       ON usuario (ativo);
CREATE INDEX idx_usuario_perfil      ON usuario (perfil_id);
CREATE INDEX idx_usuario_criado_em   ON usuario (criado_em);

-- cliente
CREATE INDEX idx_cliente_nome        ON cliente (nome);
CREATE INDEX idx_cliente_telefone    ON cliente (telefone);
CREATE INDEX idx_cliente_criado_em   ON cliente (criado_em);
CREATE INDEX idx_cliente_email       ON cliente (email);
CREATE INDEX idx_cliente_cpf         ON cliente (cpf);
CREATE INDEX idx_cliente_endereco    ON cliente (endereco);
CREATE INDEX idx_cliente_cidade      ON cliente (cidade);
CREATE INDEX idx_cliente_cep         ON cliente (cep);
CREATE INDEX idx_cliente_observacoes ON cliente (observacoes);

-- animal
CREATE INDEX idx_animal_nome             ON animal (nome);
CREATE INDEX idx_animal_especie          ON animal (especie);
CREATE INDEX idx_animal_porte            ON animal (porte);
CREATE INDEX idx_animal_cliente          ON animal (cliente_id);
CREATE INDEX idx_animal_raca             ON animal (raca);
CREATE INDEX idx_animal_sexo             ON animal (sexo);
CREATE INDEX idx_animal_data_nascimento  ON animal (data_nascimento);
CREATE INDEX idx_animal_peso             ON animal (peso);
CREATE INDEX idx_animal_cor_pelagem      ON animal (cor_pelagem);
CREATE INDEX idx_animal_observacoes      ON animal (observacoes);

-- servico (nenhum índice próprio até aqui, nem empresa_id)
CREATE INDEX idx_servico_nome        ON servico (nome);
CREATE INDEX idx_servico_duracao_min ON servico (duracao_min);
CREATE INDEX idx_servico_empresa     ON servico (empresa_id);

-- produto
CREATE INDEX idx_produto_nome               ON produto (nome);
CREATE INDEX idx_produto_quantidade_estoque ON produto (quantidade_estoque);
CREATE INDEX idx_produto_quantidade_minima  ON produto (quantidade_minima);
CREATE INDEX idx_produto_unidade            ON produto (unidade);

-- agendamento
CREATE INDEX idx_agendamento_animal              ON agendamento (animal_id);
CREATE INDEX idx_agendamento_servico             ON agendamento (servico_id);
CREATE INDEX idx_agendamento_usuario             ON agendamento (usuario_id);
CREATE INDEX idx_agendamento_data_hora           ON agendamento (data_hora);
CREATE INDEX idx_agendamento_status              ON agendamento (status);
CREATE INDEX idx_agendamento_observacao          ON agendamento (observacao);
CREATE INDEX idx_agendamento_criado_em           ON agendamento (criado_em);
CREATE INDEX idx_agendamento_motivo_cancelamento ON agendamento (motivo_cancelamento);
CREATE INDEX idx_agendamento_iniciado_em         ON agendamento (iniciado_em);

-- agendamento_produto (chave composta já cobre agendamento_id como coluna mais à esquerda)
CREATE INDEX idx_agendamento_produto_produto     ON agendamento_produto (produto_id);
CREATE INDEX idx_agendamento_produto_quantidade  ON agendamento_produto (quantidade);

-- permissao
CREATE INDEX idx_permissao_descricao ON permissao (descricao);
CREATE INDEX idx_permissao_modulo    ON permissao (modulo);

-- perfil
CREATE INDEX idx_perfil_nome      ON perfil (nome);
CREATE INDEX idx_perfil_sistema   ON perfil (sistema);
CREATE INDEX idx_perfil_criado_em ON perfil (criado_em);

-- perfil_permissao (chave composta já cobre perfil_id como coluna mais à esquerda)
CREATE INDEX idx_perfil_permissao_permissao ON perfil_permissao (permissao_id);

-- agendamento_produto_planejado (chave composta já cobre agendamento_id como coluna mais à esquerda)
CREATE INDEX idx_agendamento_produto_planejado_produto      ON agendamento_produto_planejado (produto_id);
CREATE INDEX idx_agendamento_produto_planejado_produto_nome ON agendamento_produto_planejado (produto_nome);
CREATE INDEX idx_agendamento_produto_planejado_quantidade   ON agendamento_produto_planejado (quantidade);

-- Amplia os dados de cliente e animal para um cadastro completo (páginas dedicadas de cadastro).

ALTER TABLE cliente ADD COLUMN email        VARCHAR(180);
ALTER TABLE cliente ADD COLUMN cpf          VARCHAR(20);
ALTER TABLE cliente ADD COLUMN endereco     VARCHAR(200);
ALTER TABLE cliente ADD COLUMN cidade       VARCHAR(100);
ALTER TABLE cliente ADD COLUMN cep          VARCHAR(15);
ALTER TABLE cliente ADD COLUMN observacoes  VARCHAR(500);

ALTER TABLE animal ADD COLUMN raca             VARCHAR(80);
ALTER TABLE animal ADD COLUMN sexo             VARCHAR(10);
ALTER TABLE animal ADD COLUMN data_nascimento  DATE;
ALTER TABLE animal ADD COLUMN peso             NUMERIC(6,2);
ALTER TABLE animal ADD COLUMN cor_pelagem      VARCHAR(60);
ALTER TABLE animal ADD COLUMN observacoes      VARCHAR(500);

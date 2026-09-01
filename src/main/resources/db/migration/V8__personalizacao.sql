-- Personalização de marca (PER-01 a PER-03): nome do negócio e logotipo, configuráveis pelo
-- proprietário da conta e refletidos em toda a interface.

-- O logotipo passa a ser armazenado como data URI (base64) diretamente na coluna, então o
-- limite de 500 caracteres não comporta mais o conteúdo, e o índice antigo (pensado para uma URL
-- curta) deixa de fazer sentido: ninguém filtra empresas pelo conteúdo do logotipo.
DROP INDEX IF EXISTS idx_empresa_logotipo_url;
ALTER TABLE empresa ALTER COLUMN logotipo_url TYPE TEXT;

INSERT INTO permissao (codigo, descricao, modulo) VALUES
    ('EMPRESA_PERSONALIZAR', 'Editar o nome do negócio e o logotipo exibidos no sistema', 'PERSONALIZACAO');

-- Concede a nova permissão aos perfis "Proprietário" já existentes (sistema=true).
INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM perfil p
JOIN permissao perm ON perm.codigo = 'EMPRESA_PERSONALIZAR'
WHERE p.nome = 'Proprietário' AND p.sistema = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );

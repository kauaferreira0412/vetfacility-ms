-- Adiciona o registro da justificativa informada ao cancelar um agendamento.
ALTER TABLE agendamento ADD COLUMN motivo_cancelamento VARCHAR(300);

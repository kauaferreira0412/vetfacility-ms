package com.br.vetfacility.dto.financeiro;

import com.br.vetfacility.domain.MovimentacaoFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoFinanceiraResponse(
        Long id, String tipo, BigDecimal valor, LocalDate data, String descricao,
        Long agendamentoId, String servicoNome, String animalNome
) {
    public static MovimentacaoFinanceiraResponse from(MovimentacaoFinanceira m) {
        boolean temAgendamento = m.getAgendamento() != null;
        return new MovimentacaoFinanceiraResponse(
                m.getId(), m.getTipo().name(), m.getValor(), m.getData(), m.getDescricao(),
                temAgendamento ? m.getAgendamento().getId() : null,
                temAgendamento ? m.getAgendamento().getServico().getNome() : null,
                temAgendamento ? m.getAgendamento().getAnimal().getNome() : null
        );
    }
}

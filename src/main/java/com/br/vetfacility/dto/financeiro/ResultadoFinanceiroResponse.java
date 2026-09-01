package com.br.vetfacility.dto.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResultadoFinanceiroResponse(
        LocalDate de, LocalDate ate,
        BigDecimal totalGanhos, BigDecimal totalGastos, BigDecimal resultado,
        List<MovimentacaoFinanceiraResponse> movimentacoes
) {
}

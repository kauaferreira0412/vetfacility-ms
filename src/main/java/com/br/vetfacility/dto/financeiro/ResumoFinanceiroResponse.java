package com.br.vetfacility.dto.financeiro;

import java.math.BigDecimal;

public record ResumoFinanceiroResponse(
        BigDecimal resultadoHoje,
        BigDecimal resultadoMes
) {
}

package com.br.vetfacility.dto.financeiro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoFinanceiraRequest(
        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "A data é obrigatória") LocalDate data,
        @Size(max = 200, message = "A descrição pode ter no máximo 200 caracteres") String descricao
) {
}

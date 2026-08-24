package com.br.vetfacility.dto.agendamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ConcluirAgendamentoRequest(
        List<@Valid ProdutoConsumido> produtosConsumidos
) {
    public record ProdutoConsumido(
            @NotNull(message = "O produto é obrigatório") Long produtoId,
            @NotNull(message = "A quantidade é obrigatória")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero") BigDecimal quantidade
    ) {
    }
}

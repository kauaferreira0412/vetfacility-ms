package com.br.vetfacility.dto.servico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record AtualizarProdutosPadraoRequest(
        @NotNull(message = "A lista de produtos é obrigatória") List<@Valid ProdutoPadrao> produtos
) {
    public record ProdutoPadrao(
            @NotNull(message = "O produto é obrigatório") Long produtoId,
            @NotNull(message = "A quantidade padrão é obrigatória")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero") BigDecimal quantidadePadrao
    ) {
    }
}

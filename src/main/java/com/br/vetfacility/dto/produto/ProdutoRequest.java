package com.br.vetfacility.dto.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "O nome do produto é obrigatório") String nome,
        @NotNull(message = "A quantidade em estoque é obrigatória")
        @DecimalMin(value = "0", message = "A quantidade não pode ser negativa") BigDecimal quantidadeEstoque,
        @NotNull(message = "A quantidade mínima é obrigatória")
        @DecimalMin(value = "0", message = "A quantidade mínima não pode ser negativa") BigDecimal quantidadeMinima,
        String unidade
) {
}

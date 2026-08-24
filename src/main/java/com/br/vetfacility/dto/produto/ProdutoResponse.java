package com.br.vetfacility.dto.produto;

import com.br.vetfacility.domain.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id, String nome, BigDecimal quantidadeEstoque, BigDecimal quantidadeMinima,
        String unidade, boolean estoqueBaixo
) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
                p.getId(), p.getNome(), p.getQuantidadeEstoque(), p.getQuantidadeMinima(),
                p.getUnidade(), p.isEstoqueBaixo()
        );
    }
}

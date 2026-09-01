package com.br.vetfacility.dto.servico;

import com.br.vetfacility.domain.Servico;

import java.math.BigDecimal;
import java.util.List;

public record ServicoResponse(Long id, String nome, Integer duracaoMin, List<ProdutoPadraoResponse> produtosPadrao) {
    public record ProdutoPadraoResponse(Long produtoId, String produtoNome, BigDecimal quantidadePadrao) {
    }

    public static ServicoResponse from(Servico s) {
        return new ServicoResponse(
                s.getId(), s.getNome(), s.getDuracaoMin(),
                s.getProdutosPadrao().stream()
                        .map(sp -> new ProdutoPadraoResponse(sp.getProduto().getId(), sp.getProduto().getNome(), sp.getQuantidadePadrao()))
                        .toList()
        );
    }
}

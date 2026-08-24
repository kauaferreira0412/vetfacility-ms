package com.br.vetfacility.dto.servico;

import com.br.vetfacility.domain.Servico;

public record ServicoResponse(Long id, String nome, Integer duracaoMin) {
    public static ServicoResponse from(Servico s) {
        return new ServicoResponse(s.getId(), s.getNome(), s.getDuracaoMin());
    }
}

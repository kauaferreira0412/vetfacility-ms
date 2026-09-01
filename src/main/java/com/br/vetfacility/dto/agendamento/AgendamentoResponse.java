package com.br.vetfacility.dto.agendamento;

import com.br.vetfacility.domain.Agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoResponse(
        Long id,
        Long animalId, String animalNome,
        Long clienteId, String clienteNome,
        Long servicoId, String servicoNome, Integer duracaoMin,
        Long usuarioId, String usuarioNome,
        LocalDateTime dataHora,
        String status,
        String observacao,
        String motivoCancelamento,
        LocalDateTime iniciadoEm,
        List<ProdutoPlanejadoResponse> produtosPlanejados
) {
    public record ProdutoPlanejadoResponse(Long produtoId, String produtoNome, BigDecimal quantidade) {
    }

    public static AgendamentoResponse from(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getAnimal().getId(), a.getAnimal().getNome(),
                a.getAnimal().getCliente().getId(), a.getAnimal().getCliente().getNome(),
                a.getServico().getId(), a.getServico().getNome(), a.getServico().getDuracaoMin(),
                a.getUsuario().getId(), a.getUsuario().getNome(),
                a.getDataHora(),
                a.getStatus().name(),
                a.getObservacao(),
                a.getMotivoCancelamento(),
                a.getIniciadoEm(),
                a.getProdutosPlanejados().stream()
                        .map(p -> new ProdutoPlanejadoResponse(p.getProdutoId(), p.getProdutoNome(), p.getQuantidade()))
                        .toList()
        );
    }
}

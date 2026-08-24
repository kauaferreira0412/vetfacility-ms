package com.br.vetfacility.dto.agendamento;

import com.br.vetfacility.domain.Agendamento;

import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id,
        Long animalId, String animalNome,
        Long clienteId, String clienteNome,
        Long servicoId, String servicoNome, Integer duracaoMin,
        Long usuarioId, String usuarioNome,
        LocalDateTime dataHora,
        String status,
        String observacao,
        String motivoCancelamento
) {
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
                a.getMotivoCancelamento()
        );
    }
}

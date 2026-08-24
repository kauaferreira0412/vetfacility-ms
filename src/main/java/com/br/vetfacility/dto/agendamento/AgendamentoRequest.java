package com.br.vetfacility.dto.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @NotNull(message = "O animal é obrigatório") Long animalId,
        @NotNull(message = "O serviço é obrigatório") Long servicoId,
        @NotNull(message = "O executor (usuário) é obrigatório") Long usuarioId,
        @NotNull(message = "A data/hora é obrigatória")
        @Future(message = "A data/hora do agendamento deve ser no futuro") LocalDateTime dataHora,
        String observacao
) {
}

package com.br.vetfacility.dto.agendamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarAgendamentoRequest(
        @NotBlank(message = "Informe o motivo do cancelamento.")
        @Size(max = 300, message = "O motivo deve ter no máximo 300 caracteres.") String motivo
) {
}

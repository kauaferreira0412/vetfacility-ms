package com.br.vetfacility.dto.empresa;

import jakarta.validation.constraints.NotBlank;

public record AtualizarLogotipoRequest(
        @NotBlank(message = "O arquivo do logotipo é obrigatório") String logotipoBase64
) {
}

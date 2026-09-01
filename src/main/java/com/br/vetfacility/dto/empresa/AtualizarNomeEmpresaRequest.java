package com.br.vetfacility.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarNomeEmpresaRequest(
        @NotBlank(message = "O nome do negócio é obrigatório")
        @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres") String nome
) {
}

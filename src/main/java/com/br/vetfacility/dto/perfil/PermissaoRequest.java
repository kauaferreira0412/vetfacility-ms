package com.br.vetfacility.dto.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PermissaoRequest(
        @NotBlank(message = "O código é obrigatório")
        @Pattern(regexp = "^[A-Z0-9_]{3,60}$", message = "O código deve conter apenas letras maiúsculas, números e underscore (ex.: RELATORIO_VISUALIZAR)")
        String codigo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres") String descricao,

        @NotBlank(message = "O módulo é obrigatório")
        @Size(max = 40, message = "O módulo deve ter no máximo 40 caracteres") String modulo
) {
}

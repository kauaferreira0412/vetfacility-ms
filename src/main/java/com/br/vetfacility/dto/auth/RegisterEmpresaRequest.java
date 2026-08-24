package com.br.vetfacility.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterEmpresaRequest(
        @NotBlank(message = "O nome da empresa é obrigatório") String nomeEmpresa,
        @NotBlank(message = "O nome do usuário é obrigatório") String nomeUsuario,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha
) {
}

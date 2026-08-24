package com.br.vetfacility.dto.cliente;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "O nome do cliente é obrigatório") String nome,
        String telefone,
        String email,
        String cpf,
        String endereco,
        String cidade,
        String cep,
        String observacoes
) {
}

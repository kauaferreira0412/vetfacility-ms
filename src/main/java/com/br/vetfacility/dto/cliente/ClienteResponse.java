package com.br.vetfacility.dto.cliente;

import com.br.vetfacility.domain.Cliente;

public record ClienteResponse(
        Long id, String nome, String telefone, String email, String cpf,
        String endereco, String cidade, String cep, String observacoes
) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(
                c.getId(), c.getNome(), c.getTelefone(), c.getEmail(), c.getCpf(),
                c.getEndereco(), c.getCidade(), c.getCep(), c.getObservacoes()
        );
    }
}

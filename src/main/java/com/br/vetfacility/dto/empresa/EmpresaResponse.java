package com.br.vetfacility.dto.empresa;

import com.br.vetfacility.domain.Empresa;

public record EmpresaResponse(Long id, String nome) {
    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(empresa.getId(), empresa.getNome());
    }
}

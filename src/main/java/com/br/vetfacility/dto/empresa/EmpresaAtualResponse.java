package com.br.vetfacility.dto.empresa;

import com.br.vetfacility.domain.Empresa;

public record EmpresaAtualResponse(Long id, String nome, String logotipoUrl) {
    public static EmpresaAtualResponse from(Empresa empresa) {
        return new EmpresaAtualResponse(empresa.getId(), empresa.getNome(), empresa.getLogotipoUrl());
    }
}

package com.br.vetfacility.dto.perfil;

import com.br.vetfacility.domain.Permissao;

public record PermissaoResponse(Long id, String codigo, String descricao, String modulo) {
    public static PermissaoResponse from(Permissao p) {
        return new PermissaoResponse(p.getId(), p.getCodigo(), p.getDescricao(), p.getModulo());
    }
}

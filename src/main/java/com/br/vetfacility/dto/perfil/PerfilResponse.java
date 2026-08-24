package com.br.vetfacility.dto.perfil;

import com.br.vetfacility.domain.Perfil;

import java.util.List;

public record PerfilResponse(Long id, String nome, boolean sistema, List<PermissaoResponse> permissoes) {
    public static PerfilResponse from(Perfil p) {
        return new PerfilResponse(
                p.getId(), p.getNome(), p.isSistema(),
                p.getPermissoes().stream().map(PermissaoResponse::from).toList()
        );
    }
}

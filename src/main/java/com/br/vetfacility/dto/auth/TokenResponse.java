package com.br.vetfacility.dto.auth;

import java.util.List;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UsuarioResumo usuario
) {
    public record UsuarioResumo(
            Long id, String nome, String email, boolean root,
            Long empresaId, String empresaNome,
            Long perfilId, String perfilNome,
            List<String> permissoes
    ) {
    }
}

package com.br.vetfacility.dto.usuario;

import com.br.vetfacility.domain.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Long perfilId, String perfilNome) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil().getId(), u.getPerfil().getNome());
    }
}

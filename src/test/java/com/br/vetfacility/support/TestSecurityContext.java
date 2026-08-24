package com.br.vetfacility.support;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

public final class TestSecurityContext {

    private TestSecurityContext() {
    }

    public static void autenticarComoUsuarioDaEmpresa(Long usuarioId, Long empresaId, List<String> permissoes) {
        autenticar(usuarioId, empresaId, false, permissoes);
    }

    public static void autenticarComoRoot(Long usuarioId) {
        autenticar(usuarioId, null, true, List.of("EMPRESA_GERENCIAR"));
    }

    private static void autenticar(Long usuarioId, Long empresaId, boolean root, List<String> permissoes) {
        Jwt.Builder builder = Jwt.withTokenValue("token-de-teste")
                .header("alg", "none")
                .subject(String.valueOf(usuarioId))
                .claim("permissoes", permissoes)
                .claim("root", root)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        if (empresaId != null) {
            builder.claim("empresaId", empresaId);
        }
        Jwt jwt = builder.build();

        List<GrantedAuthority> authorities = permissoes.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, authorities));
    }

    public static void autenticarSemJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("anonimo", null));
    }

    public static void limpar() {
        SecurityContextHolder.clearContext();
    }
}

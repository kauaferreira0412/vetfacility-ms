package com.br.vetfacility.security;

import com.br.vetfacility.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

public final class SecurityUtils {

    private static final String HEADER_EMPRESA_ID = "X-Empresa-Id";

    private SecurityUtils() {
    }

    private static Jwt currentJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        throw new IllegalStateException("Nenhum usuário autenticado no contexto de segurança.");
    }

    public static Long currentUsuarioId() {
        return Long.valueOf(currentJwt().getSubject());
    }

    public static Long currentEmpresaId() {
        Object empresaId = currentJwt().getClaim("empresaId");
        if (empresaId != null) {
            return toLong(empresaId);
        }

        if (!isRoot()) {
            throw new IllegalStateException("O usuário autenticado não pertence a nenhuma empresa.");
        }

        String header = empresaIdHeader();
        if (header == null || header.isBlank()) {
            throw new BusinessException("Selecione uma empresa para continuar.");
        }
        try {
            return Long.valueOf(header.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("Empresa selecionada inválida.");
        }
    }

    public static boolean isRoot() {
        Boolean root = currentJwt().getClaim("root");
        return Boolean.TRUE.equals(root);
    }

    @SuppressWarnings("unchecked")
    public static List<String> currentPermissoes() {
        List<String> permissoes = currentJwt().getClaim("permissoes");
        return permissoes == null ? List.of() : permissoes;
    }

    private static String empresaIdHeader() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getHeader(HEADER_EMPRESA_ID);
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        return Long.valueOf(String.valueOf(value));
    }
}

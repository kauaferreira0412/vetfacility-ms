package com.br.vetfacility.controller;

import com.br.vetfacility.dto.usuario.UsuarioResponse;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    @Operation(summary = "Lista os usuários da empresa autenticada")
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(UsuarioResponse::from).toList();
    }
}

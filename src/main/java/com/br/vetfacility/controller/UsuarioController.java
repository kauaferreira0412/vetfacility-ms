package com.br.vetfacility.controller;

import com.br.vetfacility.dto.usuario.UsuarioResponse;
import com.br.vetfacility.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    @Operation(summary = "Lista os usuários da empresa autenticada")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    @Operation(summary = "Desativa um usuário da empresa, revogando seu acesso imediatamente")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}

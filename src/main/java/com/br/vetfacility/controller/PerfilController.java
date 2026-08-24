package com.br.vetfacility.controller;

import com.br.vetfacility.dto.perfil.PerfilRequest;
import com.br.vetfacility.dto.perfil.PerfilResponse;
import com.br.vetfacility.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@Tag(name = "Perfis de Acesso")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERFIL_GERENCIAR', 'USUARIO_GERENCIAR')")
    @Operation(summary = "Lista os perfis de acesso da empresa autenticada")
    public List<PerfilResponse> listar() {
        return perfilService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    @Operation(summary = "Cria um novo perfil de acesso customizado")
    public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    @Operation(summary = "Atualiza o nome e as permissões de um perfil customizado")
    public PerfilResponse atualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest request) {
        return perfilService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    @Operation(summary = "Remove um perfil de acesso customizado")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        perfilService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

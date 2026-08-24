package com.br.vetfacility.controller;

import com.br.vetfacility.dto.perfil.PermissaoRequest;
import com.br.vetfacility.dto.perfil.PermissaoResponse;
import com.br.vetfacility.service.PermissaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissoes")
@Tag(name = "Permissões")
public class PermissaoController {

    private final PermissaoService permissaoService;

    public PermissaoController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @GetMapping
    @Operation(summary = "Lista o catálogo de permissões do sistema")
    public List<PermissaoResponse> listar() {
        return permissaoService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROOT')")
    @Operation(summary = "Cadastra uma nova permissão (e, opcionalmente, um novo módulo) no catálogo do sistema (somente ROOT)")
    public ResponseEntity<PermissaoResponse> criar(@Valid @RequestBody PermissaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissaoService.criar(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROOT')")
    @Operation(summary = "Remove uma permissão do catálogo, desde que não esteja atribuída a nenhum perfil (somente ROOT)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        permissaoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

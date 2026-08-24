package com.br.vetfacility.controller;

import com.br.vetfacility.dto.cliente.ClienteRequest;
import com.br.vetfacility.dto.cliente.ClienteResponse;
import com.br.vetfacility.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE_VISUALIZAR')")
    @Operation(summary = "Lista os clientes da empresa")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    @Operation(summary = "Cadastra um novo cliente")
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    @Operation(summary = "Atualiza os dados de um cliente")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    @Operation(summary = "Remove um cliente")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

package com.br.vetfacility.controller;

import com.br.vetfacility.dto.produto.ProdutoRequest;
import com.br.vetfacility.dto.produto.ProdutoResponse;
import com.br.vetfacility.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Estoque")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_VISUALIZAR')")
    @Operation(summary = "Lista os produtos em estoque da empresa")
    public List<ProdutoResponse> listar() {
        return produtoService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    @Operation(summary = "Cadastra um novo produto")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    @Operation(summary = "Atualiza os dados de um produto")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        produtoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

package com.br.vetfacility.controller;

import com.br.vetfacility.dto.servico.AtualizarProdutosPadraoRequest;
import com.br.vetfacility.dto.servico.ServicoResponse;
import com.br.vetfacility.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@Tag(name = "Serviços")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SERVICO_VISUALIZAR')")
    @Operation(summary = "Lista os tipos de serviço da empresa, com os produtos padrão de cada um")
    public List<ServicoResponse> listar() {
        return servicoService.listar();
    }

    @PutMapping("/{id}/produtos-padrao")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    @Operation(summary = "Define os produtos e quantidades consumidos por padrão nesse tipo de serviço")
    public ServicoResponse atualizarProdutosPadrao(@PathVariable Long id, @Valid @RequestBody AtualizarProdutosPadraoRequest request) {
        return servicoService.atualizarProdutosPadrao(id, request);
    }
}

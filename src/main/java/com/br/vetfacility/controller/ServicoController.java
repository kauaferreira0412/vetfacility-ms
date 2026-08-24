package com.br.vetfacility.controller;

import com.br.vetfacility.dto.servico.ServicoResponse;
import com.br.vetfacility.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(summary = "Lista os tipos de serviço da empresa")
    public List<ServicoResponse> listar() {
        return servicoService.listar();
    }
}

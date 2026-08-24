package com.br.vetfacility.controller;

import com.br.vetfacility.dto.empresa.EmpresaResponse;
import com.br.vetfacility.repository.EmpresaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROOT')")
    @Operation(summary = "Lista todas as empresas cadastradas na plataforma (somente ROOT)")
    public List<EmpresaResponse> listar() {
        return empresaRepository.findAllByOrderByNomeAsc().stream().map(EmpresaResponse::from).toList();
    }
}

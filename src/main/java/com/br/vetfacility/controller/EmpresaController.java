package com.br.vetfacility.controller;

import com.br.vetfacility.dto.empresa.AtualizarLogotipoRequest;
import com.br.vetfacility.dto.empresa.AtualizarNomeEmpresaRequest;
import com.br.vetfacility.dto.empresa.EmpresaAtualResponse;
import com.br.vetfacility.dto.empresa.EmpresaResponse;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaRepository empresaRepository, EmpresaService empresaService) {
        this.empresaRepository = empresaRepository;
        this.empresaService = empresaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROOT')")
    @Operation(summary = "Lista todas as empresas cadastradas na plataforma (somente ROOT)")
    public List<EmpresaResponse> listar() {
        return empresaRepository.findAllByOrderByNomeAsc().stream().map(EmpresaResponse::from).toList();
    }

    @GetMapping("/atual")
    @Operation(summary = "Retorna o nome e o logotipo da empresa do usuário autenticado, para exibir na interface")
    public EmpresaAtualResponse atual() {
        return empresaService.obterAtual();
    }

    @PutMapping("/atual/nome")
    @PreAuthorize("hasAuthority('EMPRESA_PERSONALIZAR')")
    @Operation(summary = "Atualiza o nome do negócio exibido no sistema")
    public EmpresaAtualResponse atualizarNome(@Valid @RequestBody AtualizarNomeEmpresaRequest request) {
        return empresaService.atualizarNome(request);
    }

    @PutMapping("/atual/logotipo")
    @PreAuthorize("hasAuthority('EMPRESA_PERSONALIZAR')")
    @Operation(summary = "Atualiza o logotipo do negócio (imagem PNG/JPG em base64, até 2MB)")
    public EmpresaAtualResponse atualizarLogotipo(@Valid @RequestBody AtualizarLogotipoRequest request) {
        return empresaService.atualizarLogotipo(request);
    }

    @DeleteMapping("/atual/logotipo")
    @PreAuthorize("hasAuthority('EMPRESA_PERSONALIZAR')")
    @Operation(summary = "Remove o logotipo do negócio, voltando ao ícone padrão do sistema")
    public EmpresaAtualResponse removerLogotipo() {
        return empresaService.removerLogotipo();
    }
}

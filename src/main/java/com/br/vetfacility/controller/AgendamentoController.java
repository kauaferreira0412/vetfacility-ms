package com.br.vetfacility.controller;

import com.br.vetfacility.dto.agendamento.AgendamentoRequest;
import com.br.vetfacility.dto.agendamento.AgendamentoResponse;
import com.br.vetfacility.dto.agendamento.CancelarAgendamentoRequest;
import com.br.vetfacility.dto.agendamento.ConcluirAgendamentoRequest;
import com.br.vetfacility.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@Tag(name = "Agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AGENDAMENTO_VISUALIZAR')")
    @Operation(summary = "Lista os agendamentos da empresa, com filtro opcional por período")
    public List<AgendamentoResponse> listar(
            @RequestParam(name = "de", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(name = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return agendamentoService.listar(de, ate);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AGENDAMENTO_CRIAR')")
    @Operation(summary = "Cria um novo agendamento")
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('AGENDAMENTO_CRIAR')")
    @Operation(summary = "Reagenda um agendamento existente")
    public AgendamentoResponse reagendar(@PathVariable Long id, @Valid @RequestBody AgendamentoRequest request) {
        return agendamentoService.reagendar(id, request);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('AGENDAMENTO_CANCELAR')")
    @Operation(summary = "Cancela um agendamento, registrando a justificativa informada")
    public AgendamentoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarAgendamentoRequest request) {
        return agendamentoService.cancelar(id, request);
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAuthority('AGENDAMENTO_CONCLUIR')")
    @Operation(summary = "Inicia o atendimento (AGENDADO -> EM_ATENDIMENTO)")
    public AgendamentoResponse iniciar(@PathVariable Long id) {
        return agendamentoService.iniciarAtendimento(id);
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize("hasAuthority('AGENDAMENTO_CONCLUIR')")
    @Operation(summary = "Conclui o atendimento e dá baixa nos produtos de estoque utilizados")
    public AgendamentoResponse concluir(@PathVariable Long id,
                                         @RequestBody(required = false) ConcluirAgendamentoRequest request) {
        return agendamentoService.concluir(id, request);
    }
}

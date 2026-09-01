package com.br.vetfacility.controller;

import com.br.vetfacility.dto.financeiro.MovimentacaoFinanceiraRequest;
import com.br.vetfacility.dto.financeiro.MovimentacaoFinanceiraResponse;
import com.br.vetfacility.dto.financeiro.ResultadoFinanceiroResponse;
import com.br.vetfacility.dto.financeiro.ResumoFinanceiroResponse;
import com.br.vetfacility.service.FinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro")
@Tag(name = "Financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/resultado")
    @PreAuthorize("hasAuthority('FINANCEIRO_VISUALIZAR')")
    @Operation(summary = "Lista as movimentações e o resultado (ganhos - gastos) do período informado")
    public ResultadoFinanceiroResponse resultado(
            @RequestParam(name = "de", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(name = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return financeiroService.resultado(de, ate);
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('FINANCEIRO_VISUALIZAR')")
    @Operation(summary = "Resumo financeiro do dia e do mês atual, para o dashboard")
    public ResumoFinanceiroResponse resumo() {
        return financeiroService.resumo();
    }

    @PostMapping("/gastos")
    @PreAuthorize("hasAuthority('FINANCEIRO_GERENCIAR')")
    @Operation(summary = "Lança um gasto avulso do negócio, sem vínculo com agendamento")
    public ResponseEntity<MovimentacaoFinanceiraResponse> registrarGasto(@Valid @RequestBody MovimentacaoFinanceiraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.registrarGasto(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_GERENCIAR')")
    @Operation(summary = "Remove uma movimentação financeira")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        financeiroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

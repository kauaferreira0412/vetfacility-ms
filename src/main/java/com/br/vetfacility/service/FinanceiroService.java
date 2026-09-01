package com.br.vetfacility.service;

import com.br.vetfacility.domain.Agendamento;
import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.MovimentacaoFinanceira;
import com.br.vetfacility.enums.TipoMovimentacao;
import com.br.vetfacility.dto.financeiro.MovimentacaoFinanceiraRequest;
import com.br.vetfacility.dto.financeiro.MovimentacaoFinanceiraResponse;
import com.br.vetfacility.dto.financeiro.ResultadoFinanceiroResponse;
import com.br.vetfacility.dto.financeiro.ResumoFinanceiroResponse;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.MovimentacaoFinanceiraRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class FinanceiroService {

    private final MovimentacaoFinanceiraRepository movimentacaoRepository;
    private final EmpresaRepository empresaRepository;

    public FinanceiroService(MovimentacaoFinanceiraRepository movimentacaoRepository, EmpresaRepository empresaRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public ResultadoFinanceiroResponse resultado(LocalDate de, LocalDate ate) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        LocalDate inicio = de != null ? de : LocalDate.now().withDayOfMonth(1);
        LocalDate fim = ate != null ? ate : LocalDate.now();

        List<MovimentacaoFinanceira> movimentacoes = movimentacaoRepository.findAllByEmpresaIdAndDataBetween(empresaId, inicio, fim);

        BigDecimal totalGanhos = somar(movimentacoes, TipoMovimentacao.GANHO);
        BigDecimal totalGastos = somar(movimentacoes, TipoMovimentacao.GASTO);

        return new ResultadoFinanceiroResponse(
                inicio, fim, totalGanhos, totalGastos, totalGanhos.subtract(totalGastos),
                movimentacoes.stream().map(MovimentacaoFinanceiraResponse::from).toList()
        );
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse resumo() {
        Long empresaId = SecurityUtils.currentEmpresaId();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioDoMes = hoje.with(TemporalAdjusters.firstDayOfMonth());

        List<MovimentacaoFinanceira> doMes = movimentacaoRepository.findAllByEmpresaIdAndDataBetween(empresaId, inicioDoMes, hoje);

        BigDecimal resultadoMes = somar(doMes, TipoMovimentacao.GANHO).subtract(somar(doMes, TipoMovimentacao.GASTO));

        List<MovimentacaoFinanceira> doDia = doMes.stream().filter(m -> m.getData().isEqual(hoje)).toList();
        BigDecimal resultadoHoje = somar(doDia, TipoMovimentacao.GANHO).subtract(somar(doDia, TipoMovimentacao.GASTO));

        return new ResumoFinanceiroResponse(resultadoHoje, resultadoMes);
    }

    @Transactional
    public MovimentacaoFinanceiraResponse registrarGasto(MovimentacaoFinanceiraRequest request) {
        Empresa empresa = empresaRepository.getReferenceById(SecurityUtils.currentEmpresaId());
        MovimentacaoFinanceira movimentacao = MovimentacaoFinanceira.builder()
                .tipo(TipoMovimentacao.GASTO)
                .valor(request.valor())
                .data(request.data())
                .descricao(request.descricao())
                .empresa(empresa)
                .build();
        return MovimentacaoFinanceiraResponse.from(movimentacaoRepository.save(movimentacao));
    }

    @Transactional
    public void registrarGanhoDeAgendamento(Agendamento agendamento, BigDecimal valor) {
        MovimentacaoFinanceira movimentacao = MovimentacaoFinanceira.builder()
                .tipo(TipoMovimentacao.GANHO)
                .valor(valor)
                .data(LocalDate.now())
                .descricao("Atendimento: " + agendamento.getServico().getNome() + " - " + agendamento.getAnimal().getNome())
                .agendamento(agendamento)
                .empresa(agendamento.getEmpresa())
                .build();
        movimentacaoRepository.save(movimentacao);
    }

    @Transactional
    public void remover(Long id) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        MovimentacaoFinanceira movimentacao = movimentacaoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação financeira não encontrada."));
        movimentacaoRepository.delete(movimentacao);
    }

    private BigDecimal somar(List<MovimentacaoFinanceira> movimentacoes, TipoMovimentacao tipo) {
        return movimentacoes.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(MovimentacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

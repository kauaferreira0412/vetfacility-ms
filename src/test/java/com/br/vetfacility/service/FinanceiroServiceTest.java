package com.br.vetfacility.service;

import com.br.vetfacility.domain.Agendamento;
import com.br.vetfacility.domain.Animal;
import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.MovimentacaoFinanceira;
import com.br.vetfacility.domain.Servico;
import com.br.vetfacility.enums.TipoMovimentacao;
import com.br.vetfacility.dto.financeiro.MovimentacaoFinanceiraRequest;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.MovimentacaoFinanceiraRepository;
import com.br.vetfacility.support.TestSecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Módulo Financeiro (FIN-01 a FIN-05): apuração de resultado (ganhos - gastos) por período e o
 * ganho automático vinculado ao agendamento concluído (ver AgendamentoServiceTest).
 */
@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock private MovimentacaoFinanceiraRepository movimentacaoRepository;
    @Mock private EmpresaRepository empresaRepository;

    private FinanceiroService service;

    @BeforeEach
    void setUp() {
        service = new FinanceiroService(movimentacaoRepository, empresaRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(1L, EMPRESA_ID, List.of("FINANCEIRO_GERENCIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    private Empresa empresa() {
        return Empresa.builder().id(EMPRESA_ID).nome("Pet Smack").build();
    }

    @Test
    void resultado_comGanhosEGastos_deveCalcularSaldoLiquido() {
        LocalDate hoje = LocalDate.now();
        List<MovimentacaoFinanceira> movimentacoes = List.of(
                MovimentacaoFinanceira.builder().id(1L).tipo(TipoMovimentacao.GANHO)
                        .valor(new BigDecimal("100.00")).data(hoje).empresa(empresa()).build(),
                MovimentacaoFinanceira.builder().id(2L).tipo(TipoMovimentacao.GASTO)
                        .valor(new BigDecimal("30.00")).data(hoje).descricao("Compra de shampoo").empresa(empresa()).build()
        );
        when(movimentacaoRepository.findAllByEmpresaIdAndDataBetween(eq(EMPRESA_ID), any(), any())).thenReturn(movimentacoes);

        var resultado = service.resultado(hoje, hoje);

        assertThat(resultado.totalGanhos()).isEqualByComparingTo("100.00");
        assertThat(resultado.totalGastos()).isEqualByComparingTo("30.00");
        assertThat(resultado.resultado()).isEqualByComparingTo("70.00");
        assertThat(resultado.movimentacoes()).hasSize(2);
    }

    @Test
    void resultado_semPeriodoInformado_devePadraoParaMesAtual() {
        when(movimentacaoRepository.findAllByEmpresaIdAndDataBetween(eq(EMPRESA_ID), any(), any())).thenReturn(List.of());

        var resultado = service.resultado(null, null);

        assertThat(resultado.de()).isEqualTo(LocalDate.now().withDayOfMonth(1));
        assertThat(resultado.ate()).isEqualTo(LocalDate.now());
        assertThat(resultado.resultado()).isEqualByComparingTo("0");
    }

    @Test
    void registrarGasto_devePersistirComoTipoGasto() {
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa());
        when(movimentacaoRepository.save(any(MovimentacaoFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new MovimentacaoFinanceiraRequest(new BigDecimal("45.00"), LocalDate.now(), "Compra de produtos");
        var response = service.registrarGasto(request);

        assertThat(response.tipo()).isEqualTo("GASTO");
        assertThat(response.valor()).isEqualByComparingTo("45.00");
    }

    @Test
    void registrarGanhoDeAgendamento_devePersistirComoTipoGanhoVinculadoAoAgendamento() {
        Agendamento agendamento = Agendamento.builder().id(7L)
                .animal(Animal.builder().id(1L).nome("Bidu").build())
                .servico(Servico.builder().id(1L).nome("Banho").build())
                .empresa(empresa())
                .build();
        when(movimentacaoRepository.save(any(MovimentacaoFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarGanhoDeAgendamento(agendamento, new BigDecimal("80.00"));

        verify(movimentacaoRepository).save(argThat(m ->
                m.getTipo() == TipoMovimentacao.GANHO
                        && m.getValor().compareTo(new BigDecimal("80.00")) == 0
                        && m.getAgendamento() == agendamento));
    }

    @Test
    void remover_movimentacaoDeOutraEmpresa_deveLancarResourceNotFound() {
        when(movimentacaoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(movimentacaoRepository, never()).delete(any());
    }
}

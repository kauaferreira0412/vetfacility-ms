package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.MovimentacaoFinanceira;
import com.br.vetfacility.enums.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MovimentacaoFinanceiraRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MovimentacaoFinanceiraRepository movimentacaoRepository;

    @Test
    void findAllByEmpresaIdAndDataBetween_deveFiltrarPorPeriodoEEmpresa() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Empresa outraEmpresa = em.persistAndFlush(Empresa.builder().nome("Outra").build());

        em.persistAndFlush(MovimentacaoFinanceira.builder().tipo(TipoMovimentacao.GANHO)
                .valor(new BigDecimal("50.00")).data(LocalDate.now()).empresa(empresa).build());
        em.persistAndFlush(MovimentacaoFinanceira.builder().tipo(TipoMovimentacao.GASTO)
                .valor(new BigDecimal("20.00")).data(LocalDate.now().minusDays(40)).empresa(empresa).build());
        em.persistAndFlush(MovimentacaoFinanceira.builder().tipo(TipoMovimentacao.GANHO)
                .valor(new BigDecimal("99.00")).data(LocalDate.now()).empresa(outraEmpresa).build());

        List<MovimentacaoFinanceira> doPeriodo = movimentacaoRepository.findAllByEmpresaIdAndDataBetween(
                empresa.getId(), LocalDate.now().minusDays(5), LocalDate.now());

        assertThat(doPeriodo).hasSize(1);
        assertThat(doPeriodo.get(0).getValor()).isEqualByComparingTo("50.00");
    }

    @Test
    void findByIdAndEmpresaId_deveIsolarPorTenant() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Empresa outraEmpresa = em.persistAndFlush(Empresa.builder().nome("Outra").build());
        MovimentacaoFinanceira movimentacao = em.persistAndFlush(MovimentacaoFinanceira.builder()
                .tipo(TipoMovimentacao.GASTO).valor(new BigDecimal("10.00")).data(LocalDate.now()).empresa(empresa).build());

        assertThat(movimentacaoRepository.findByIdAndEmpresaId(movimentacao.getId(), empresa.getId())).isPresent();
        assertThat(movimentacaoRepository.findByIdAndEmpresaId(movimentacao.getId(), outraEmpresa.getId())).isEmpty();
    }
}

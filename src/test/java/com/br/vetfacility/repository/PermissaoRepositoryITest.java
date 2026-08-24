package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Permissao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PermissaoRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Test
    void existsByCodigoIgnoreCase_deveIgnorarCaixa() {
        em.persistAndFlush(Permissao.builder().codigo("TESTE_RELATORIO").descricao("d").modulo("TESTE").build());

        assertThat(permissaoRepository.existsByCodigoIgnoreCase("teste_relatorio")).isTrue();
        assertThat(permissaoRepository.existsByCodigoIgnoreCase("TESTE_RELATORIO")).isTrue();
        assertThat(permissaoRepository.existsByCodigoIgnoreCase("TESTE_NAO_EXISTE")).isFalse();
    }

    @Test
    void findAllByCodigoIn_eFindAllByIdIn_devemRetornarApenasOsSolicitados() {
        Permissao p1 = em.persistAndFlush(Permissao.builder().codigo("TESTE_A").descricao("d").modulo("TESTE").build());
        Permissao p2 = em.persistAndFlush(Permissao.builder().codigo("TESTE_B").descricao("d").modulo("TESTE").build());
        em.persistAndFlush(Permissao.builder().codigo("TESTE_C").descricao("d").modulo("TESTE").build());

        List<Permissao> porCodigo = permissaoRepository.findAllByCodigoIn(List.of("TESTE_A", "TESTE_B"));
        List<Permissao> porId = permissaoRepository.findAllByIdIn(List.of(p1.getId(), p2.getId()));

        assertThat(porCodigo).extracting(Permissao::getCodigo).containsExactlyInAnyOrder("TESTE_A", "TESTE_B");
        assertThat(porId).extracting(Permissao::getCodigo).containsExactlyInAnyOrder("TESTE_A", "TESTE_B");
    }

    @Test
    void findAllByOrderByModuloAscCodigoAsc_deveOrdenarPrimeiroPorModuloDepoisPorCodigo() {
        em.persistAndFlush(Permissao.builder().codigo("TESTE_Z_B").descricao("d").modulo("TESTE_Z").build());
        em.persistAndFlush(Permissao.builder().codigo("TESTE_Z_A").descricao("d").modulo("TESTE_Z").build());
        em.persistAndFlush(Permissao.builder().codigo("TESTE_A_A").descricao("d").modulo("TESTE_A").build());

        List<Permissao> resultado = permissaoRepository.findAllByOrderByModuloAscCodigoAsc();

        int idxModuloA = resultado.indexOf(resultado.stream().filter(p -> p.getCodigo().equals("TESTE_A_A")).findFirst().orElseThrow());
        int idxModuloZ_A = resultado.indexOf(resultado.stream().filter(p -> p.getCodigo().equals("TESTE_Z_A")).findFirst().orElseThrow());
        int idxModuloZ_B = resultado.indexOf(resultado.stream().filter(p -> p.getCodigo().equals("TESTE_Z_B")).findFirst().orElseThrow());

        assertThat(idxModuloA).isLessThan(idxModuloZ_A);
        assertThat(idxModuloZ_A).isLessThan(idxModuloZ_B);
    }
}

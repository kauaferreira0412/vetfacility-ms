package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
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
class EmpresaRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    void findAllByOrderByNomeAscdeveRetornarEmpresasEmOrdemAlfabetica() {
        em.persistAndFlush(Empresa.builder().nome("Zoo Pet").build());
        em.persistAndFlush(Empresa.builder().nome("Amigo Fiel").build());
        em.persistAndFlush(Empresa.builder().nome("Mundo Cão").build());

        List<Empresa> resultado = empresaRepository.findAllByOrderByNomeAsc();

        assertThat(resultado).extracting(Empresa::getNome)
                .containsExactly("Amigo Fiel", "Mundo Cão", "Zoo Pet");
    }
}

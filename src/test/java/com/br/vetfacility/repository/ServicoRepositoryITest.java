package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Servico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ServicoRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ServicoRepository servicoRepository;

    @Test
    void findAllByEmpresaId_eFindByIdAndEmpresaId_devemIsolarPorEmpresa() {
        Empresa empresaA = em.persistAndFlush(Empresa.builder().nome("Empresa A").build());
        Empresa empresaB = em.persistAndFlush(Empresa.builder().nome("Empresa B").build());
        Servico banho = em.persistAndFlush(Servico.builder().nome("Banho").duracaoMin(60).empresa(empresaA).build());
        em.persistAndFlush(Servico.builder().nome("Tosa").duracaoMin(90).empresa(empresaB).build());

        assertThat(servicoRepository.findAllByEmpresaId(empresaA.getId())).hasSize(1);

        Optional<Servico> encontrado = servicoRepository.findByIdAndEmpresaId(banho.getId(), empresaA.getId());
        Optional<Servico> naoEncontrado = servicoRepository.findByIdAndEmpresaId(banho.getId(), empresaB.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDuracaoMin()).isEqualTo(60);
        assertThat(naoEncontrado).isEmpty();
    }
}

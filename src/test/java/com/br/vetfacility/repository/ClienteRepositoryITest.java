package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Cliente;
import com.br.vetfacility.domain.Empresa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClienteRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void findAllByEmpresaIdOrderByNomeAsc_devePrecisarIsolarPorEmpresaEOrdenar() {
        Empresa empresaA = em.persistAndFlush(Empresa.builder().nome("Empresa A").build());
        Empresa empresaB = em.persistAndFlush(Empresa.builder().nome("Empresa B").build());

        em.persistAndFlush(Cliente.builder().nome("Zeca").empresa(empresaA).build());
        em.persistAndFlush(Cliente.builder().nome("Ana").empresa(empresaA).build());
        em.persistAndFlush(Cliente.builder().nome("Cliente de outra empresa").empresa(empresaB).build());

        List<Cliente> resultado = clienteRepository.findAllByEmpresaIdOrderByNomeAsc(empresaA.getId());

        assertThat(resultado).extracting(Cliente::getNome).containsExactly("Ana", "Zeca");
    }

    @Test
    void findByIdAndEmpresaId_deNaoDevePermitirVazamentoEntreEmpresas() {
        Empresa empresaA = em.persistAndFlush(Empresa.builder().nome("Empresa A").build());
        Empresa empresaB = em.persistAndFlush(Empresa.builder().nome("Empresa B").build());
        Cliente cliente = em.persistAndFlush(Cliente.builder().nome("Maria").email("maria@teste.com").empresa(empresaA).build());

        Optional<Cliente> comEmpresaCerta = clienteRepository.findByIdAndEmpresaId(cliente.getId(), empresaA.getId());
        Optional<Cliente> comEmpresaErrada = clienteRepository.findByIdAndEmpresaId(cliente.getId(), empresaB.getId());

        assertThat(comEmpresaCerta).isPresent();
        assertThat(comEmpresaCerta.get().getEmail()).isEqualTo("maria@teste.com");
        assertThat(comEmpresaErrada).isEmpty();
    }
}

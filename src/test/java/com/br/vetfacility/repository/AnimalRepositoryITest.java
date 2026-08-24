package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Animal;
import com.br.vetfacility.domain.Cliente;
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
class AnimalRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AnimalRepository animalRepository;

    @Test
    void findAllByClienteIdAndEmpresaId_deveFiltrarPorClienteEEmpresa() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Cliente cliente1 = em.persistAndFlush(Cliente.builder().nome("Maria").empresa(empresa).build());
        Cliente cliente2 = em.persistAndFlush(Cliente.builder().nome("João").empresa(empresa).build());

        em.persistAndFlush(Animal.builder().nome("Bidu").cliente(cliente1).empresa(empresa).build());
        em.persistAndFlush(Animal.builder().nome("Rex").cliente(cliente1).empresa(empresa).build());
        em.persistAndFlush(Animal.builder().nome("Thor").cliente(cliente2).empresa(empresa).build());

        List<Animal> animaisDaMaria = animalRepository.findAllByClienteIdAndEmpresaId(cliente1.getId(), empresa.getId());

        assertThat(animaisDaMaria).extracting(Animal::getNome).containsExactlyInAnyOrder("Bidu", "Rex");
    }

    @Test
    void findAllByEmpresaId_deveRetornarTodosOsAnimaisDaEmpresa() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Cliente cliente = em.persistAndFlush(Cliente.builder().nome("Maria").empresa(empresa).build());
        em.persistAndFlush(Animal.builder().nome("Bidu").cliente(cliente).empresa(empresa).build());
        em.persistAndFlush(Animal.builder().nome("Rex").cliente(cliente).empresa(empresa).build());

        assertThat(animalRepository.findAllByEmpresaId(empresa.getId())).hasSize(2);
    }
}

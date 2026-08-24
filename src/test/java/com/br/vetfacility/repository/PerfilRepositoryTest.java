package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PerfilRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PerfilRepository perfilRepository;

    @Test
    void findByNomeAndEmpresaIsNull_deveEncontrarSoOPerfilSemEmpresa() {
        em.persistAndFlush(Perfil.builder().nome("Administrador da Plataforma").empresa(null).sistema(true).build());

        Optional<Perfil> comEmpresaQualquer = perfilRepository.findByNomeAndEmpresaId("Administrador da Plataforma", 99999L);
        assertThat(comEmpresaQualquer).isEmpty();

        Optional<Perfil> semEmpresa = perfilRepository.findByNomeAndEmpresaIsNull("Administrador da Plataforma");
        assertThat(semEmpresa).isPresent();
        assertThat(semEmpresa.get().isSistema()).isTrue();
    }

    @Test
    void findAllByEmpresaId_eFindByNomeAndEmpresaId_devemIsolarPorEmpresa() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        em.persistAndFlush(Perfil.builder().nome("Tosador").empresa(empresa).sistema(false).build());

        assertThat(perfilRepository.findAllByEmpresaId(empresa.getId())).hasSize(1);
        assertThat(perfilRepository.findByNomeAndEmpresaId("Tosador", empresa.getId())).isPresent();
        assertThat(perfilRepository.findByNomeAndEmpresaId("Inexistente", empresa.getId())).isEmpty();
    }

    @Test
    void existsByPermissoesId_deveDetectarQuandoPermissaoEstaAtribuidaAAlgumPerfil() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Permissao permissao = em.persistAndFlush(Permissao.builder().codigo("TESTE_X").descricao("d").modulo("M").build());
        Permissao permissaoNaoUsada = em.persistAndFlush(Permissao.builder().codigo("TESTE_Y").descricao("d").modulo("M").build());

        Set<Permissao> permissoes = new HashSet<>(List.of(permissao));
        em.persistAndFlush(Perfil.builder().nome("Recepção").empresa(empresa).sistema(false).permissoes(permissoes).build());

        assertThat(perfilRepository.existsByPermissoesId(permissao.getId())).isTrue();
        assertThat(perfilRepository.existsByPermissoesId(permissaoNaoUsada.getId())).isFalse();
    }
}

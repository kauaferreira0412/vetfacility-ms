package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Usuario;
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
class UsuarioRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Empresa empresa;

    private Usuario novoUsuario(String nome, String email, Perfil perfil) {
        return em.persistAndFlush(Usuario.builder()
                .nome(nome).email(email).senhaHash("hash").ativo(true)
                .empresa(empresa).perfil(perfil).build());
    }

    @Test
    void findAllByEmpresaId_devePopularOPerfilDeCadaUsuarioSemLazyLoading() {
        empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Perfil proprietario = em.persistAndFlush(Perfil.builder().nome("Proprietário").empresa(empresa).sistema(true).build());
        Perfil auxiliar = em.persistAndFlush(Perfil.builder().nome("Auxiliar").empresa(empresa).sistema(true).build());

        novoUsuario("Zeca", "zeca@teste.com", auxiliar);
        novoUsuario("Ana", "ana@teste.com", proprietario);

        List<Usuario> resultado = usuarioRepository.findAllByEmpresaId(empresa.getId());

        em.getEntityManager().clear();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Usuario::getNome).containsExactly("Ana", "Zeca"); // ORDER BY u.nome
        assertThat(resultado.get(0).getPerfil().getNome()).isEqualTo("Proprietário");
        assertThat(resultado.get(1).getPerfil().getNome()).isEqualTo("Auxiliar");
    }

    @Test
    void findByEmailIgnoreCase_deveIgnorarCaixaDoEmail() {
        empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Perfil perfil = em.persistAndFlush(Perfil.builder().nome("Proprietário").empresa(empresa).sistema(true).build());
        novoUsuario("Francisco", "Francisco@Teste.COM", perfil);

        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase("francisco@teste.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Francisco");
    }

    @Test
    void existsByEmailIgnoreCase_eExistsByPerfilId() {
        empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Perfil perfil = em.persistAndFlush(Perfil.builder().nome("Proprietário").empresa(empresa).sistema(true).build());
        novoUsuario("Francisco", "francisco@teste.com", perfil);

        assertThat(usuarioRepository.existsByEmailIgnoreCase("FRANCISCO@TESTE.COM")).isTrue();
        assertThat(usuarioRepository.existsByEmailIgnoreCase("outro@teste.com")).isFalse();
        assertThat(usuarioRepository.existsByPerfilId(perfil.getId())).isTrue();
    }
}

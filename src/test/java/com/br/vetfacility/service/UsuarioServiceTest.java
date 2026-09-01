package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.support.TestSecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Gerenciamento de usuários (SEG-05): desativação revoga o acesso imediatamente (login passa a
 * ser recusado, ver AuthService.login), sem apagar o histórico de agendamentos do usuário.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_LOGADO_ID = 10L;

    @Mock private UsuarioRepository usuarioRepository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(USUARIO_LOGADO_ID, EMPRESA_ID, List.of("USUARIO_GERENCIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    private Usuario usuario(Long id) {
        Perfil perfil = Perfil.builder().id(1L).nome("Auxiliar").build();
        return Usuario.builder().id(id).nome("Auxiliar").ativo(true)
                .empresa(Empresa.builder().id(EMPRESA_ID).build()).perfil(perfil).build();
    }

    @Test
    void desativar_usuarioDaEmpresa_deveMarcarComoInativo() {
        Usuario alvo = usuario(20L);
        when(usuarioRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(alvo));

        service.desativar(20L);

        assertThat(alvo.isAtivo()).isFalse();
        verify(usuarioRepository).save(alvo);
    }

    @Test
    void desativar_proprioUsuario_deveLancarBusinessException() {
        assertThatThrownBy(() -> service.desativar(USUARIO_LOGADO_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("próprio usuário");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desativar_usuarioDeOutraEmpresa_deveLancarResourceNotFound() {
        when(usuarioRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(usuarioRepository, never()).save(any());
    }
}

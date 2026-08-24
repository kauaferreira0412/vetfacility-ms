package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.dto.perfil.PerfilRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock private PerfilRepository perfilRepository;
    @Mock private PermissaoRepository permissaoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private PerfilService service;

    @BeforeEach
    void setUp() {
        service = new PerfilService(perfilRepository, permissaoRepository, empresaRepository, usuarioRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(1L, EMPRESA_ID, List.of("PERFIL_GERENCIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    @Test
    void criar_comNomeJaExistenteNaEmpresa_deveLancarBusinessException() {
        when(perfilRepository.findByNomeAndEmpresaId("Tosador", EMPRESA_ID))
                .thenReturn(Optional.of(Perfil.builder().id(5L).nome("Tosador").build()));

        var request = new PerfilRequest("Tosador", List.of(1L));

        assertThatThrownBy(() -> service.criar(request)).isInstanceOf(BusinessException.class);
        verify(perfilRepository, never()).save(any());
    }

    @Test
    void criar_comPermissaoInexistente_deveLancarBusinessException() {
        when(perfilRepository.findByNomeAndEmpresaId(any(), any())).thenReturn(Optional.empty());
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(Empresa.builder().id(EMPRESA_ID).build());
        when(permissaoRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(
                Permissao.builder().id(1L).codigo("A").descricao("d").modulo("M").build()
        ));

        var request = new PerfilRequest("Recepção", List.of(1L, 2L));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválidas");
    }

    @Test
    void atualizar_perfilSistema_deveLancarBusinessException() {
        Perfil perfilSistema = Perfil.builder().id(1L).nome("Proprietário").sistema(true).empresa(Empresa.builder().id(EMPRESA_ID).build()).build();
        when(perfilRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(perfilSistema));

        assertThatThrownBy(() -> service.atualizar(1L, new PerfilRequest("Novo nome", List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não podem ser editados");

        verify(perfilRepository, never()).save(any());
    }

    @Test
    void remover_perfilSistema_deveLancarBusinessException() {
        Perfil perfilSistema = Perfil.builder().id(1L).nome("Auxiliar").sistema(true).build();
        when(perfilRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(perfilSistema));

        assertThatThrownBy(() -> service.remover(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não podem ser removidos");

        verify(perfilRepository, never()).delete(any());
    }

    @Test
    void remover_perfilComUsuariosVinculados_deveLancarBusinessException() {
        Perfil perfilCustom = Perfil.builder().id(6L).nome("Caixa").sistema(false).build();
        when(perfilRepository.findByIdAndEmpresaId(6L, EMPRESA_ID)).thenReturn(Optional.of(perfilCustom));
        when(usuarioRepository.existsByPerfilId(6L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(6L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("usuários vinculados");

        verify(perfilRepository, never()).delete(any());
    }

    @Test
    void remover_perfilCustomSemUsuarios_deveRemover() {
        Perfil perfilCustom = Perfil.builder().id(6L).nome("Caixa").sistema(false).build();
        when(perfilRepository.findByIdAndEmpresaId(6L, EMPRESA_ID)).thenReturn(Optional.of(perfilCustom));
        when(usuarioRepository.existsByPerfilId(6L)).thenReturn(false);

        service.remover(6L);

        verify(perfilRepository).delete(perfilCustom);
    }
}

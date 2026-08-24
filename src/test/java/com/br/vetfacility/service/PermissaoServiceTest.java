package com.br.vetfacility.service;

import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.dto.perfil.PermissaoRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PermissaoServiceTest {

    @Mock private PermissaoRepository permissaoRepository;
    @Mock private PerfilRepository perfilRepository;

    private PermissaoService service;

    private void construir() {
        service = new PermissaoService(permissaoRepository, perfilRepository);
    }

    @Test
    void criar_comCodigoJaExistente_deveLancarBusinessException() {
        construir();
        when(permissaoRepository.existsByCodigoIgnoreCase("RELATORIO_VISUALIZAR")).thenReturn(true);

        var request = new PermissaoRequest("relatorio_visualizar", "Ver relatórios", "relatorio");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RELATORIO_VISUALIZAR");

        verify(permissaoRepository, never()).save(any());
    }

    @Test
    void criar_normalizaCodigoEModuloParaMaiusculo() {
        construir();
        when(permissaoRepository.existsByCodigoIgnoreCase("RELATORIO_VISUALIZAR")).thenReturn(false);
        when(permissaoRepository.save(any(Permissao.class))).thenAnswer(inv -> {
            Permissao p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var request = new PermissaoRequest("relatorio_visualizar", "  Ver relatórios  ", "relatorio");
        var response = service.criar(request);

        assertThat(response.codigo()).isEqualTo("RELATORIO_VISUALIZAR");
        assertThat(response.modulo()).isEqualTo("RELATORIO");
        assertThat(response.descricao()).isEqualTo("Ver relatórios");
    }

    @Test
    void remover_permissaoInexistente_deveLancarResourceNotFound() {
        construir();
        when(permissaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void remover_permissaoAtribuidaAPerfil_deveLancarBusinessException() {
        construir();
        Permissao permissao = Permissao.builder().id(5L).codigo("CLIENTE_VISUALIZAR").descricao("d").modulo("CLIENTES").build();
        when(permissaoRepository.findById(5L)).thenReturn(Optional.of(permissao));
        when(perfilRepository.existsByPermissoesId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("atribuída");

        verify(permissaoRepository, never()).delete(any());
    }

    @Test
    void remover_permissaoNaoAtribuida_deveRemover() {
        construir();
        Permissao permissao = Permissao.builder().id(5L).codigo("X").descricao("d").modulo("M").build();
        when(permissaoRepository.findById(5L)).thenReturn(Optional.of(permissao));
        when(perfilRepository.existsByPermissoesId(5L)).thenReturn(false);

        service.remover(5L);

        verify(permissaoRepository).delete(permissao);
    }
}

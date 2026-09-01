package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.dto.empresa.AtualizarLogotipoRequest;
import com.br.vetfacility.dto.empresa.AtualizarNomeEmpresaRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.repository.EmpresaRepository;
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
 * Personalização (PER-01 a PER-03): nome do negócio e logotipo configuráveis por empresa.
 */
@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock private EmpresaRepository empresaRepository;

    private EmpresaService service;

    @BeforeEach
    void setUp() {
        service = new EmpresaService(empresaRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(1L, EMPRESA_ID, List.of("EMPRESA_PERSONALIZAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    private Empresa empresa() {
        return Empresa.builder().id(EMPRESA_ID).nome("Pet Smack").build();
    }

    @Test
    void atualizarNome_devePersistirNovoNome() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa()));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.atualizarNome(new AtualizarNomeEmpresaRequest("Centro Estético Pet Smack"));

        assertThat(response.nome()).isEqualTo("Centro Estético Pet Smack");
    }

    @Test
    void atualizarLogotipo_comPngValido_devePersistir() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa()));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AtualizarLogotipoRequest("data:image/png;base64,aGVsbG8=");
        var response = service.atualizarLogotipo(request);

        assertThat(response.logotipoUrl()).isEqualTo("data:image/png;base64,aGVsbG8=");
    }

    @Test
    void atualizarLogotipo_comFormatoInvalido_deveLancarBusinessException() {
        var request = new AtualizarLogotipoRequest("data:application/pdf;base64,aGVsbG8=");

        assertThatThrownBy(() -> service.atualizarLogotipo(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PNG ou JPG");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void atualizarLogotipo_maiorQueLimite_deveLancarBusinessException() {
        String base64Enorme = "data:image/png;base64," + "A".repeat(3_000_000);
        var request = new AtualizarLogotipoRequest(base64Enorme);

        assertThatThrownBy(() -> service.atualizarLogotipo(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("2MB");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void removerLogotipo_deveLimparCampo() {
        Empresa empresa = empresa();
        empresa.setLogotipoUrl("data:image/png;base64,aGVsbG8=");
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.removerLogotipo();

        assertThat(response.logotipoUrl()).isNull();
    }
}

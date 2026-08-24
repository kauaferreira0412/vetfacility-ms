package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Produto;
import com.br.vetfacility.dto.produto.ProdutoRequest;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.ProdutoRepository;
import com.br.vetfacility.support.TestSecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Controle de estoque (EST-01/EST-04): unidade padrão "un" quando não informada, e o indicador de
 * estoque baixo (quantidade <= mínima) que alimenta o alerta do dashboard.
 */
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock private ProdutoRepository produtoRepository;
    @Mock private EmpresaRepository empresaRepository;

    private ProdutoService service;

    @BeforeEach
    void setUp() {
        service = new ProdutoService(produtoRepository, empresaRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(1L, EMPRESA_ID, List.of("PRODUTO_GERENCIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    @Test
    void criar_semUnidadeInformada_devePreencherComUn() {
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(Empresa.builder().id(EMPRESA_ID).build());
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new ProdutoRequest("Shampoo", new BigDecimal("10"), new BigDecimal("2"), "  ");
        var response = service.criar(request);

        assertThat(response.unidade()).isEqualTo("un");
    }

    @Test
    void criar_comEstoqueIgualAoMinimo_deveMarcarComoEstoqueBaixo() {
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(Empresa.builder().id(EMPRESA_ID).build());
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new ProdutoRequest("Condicionador", new BigDecimal("2"), new BigDecimal("2"), "un");
        var response = service.criar(request);

        assertThat(response.estoqueBaixo()).isTrue();
    }

    @Test
    void criar_comEstoqueAcimaDoMinimo_naoDeveMarcarComoEstoqueBaixo() {
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(Empresa.builder().id(EMPRESA_ID).build());
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new ProdutoRequest("Perfume", new BigDecimal("5"), new BigDecimal("2"), "un");
        var response = service.criar(request);

        assertThat(response.estoqueBaixo()).isFalse();
    }

    @Test
    void remover_produtoDeOutraEmpresa_deveLancarResourceNotFound() {
        when(produtoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(produtoRepository, never()).delete(any());
    }
}

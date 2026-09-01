package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Produto;
import com.br.vetfacility.domain.Servico;
import com.br.vetfacility.dto.servico.AtualizarProdutosPadraoRequest;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.ProdutoRepository;
import com.br.vetfacility.repository.ServicoRepository;
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
 * Vínculo produto-serviço (EST-02/EST-03): produtos e quantidades padrão de cada tipo de
 * serviço, usados para pré-preencher o início do atendimento (ver AgendamentoServiceTest).
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock private ServicoRepository servicoRepository;
    @Mock private ProdutoRepository produtoRepository;

    private ServicoService service;

    @BeforeEach
    void setUp() {
        service = new ServicoService(servicoRepository, produtoRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(1L, EMPRESA_ID, List.of("PRODUTO_GERENCIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    private Empresa empresa() {
        return Empresa.builder().id(EMPRESA_ID).nome("Pet Smack").build();
    }

    @Test
    void atualizarProdutosPadrao_devePersistirListaInformada() {
        Servico servico = Servico.builder().id(1L).nome("Banho").duracaoMin(60).empresa(empresa()).build();
        Produto produto = Produto.builder().id(1L).nome("Shampoo").quantidadeEstoque(new BigDecimal("5"))
                .quantidadeMinima(new BigDecimal("1")).unidade("un").empresa(empresa()).build();

        when(servicoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(servico));
        when(produtoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(produto));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AtualizarProdutosPadraoRequest(
                List.of(new AtualizarProdutosPadraoRequest.ProdutoPadrao(1L, new BigDecimal("2.00"))));

        var response = service.atualizarProdutosPadrao(1L, request);

        assertThat(response.produtosPadrao()).hasSize(1);
        assertThat(response.produtosPadrao().get(0).produtoNome()).isEqualTo("Shampoo");
        assertThat(response.produtosPadrao().get(0).quantidadePadrao()).isEqualByComparingTo("2.00");
    }

    @Test
    void atualizarProdutosPadrao_servicoDeOutraEmpresa_deveLancarResourceNotFound() {
        when(servicoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

        var request = new AtualizarProdutosPadraoRequest(List.of());

        assertThatThrownBy(() -> service.atualizarProdutosPadrao(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(servicoRepository, never()).save(any());
    }
}

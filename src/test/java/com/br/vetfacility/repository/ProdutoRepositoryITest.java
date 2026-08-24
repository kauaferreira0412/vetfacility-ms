package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProdutoRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void findAllByEmpresaIdOrderByNomeAsc_deveOrdenarEPreservarPrecisaoDecimal() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        em.persistAndFlush(Produto.builder().nome("Xampu").quantidadeEstoque(new BigDecimal("10.50"))
                .quantidadeMinima(new BigDecimal("2.00")).unidade("un").empresa(empresa).build());
        em.persistAndFlush(Produto.builder().nome("Condicionador").quantidadeEstoque(new BigDecimal("3.25"))
                .quantidadeMinima(new BigDecimal("1.00")).unidade("un").empresa(empresa).build());

        List<Produto> resultado = produtoRepository.findAllByEmpresaIdOrderByNomeAsc(empresa.getId());

        assertThat(resultado).extracting(Produto::getNome).containsExactly("Condicionador", "Xampu");
        assertThat(resultado.get(0).getQuantidadeEstoque()).isEqualByComparingTo("3.25");
    }

    @Test
    void isEstoqueBaixo_deveConsiderarEstoqueAbaixoOuIgualAoMinimo() {
        Empresa empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Produto baixo = em.persistAndFlush(Produto.builder().nome("Perfume").quantidadeEstoque(new BigDecimal("1"))
                .quantidadeMinima(new BigDecimal("2")).unidade("un").empresa(empresa).build());
        Produto ok = em.persistAndFlush(Produto.builder().nome("Shampoo").quantidadeEstoque(new BigDecimal("5"))
                .quantidadeMinima(new BigDecimal("2")).unidade("un").empresa(empresa).build());

        Produto baixoCarregado = produtoRepository.findByIdAndEmpresaId(baixo.getId(), empresa.getId()).orElseThrow();
        Produto okCarregado = produtoRepository.findByIdAndEmpresaId(ok.getId(), empresa.getId()).orElseThrow();

        assertThat(baixoCarregado.isEstoqueBaixo()).isTrue();
        assertThat(okCarregado.isEstoqueBaixo()).isFalse();
    }
}

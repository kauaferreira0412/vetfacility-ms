package com.br.vetfacility.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produto", indexes = {
        @Index(name = "idx_produto_empresa", columnList = "empresa_id"),
        @Index(name = "idx_produto_nome", columnList = "nome"),
        @Index(name = "idx_produto_quantidade_estoque", columnList = "quantidade_estoque"),
        @Index(name = "idx_produto_quantidade_minima", columnList = "quantidade_minima"),
        @Index(name = "idx_produto_unidade", columnList = "unidade"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "quantidade_estoque", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeEstoque;

    @Column(name = "quantidade_minima", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadeMinima;

    @Column(nullable = false, length = 20)
    private String unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Transient
    public boolean isEstoqueBaixo() {
        return quantidadeEstoque != null && quantidadeMinima != null
                && quantidadeEstoque.compareTo(quantidadeMinima) <= 0;
    }
}

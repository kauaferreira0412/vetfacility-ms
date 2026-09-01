package com.br.vetfacility.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "servico_produto", indexes = {
        @Index(name = "idx_servico_produto_produto", columnList = "produto_id"),
        @Index(name = "idx_servico_produto_quantidade", columnList = "quantidade_padrao"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoProduto {

    @EmbeddedId
    private ServicoProdutoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("servicoId")
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produtoId")
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "quantidade_padrao", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidadePadrao;
}

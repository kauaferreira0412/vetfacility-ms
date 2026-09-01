package com.br.vetfacility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Produto que o executor sinaliza, no momento em que inicia o atendimento, que pretende usar - só
 * informativo (mostrado durante o "Em atendimento"), sem efeito nenhum sobre o estoque. A baixa real
 * de estoque continua acontecendo só na conclusão do atendimento (ver AgendamentoProduto), reaproveitando
 * esta mesma lista como sugestão automática se nenhuma nova lista for informada nesse momento.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoPlanejado {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "produto_nome", nullable = false, length = 120)
    private String produtoNome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;
}

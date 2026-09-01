package com.br.vetfacility.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoProdutoId implements Serializable {

    @Column(name = "agendamento_id")
    private Long agendamentoId;

    @Column(name = "produto_id")
    private Long produtoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgendamentoProdutoId that)) return false;
        return Objects.equals(agendamentoId, that.agendamentoId) && Objects.equals(produtoId, that.produtoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agendamentoId, produtoId);
    }
}

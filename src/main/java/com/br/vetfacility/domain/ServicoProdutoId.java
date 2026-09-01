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
public class ServicoProdutoId implements Serializable {

    @Column(name = "servico_id")
    private Long servicoId;

    @Column(name = "produto_id")
    private Long produtoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicoProdutoId that)) return false;
        return Objects.equals(servicoId, that.servicoId) && Objects.equals(produtoId, that.produtoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicoId, produtoId);
    }
}

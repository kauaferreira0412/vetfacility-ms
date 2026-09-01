package com.br.vetfacility.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servico", indexes = {
        @Index(name = "idx_servico_nome", columnList = "nome"),
        @Index(name = "idx_servico_duracao_min", columnList = "duracao_min"),
        @Index(name = "idx_servico_empresa", columnList = "empresa_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "duracao_min", nullable = false)
    private Integer duracaoMin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Builder.Default
    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoProduto> produtosPadrao = new ArrayList<>();
}

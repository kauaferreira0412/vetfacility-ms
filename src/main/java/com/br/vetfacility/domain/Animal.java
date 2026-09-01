package com.br.vetfacility.domain;

import com.br.vetfacility.enums.Porte;
import com.br.vetfacility.enums.Sexo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "animal", indexes = {
        @Index(name = "idx_animal_empresa", columnList = "empresa_id"),
        @Index(name = "idx_animal_nome", columnList = "nome"),
        @Index(name = "idx_animal_especie", columnList = "especie"),
        @Index(name = "idx_animal_porte", columnList = "porte"),
        @Index(name = "idx_animal_cliente", columnList = "cliente_id"),
        @Index(name = "idx_animal_raca", columnList = "raca"),
        @Index(name = "idx_animal_sexo", columnList = "sexo"),
        @Index(name = "idx_animal_data_nascimento", columnList = "data_nascimento"),
        @Index(name = "idx_animal_peso", columnList = "peso"),
        @Index(name = "idx_animal_cor_pelagem", columnList = "cor_pelagem"),
        @Index(name = "idx_animal_observacoes", columnList = "observacoes"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 60)
    private String especie;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Porte porte;

    @Column(length = 80)
    private String raca;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Sexo sexo;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(precision = 6, scale = 2)
    private BigDecimal peso;

    @Column(name = "cor_pelagem", length = 60)
    private String corPelagem;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}

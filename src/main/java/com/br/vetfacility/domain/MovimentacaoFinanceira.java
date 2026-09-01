package com.br.vetfacility.domain;

import com.br.vetfacility.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_financeira", indexes = {
        @Index(name = "idx_movimentacao_empresa", columnList = "empresa_id"),
        @Index(name = "idx_movimentacao_empresa_data", columnList = "empresa_id, data"),
        @Index(name = "idx_movimentacao_tipo", columnList = "tipo"),
        @Index(name = "idx_movimentacao_valor", columnList = "valor"),
        @Index(name = "idx_movimentacao_data", columnList = "data"),
        @Index(name = "idx_movimentacao_descricao", columnList = "descricao"),
        @Index(name = "idx_movimentacao_agendamento", columnList = "agendamento_id"),
        @Index(name = "idx_movimentacao_criado_em", columnList = "criado_em"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 200)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (data == null) {
            data = LocalDate.now();
        }
    }
}

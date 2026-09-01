package com.br.vetfacility.domain;

import com.br.vetfacility.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agendamento", indexes = {
        @Index(name = "idx_agendamento_empresa", columnList = "empresa_id"),
        @Index(name = "idx_agendamento_data", columnList = "empresa_id, data_hora"),
        @Index(name = "idx_agendamento_animal", columnList = "animal_id"),
        @Index(name = "idx_agendamento_servico", columnList = "servico_id"),
        @Index(name = "idx_agendamento_usuario", columnList = "usuario_id"),
        @Index(name = "idx_agendamento_data_hora", columnList = "data_hora"),
        @Index(name = "idx_agendamento_status", columnList = "status"),
        @Index(name = "idx_agendamento_observacao", columnList = "observacao"),
        @Index(name = "idx_agendamento_motivo_cancelamento", columnList = "motivo_cancelamento"),
        @Index(name = "idx_agendamento_iniciado_em", columnList = "iniciado_em"),
        @Index(name = "idx_agendamento_criado_em", columnList = "criado_em"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(length = 500)
    private String observacao;

    @Column(name = "motivo_cancelamento", length = 300)
    private String motivoCancelamento;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Builder.Default
    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgendamentoProduto> produtosConsumidos = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "agendamento_produto_planejado", joinColumns = @JoinColumn(name = "agendamento_id"), indexes = {
            @Index(name = "idx_agendamento_produto_planejado_produto", columnList = "produto_id"),
            @Index(name = "idx_agendamento_produto_planejado_produto_nome", columnList = "produto_nome"),
            @Index(name = "idx_agendamento_produto_planejado_quantidade", columnList = "quantidade"),
    })
    private List<ProdutoPlanejado> produtosPlanejados = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusAgendamento.AGENDADO;
        }
    }
}

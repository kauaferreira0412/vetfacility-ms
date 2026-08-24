package com.br.vetfacility.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SqlResultSetMapping(
        name = "UsuarioComPerfilMapping",
        entities = {
                @EntityResult(entityClass = Usuario.class, fields = {
                        @FieldResult(name = "id", column = "id"),
                        @FieldResult(name = "nome", column = "nome"),
                        @FieldResult(name = "email", column = "email"),
                        @FieldResult(name = "senhaHash", column = "senha_hash"),
                        @FieldResult(name = "ativo", column = "ativo"),
                        @FieldResult(name = "empresa", column = "empresa_id"),
                        @FieldResult(name = "perfil", column = "perfil_id"),
                        @FieldResult(name = "criadoEm", column = "criado_em"),
                }),
                @EntityResult(entityClass = Perfil.class, fields = {
                        @FieldResult(name = "id", column = "perfil_pk"),
                        @FieldResult(name = "nome", column = "perfil_nome"),
                        @FieldResult(name = "empresa", column = "perfil_empresa_id"),
                        @FieldResult(name = "sistema", column = "perfil_sistema"),
                        @FieldResult(name = "criadoEm", column = "perfil_criado_em"),
                })
        }
)
@NamedNativeQuery(
        name = "Usuario.findAllByEmpresaId",
        query = """
                SELECT u.id AS id, u.nome AS nome, u.email AS email, u.senha_hash AS senha_hash,
                       u.ativo AS ativo, u.empresa_id AS empresa_id, u.perfil_id AS perfil_id, u.criado_em AS criado_em,
                       p.id AS perfil_pk, p.nome AS perfil_nome, p.empresa_id AS perfil_empresa_id,
                       p.sistema AS perfil_sistema, p.criado_em AS perfil_criado_em
                FROM usuario u
                JOIN perfil p ON p.id = u.perfil_id
                WHERE u.empresa_id = :empresaId
                ORDER BY u.nome
                """,
        resultSetMapping = "UsuarioComPerfilMapping"
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}

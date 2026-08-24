package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    @Query(value = "SELECT * FROM perfil WHERE empresa_id = :empresaId", nativeQuery = true)
    List<Perfil> findAllByEmpresaId(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM perfil WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Perfil> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM perfil WHERE nome = :nome AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Perfil> findByNomeAndEmpresaId(@Param("nome") String nome, @Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM perfil WHERE nome = :nome AND empresa_id IS NULL", nativeQuery = true)
    Optional<Perfil> findByNomeAndEmpresaIsNull(@Param("nome") String nome);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM perfil_permissao WHERE permissao_id = :permissaoId)", nativeQuery = true)
    boolean existsByPermissoesId(@Param("permissaoId") Long permissaoId);
}

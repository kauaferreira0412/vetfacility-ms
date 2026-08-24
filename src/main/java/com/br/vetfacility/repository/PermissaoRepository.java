package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    @Query(value = "SELECT * FROM permissao WHERE id IN (:ids)", nativeQuery = true)
    List<Permissao> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query(value = "SELECT * FROM permissao WHERE codigo IN (:codigos)", nativeQuery = true)
    List<Permissao> findAllByCodigoIn(@Param("codigos") List<String> codigos);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM permissao WHERE LOWER(codigo) = LOWER(:codigo))", nativeQuery = true)
    boolean existsByCodigoIgnoreCase(@Param("codigo") String codigo);

    @Query(value = "SELECT * FROM permissao ORDER BY modulo ASC, codigo ASC", nativeQuery = true)
    List<Permissao> findAllByOrderByModuloAscCodigoAsc();
}

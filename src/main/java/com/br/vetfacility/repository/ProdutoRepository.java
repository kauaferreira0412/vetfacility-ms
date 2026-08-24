package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query(value = "SELECT * FROM produto WHERE empresa_id = :empresaId ORDER BY nome ASC", nativeQuery = true)
    List<Produto> findAllByEmpresaIdOrderByNomeAsc(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM produto WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Produto> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

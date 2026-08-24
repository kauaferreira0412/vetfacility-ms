package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query(value = "SELECT * FROM servico WHERE empresa_id = :empresaId", nativeQuery = true)
    List<Servico> findAllByEmpresaId(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM servico WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Servico> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

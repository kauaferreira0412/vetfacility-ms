package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query(value = "SELECT * FROM cliente WHERE empresa_id = :empresaId ORDER BY nome ASC", nativeQuery = true)
    List<Cliente> findAllByEmpresaIdOrderByNomeAsc(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM cliente WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Cliente> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

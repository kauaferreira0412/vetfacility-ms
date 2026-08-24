package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query(value = "SELECT * FROM animal WHERE empresa_id = :empresaId", nativeQuery = true)
    List<Animal> findAllByEmpresaId(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM animal WHERE cliente_id = :clienteId AND empresa_id = :empresaId", nativeQuery = true)
    List<Animal> findAllByClienteIdAndEmpresaId(@Param("clienteId") Long clienteId, @Param("empresaId") Long empresaId);

    @Query(value = "SELECT * FROM animal WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Animal> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

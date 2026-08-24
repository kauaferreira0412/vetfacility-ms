package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    @Query(value = "SELECT * FROM empresa ORDER BY nome ASC", nativeQuery = true)
    List<Empresa> findAllByOrderByNomeAsc();
}

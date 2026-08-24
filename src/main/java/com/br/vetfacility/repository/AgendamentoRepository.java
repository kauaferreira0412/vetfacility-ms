package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query(value = "SELECT * FROM agendamento WHERE empresa_id = :empresaId ORDER BY data_hora ASC", nativeQuery = true)
    List<Agendamento> findAllByEmpresaIdOrderByDataHoraAsc(@Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM agendamento
            WHERE empresa_id = :empresaId AND data_hora BETWEEN :inicio AND :fim
            ORDER BY data_hora ASC
            """, nativeQuery = true)
    List<Agendamento> findAllByEmpresaIdAndDataHoraBetweenOrderByDataHoraAsc(
            @Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query(value = "SELECT * FROM agendamento WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<Agendamento> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query(value = """
            SELECT * FROM agendamento
            WHERE empresa_id = :empresaId
              AND status <> 'CANCELADO'
              AND data_hora BETWEEN :inicioDoDia AND :fimDoDia
            """, nativeQuery = true)
    List<Agendamento> findAtivosNoIntervalo(@Param("empresaId") Long empresaId,
                                             @Param("inicioDoDia") LocalDateTime inicioDoDia,
                                             @Param("fimDoDia") LocalDateTime fimDoDia);
}

package com.br.vetfacility.repository;

import com.br.vetfacility.domain.MovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Long> {

    @Query(value = """
            SELECT * FROM movimentacao_financeira
            WHERE empresa_id = :empresaId AND data BETWEEN :de AND :ate
            ORDER BY data DESC, criado_em DESC
            """, nativeQuery = true)
    List<MovimentacaoFinanceira> findAllByEmpresaIdAndDataBetween(
            @Param("empresaId") Long empresaId, @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query(value = "SELECT * FROM movimentacao_financeira WHERE id = :id AND empresa_id = :empresaId", nativeQuery = true)
    Optional<MovimentacaoFinanceira> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

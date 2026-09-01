package com.br.vetfacility.repository;

import com.br.vetfacility.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query(value = "SELECT * FROM usuario WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    Optional<Usuario> findByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM usuario WHERE LOWER(email) = LOWER(:email))", nativeQuery = true)
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM usuario WHERE perfil_id = :perfilId)", nativeQuery = true)
    boolean existsByPerfilId(@Param("perfilId") Long perfilId);

    List<Usuario> findAllByEmpresaId(@Param("empresaId") Long empresaId);

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);
}

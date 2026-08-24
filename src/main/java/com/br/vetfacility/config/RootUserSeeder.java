package com.br.vetfacility.config;

import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RootUserSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.root.email}")
    private String rootEmail;

    @Value("${app.root.password}")
    private String rootPassword;

    public RootUserSeeder(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmailIgnoreCase(rootEmail)) {
            return;
        }

        Perfil perfilRoot = perfilRepository.findByNomeAndEmpresaIsNull("Administrador da Plataforma")
                .orElseThrow(() -> new IllegalStateException(
                        "Perfil global do ROOT não encontrado. Verifique a migration V2__perfis_e_permissoes.sql."));

        Usuario root = Usuario.builder()
                .nome("Administrador da Plataforma")
                .email(rootEmail)
                .senhaHash(passwordEncoder.encode(rootPassword))
                .ativo(true)
                .empresa(null)
                .perfil(perfilRoot)
                .build();

        usuarioRepository.save(root);
    }
}

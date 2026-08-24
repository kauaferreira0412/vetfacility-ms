package com.br.vetfacility.config;

import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("test")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestGlobalRootPerfilSeeder implements CommandLineRunner {

    private static final List<String> CODIGOS_PERMISSOES = List.of(
            "EMPRESA_GERENCIAR", "PERFIL_GERENCIAR", "USUARIO_VISUALIZAR", "USUARIO_GERENCIAR",
            "CLIENTE_VISUALIZAR", "CLIENTE_GERENCIAR", "ANIMAL_VISUALIZAR", "ANIMAL_GERENCIAR",
            "SERVICO_VISUALIZAR", "AGENDAMENTO_VISUALIZAR", "AGENDAMENTO_CRIAR", "AGENDAMENTO_CANCELAR",
            "AGENDAMENTO_CONCLUIR", "PRODUTO_VISUALIZAR", "PRODUTO_GERENCIAR"
    );

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    public TestGlobalRootPerfilSeeder(PerfilRepository perfilRepository, PermissaoRepository permissaoRepository) {
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (perfilRepository.findByNomeAndEmpresaIsNull("Administrador da Plataforma").isPresent()) {
            return;
        }

        for (String codigo : CODIGOS_PERMISSOES) {
            if (!permissaoRepository.existsByCodigoIgnoreCase(codigo)) {
                permissaoRepository.save(Permissao.builder()
                        .codigo(codigo)
                        .descricao("Permissão de teste: " + codigo)
                        .modulo(moduloDoCodigo(codigo))
                        .build());
            }
        }

        Permissao empresaGerenciar = permissaoRepository.findAllByCodigoIn(List.of("EMPRESA_GERENCIAR")).get(0);
        Set<Permissao> permissoesRoot = new HashSet<>(Set.of(empresaGerenciar));

        perfilRepository.save(Perfil.builder()
                .nome("Administrador da Plataforma")
                .empresa(null)
                .sistema(true)
                .permissoes(permissoesRoot)
                .build());
    }

    private String moduloDoCodigo(String codigo) {
        if (codigo.equals("EMPRESA_GERENCIAR")) return "PLATAFORMA";
        if (codigo.startsWith("PERFIL") || codigo.startsWith("USUARIO")) return "ACESSO";
        if (codigo.startsWith("CLIENTE") || codigo.startsWith("ANIMAL")) return "CLIENTES";
        if (codigo.startsWith("SERVICO") || codigo.startsWith("AGENDAMENTO")) return "AGENDAMENTO";
        if (codigo.startsWith("PRODUTO")) return "ESTOQUE";
        return "GERAL";
    }
}

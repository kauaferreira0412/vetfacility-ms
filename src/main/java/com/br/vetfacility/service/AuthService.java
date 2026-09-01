package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.domain.Servico;
import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.dto.auth.ConvidarUsuarioRequest;
import com.br.vetfacility.dto.auth.LoginRequest;
import com.br.vetfacility.dto.auth.RegisterEmpresaRequest;
import com.br.vetfacility.dto.auth.TokenResponse;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import com.br.vetfacility.repository.ServicoRepository;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.security.JwtService;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private static final List<String> SERVICOS_PADRAO = List.of("Banho", "Banho e Tosa", "Banho Terapêutico");

    private static final List<String> PERMISSOES_PROPRIETARIO = List.of(
            "PERFIL_GERENCIAR", "USUARIO_VISUALIZAR", "USUARIO_GERENCIAR",
            "CLIENTE_VISUALIZAR", "CLIENTE_GERENCIAR", "ANIMAL_VISUALIZAR", "ANIMAL_GERENCIAR",
            "SERVICO_VISUALIZAR", "AGENDAMENTO_VISUALIZAR", "AGENDAMENTO_CRIAR",
            "AGENDAMENTO_CANCELAR", "AGENDAMENTO_CONCLUIR", "PRODUTO_VISUALIZAR", "PRODUTO_GERENCIAR",
            "FINANCEIRO_VISUALIZAR", "FINANCEIRO_GERENCIAR", "EMPRESA_PERSONALIZAR"
    );

    private static final List<String> PERMISSOES_AUXILIAR = List.of(
            "USUARIO_VISUALIZAR", "CLIENTE_VISUALIZAR", "CLIENTE_GERENCIAR", "ANIMAL_VISUALIZAR", "ANIMAL_GERENCIAR",
            "SERVICO_VISUALIZAR", "AGENDAMENTO_VISUALIZAR", "AGENDAMENTO_CRIAR",
            "AGENDAMENTO_CANCELAR", "AGENDAMENTO_CONCLUIR", "PRODUTO_VISUALIZAR", "PRODUTO_GERENCIAR",
            "FINANCEIRO_VISUALIZAR"
    );

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
                        ServicoRepository servicoRepository, PerfilRepository perfilRepository,
                        PermissaoRepository permissaoRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse registrarEmpresa(RegisterEmpresaRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nome(request.nomeEmpresa())
                .build());

        SERVICOS_PADRAO.forEach(nomeServico -> servicoRepository.save(Servico.builder()
                .nome(nomeServico)
                .duracaoMin(60)
                .empresa(empresa)
                .build()));

        Set<Permissao> permissoesProprietario = new HashSet<>(permissaoRepository.findAllByCodigoIn(PERMISSOES_PROPRIETARIO));
        Set<Permissao> permissoesAuxiliar = new HashSet<>(permissaoRepository.findAllByCodigoIn(PERMISSOES_AUXILIAR));

        Perfil perfilProprietario = perfilRepository.save(Perfil.builder()
                .nome("Proprietário")
                .empresa(empresa)
                .sistema(true)
                .permissoes(permissoesProprietario)
                .build());

        perfilRepository.save(Perfil.builder()
                .nome("Auxiliar")
                .empresa(empresa)
                .sistema(true)
                .permissoes(permissoesAuxiliar)
                .build());

        Usuario usuario = Usuario.builder()
                .nome(request.nomeUsuario())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .empresa(empresa)
                .perfil(perfilProprietario)
                .build();
        usuario = usuarioRepository.save(usuario);

        return montarToken(usuario);
    }

    @Transactional
    public TokenResponse convidarUsuario(ConvidarUsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }
        Long empresaId = SecurityUtils.currentEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa do usuário autenticado não encontrada."));
        Perfil perfil = perfilRepository.findByIdAndEmpresaId(request.perfilId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado nesta empresa."));

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .empresa(empresa)
                .perfil(perfil)
                .build();
        usuario = usuarioRepository.save(usuario);

        return montarToken(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));

        if (!usuario.isAtivo() || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        return montarToken(usuario);
    }

    private TokenResponse montarToken(Usuario usuario) {
        String token = jwtService.gerarToken(usuario);
        boolean root = usuario.getEmpresa() == null;
        var resumo = new TokenResponse.UsuarioResumo(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), root,
                root ? null : usuario.getEmpresa().getId(),
                root ? null : usuario.getEmpresa().getNome(),
                usuario.getPerfil().getId(), usuario.getPerfil().getNome(),
                jwtService.resolverPermissoes(usuario)
        );
        return new TokenResponse(token, "Bearer", jwtService.expirationSeconds(), resumo);
    }
}

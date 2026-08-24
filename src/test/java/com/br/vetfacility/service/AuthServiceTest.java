package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.dto.auth.LoginRequest;
import com.br.vetfacility.dto.auth.RegisterEmpresaRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import com.br.vetfacility.repository.ServicoRepository;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private PermissaoRepository permissaoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService service;

    private void construir() {
        service = new AuthService(empresaRepository, usuarioRepository, servicoRepository,
                perfilRepository, permissaoRepository, passwordEncoder, jwtService);
    }

    private Usuario usuarioComSenha(String hash) {
        Empresa empresa = Empresa.builder().id(1L).nome("Pet Smack").build();
        Perfil perfil = Perfil.builder().id(1L).nome("Proprietário").sistema(true).build();
        return Usuario.builder().id(1L).nome("Francisco").email("francisco@teste.com")
                .senhaHash(hash).ativo(true).empresa(empresa).perfil(perfil).build();
    }

    @Test
    void login_comCredenciaisCorretas_devePermitirEntrada() {
        construir();
        Usuario usuario = usuarioComSenha("hash-valido");
        when(usuarioRepository.findByEmailIgnoreCase("francisco@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash-valido")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-fake");
        when(jwtService.expirationSeconds()).thenReturn(3600L);
        when(jwtService.resolverPermissoes(usuario)).thenReturn(List.of("AGENDAMENTO_VISUALIZAR"));

        var response = service.login(new LoginRequest("francisco@teste.com", "senha123"));

        assertThat(response.accessToken()).isEqualTo("jwt-fake");
        assertThat(response.usuario().nome()).isEqualTo("Francisco");
        assertThat(response.usuario().root()).isFalse();
        assertThat(response.usuario().empresaId()).isEqualTo(1L);
    }

    @Test
    void login_comSenhaErrada_deveLancarBadCredentials() {
        construir();
        Usuario usuario = usuarioComSenha("hash-valido");
        when(usuarioRepository.findByEmailIgnoreCase("francisco@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "hash-valido")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("francisco@teste.com", "senha-errada")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_comEmailInexistente_deveLancarBadCredentials() {
        construir();
        when(usuarioRepository.findByEmailIgnoreCase("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("naoexiste@teste.com", "qualquer")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_comUsuarioInativo_deveLancarBadCredentials() {
        construir();
        Usuario usuario = usuarioComSenha("hash-valido");
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("francisco@teste.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.login(new LoginRequest("francisco@teste.com", "senha123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void registrarEmpresa_comEmailJaCadastrado_deveLancarBusinessException() {
        construir();
        when(usuarioRepository.existsByEmailIgnoreCase("ja-existe@teste.com")).thenReturn(true);

        var request = new RegisterEmpresaRequest("Nova Empresa", "Dono", "ja-existe@teste.com", "senha123");

        assertThatThrownBy(() -> service.registrarEmpresa(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um usuário");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void registrarEmpresa_comDadosValidos_deveCriarEmpresaServicosPadraoEDoisPerfis() {
        construir();
        when(usuarioRepository.existsByEmailIgnoreCase("novo@teste.com")).thenReturn(false);
        Empresa empresaSalva = Empresa.builder().id(2L).nome("Novo Petshop").build();
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaSalva);
        when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(permissaoRepository.findAllByCodigoIn(any())).thenReturn(List.of(
                Permissao.builder().id(1L).codigo("CLIENTE_VISUALIZAR").descricao("d").modulo("CLIENTES").build()
        ));
        Perfil perfilProprietario = Perfil.builder().id(10L).nome("Proprietário").empresa(empresaSalva).sistema(true).build();
        when(perfilRepository.save(any(Perfil.class)))
                .thenReturn(perfilProprietario)
                .thenReturn(Perfil.builder().id(11L).nome("Auxiliar").empresa(empresaSalva).sistema(true).build());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });
        when(passwordEncoder.encode("senha123")).thenReturn("hash-gerado");
        when(jwtService.gerarToken(any())).thenReturn("jwt-fake");
        when(jwtService.expirationSeconds()).thenReturn(3600L);
        when(jwtService.resolverPermissoes(any())).thenReturn(List.of("CLIENTE_VISUALIZAR"));

        var request = new RegisterEmpresaRequest("Novo Petshop", "Dono Novo", "novo@teste.com", "senha123");
        var response = service.registrarEmpresa(request);

        assertThat(response.usuario().empresaNome()).isEqualTo("Novo Petshop");
        verify(servicoRepository, times(3)).save(any()); // Banho, Banho e Tosa, Banho Terapêutico
        verify(perfilRepository, times(2)).save(any()); // Proprietário + Auxiliar
    }
}

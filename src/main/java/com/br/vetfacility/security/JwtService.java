package com.br.vetfacility.security;

import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.repository.PermissaoRepository;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final RsaKeyProperties properties;
    private final PermissaoRepository permissaoRepository;

    public JwtService(JwtEncoder jwtEncoder, RsaKeyProperties properties, PermissaoRepository permissaoRepository) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.permissaoRepository = permissaoRepository;
    }

        public List<String> resolverPermissoes(Usuario usuario) {
        boolean root = usuario.getEmpresa() == null;
        return root
                ? permissaoRepository.findAll().stream().map(p -> p.getCodigo()).sorted().toList()
                : usuario.getPerfil().getPermissoes().stream().map(p -> p.getCodigo()).sorted().toList();
    }

    public String gerarToken(Usuario usuario) {
        Instant now = Instant.now();
        boolean root = usuario.getEmpresa() == null;
        List<String> permissoes = resolverPermissoes(usuario);

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(properties.expirationSeconds()))
                .subject(String.valueOf(usuario.getId()))
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("root", root)
                .claim("perfilId", usuario.getPerfil().getId())
                .claim("perfilNome", usuario.getPerfil().getNome())
                .claim("permissoes", permissoes);

        if (!root) {
            builder.claim("empresaId", usuario.getEmpresa().getId())
                   .claim("empresaNome", usuario.getEmpresa().getNome());
        }

        JwsHeader header = JwsHeader.with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
    }

    public long expirationSeconds() {
        return properties.expirationSeconds();
    }
}

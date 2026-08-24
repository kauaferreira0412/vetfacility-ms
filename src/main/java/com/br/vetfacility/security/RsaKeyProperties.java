package com.br.vetfacility.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.security.jwt")
public record RsaKeyProperties(Resource privateKey, Resource publicKey, long expirationSeconds, String issuer) {
}

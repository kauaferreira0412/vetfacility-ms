package com.br.vetfacility.controller;

import com.br.vetfacility.dto.auth.ConvidarUsuarioRequest;
import com.br.vetfacility.dto.auth.LoginRequest;
import com.br.vetfacility.dto.auth.RegisterEmpresaRequest;
import com.br.vetfacility.dto.auth.TokenResponse;
import com.br.vetfacility.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ROOT')")
    @Operation(summary = "Cadastra uma nova empresa (tenant) e o primeiro usuário (proprietário). Restrito ao ROOT.")
    public ResponseEntity<TokenResponse> registrar(@Valid @RequestBody RegisterEmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarEmpresa(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica o usuário e retorna o token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    @Operation(summary = "Cadastra um novo usuário na mesma empresa do usuário autenticado, atribuindo um perfil de acesso")
    public ResponseEntity<TokenResponse> convidarUsuario(@Valid @RequestBody ConvidarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.convidarUsuario(request));
    }
}

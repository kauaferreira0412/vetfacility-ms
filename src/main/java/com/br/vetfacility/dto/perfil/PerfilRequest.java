package com.br.vetfacility.dto.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PerfilRequest(
        @NotBlank(message = "O nome do perfil é obrigatório") String nome,
        @NotEmpty(message = "Selecione ao menos uma permissão") List<Long> permissaoIds
) {
}

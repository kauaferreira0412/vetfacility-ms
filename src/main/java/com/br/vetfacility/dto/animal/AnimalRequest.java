package com.br.vetfacility.dto.animal;

import com.br.vetfacility.enums.Porte;
import com.br.vetfacility.enums.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalRequest(
        @NotBlank(message = "O nome do animal é obrigatório") String nome,
        String especie,
        Porte porte,
        String raca,
        Sexo sexo,
        LocalDate dataNascimento,
        BigDecimal peso,
        String corPelagem,
        String observacoes,
        @NotNull(message = "O cliente é obrigatório") Long clienteId
) {
}

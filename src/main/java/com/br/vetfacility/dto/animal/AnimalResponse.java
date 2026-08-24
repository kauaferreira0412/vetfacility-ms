package com.br.vetfacility.dto.animal;

import com.br.vetfacility.domain.Animal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalResponse(
        Long id, String nome, String especie, String porte, String raca, String sexo,
        LocalDate dataNascimento, BigDecimal peso, String corPelagem, String observacoes,
        Long clienteId, String clienteNome
) {
    public static AnimalResponse from(Animal a) {
        return new AnimalResponse(
                a.getId(), a.getNome(), a.getEspecie(),
                a.getPorte() != null ? a.getPorte().name() : null,
                a.getRaca(),
                a.getSexo() != null ? a.getSexo().name() : null,
                a.getDataNascimento(), a.getPeso(), a.getCorPelagem(), a.getObservacoes(),
                a.getCliente().getId(), a.getCliente().getNome()
        );
    }
}

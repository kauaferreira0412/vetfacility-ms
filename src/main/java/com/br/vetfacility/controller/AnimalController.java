package com.br.vetfacility.controller;

import com.br.vetfacility.dto.animal.AnimalRequest;
import com.br.vetfacility.dto.animal.AnimalResponse;
import com.br.vetfacility.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/animais")
@Tag(name = "Animais")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ANIMAL_VISUALIZAR')")
    @Operation(summary = "Lista os animais da empresa")
    public List<AnimalResponse> listar() {
        return animalService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ANIMAL_GERENCIAR')")
    @Operation(summary = "Cadastra um novo animal")
    public ResponseEntity<AnimalResponse> criar(@Valid @RequestBody AnimalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ANIMAL_GERENCIAR')")
    @Operation(summary = "Atualiza os dados de um animal")
    public AnimalResponse atualizar(@PathVariable Long id, @Valid @RequestBody AnimalRequest request) {
        return animalService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANIMAL_GERENCIAR')")
    @Operation(summary = "Remove um animal")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        animalService.remover(id);
        return ResponseEntity.noContent().build();
    }
}

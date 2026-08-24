package com.br.vetfacility.service;

import com.br.vetfacility.domain.Animal;
import com.br.vetfacility.domain.Cliente;
import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.dto.animal.AnimalRequest;
import com.br.vetfacility.dto.animal.AnimalResponse;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.AnimalRepository;
import com.br.vetfacility.repository.ClienteRepository;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public AnimalService(AnimalRepository animalRepository, ClienteRepository clienteRepository,
                          EmpresaRepository empresaRepository) {
        this.animalRepository = animalRepository;
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> listar() {
        return animalRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(AnimalResponse::from).toList();
    }

    @Transactional
    public AnimalResponse criar(AnimalRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(request.clienteId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        Empresa empresa = empresaRepository.getReferenceById(empresaId);

        Animal animal = Animal.builder()
                .nome(request.nome())
                .especie(request.especie())
                .porte(request.porte())
                .raca(request.raca())
                .sexo(request.sexo())
                .dataNascimento(request.dataNascimento())
                .peso(request.peso())
                .corPelagem(request.corPelagem())
                .observacoes(request.observacoes())
                .cliente(cliente)
                .empresa(empresa)
                .build();
        return AnimalResponse.from(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponse atualizar(Long id, AnimalRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Animal animal = animalRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado."));

        if (!animal.getCliente().getId().equals(request.clienteId())) {
            Cliente cliente = clienteRepository.findByIdAndEmpresaId(request.clienteId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
            animal.setCliente(cliente);
        }
        animal.setNome(request.nome());
        animal.setEspecie(request.especie());
        animal.setPorte(request.porte());
        animal.setRaca(request.raca());
        animal.setSexo(request.sexo());
        animal.setDataNascimento(request.dataNascimento());
        animal.setPeso(request.peso());
        animal.setCorPelagem(request.corPelagem());
        animal.setObservacoes(request.observacoes());
        return AnimalResponse.from(animalRepository.save(animal));
    }

    @Transactional
    public void remover(Long id) {
        Animal animal = animalRepository.findByIdAndEmpresaId(id, SecurityUtils.currentEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado."));
        animalRepository.delete(animal);
    }
}

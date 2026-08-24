package com.br.vetfacility.service;

import com.br.vetfacility.domain.Cliente;
import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.dto.cliente.ClienteRequest;
import com.br.vetfacility.dto.cliente.ClienteResponse;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.ClienteRepository;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public ClienteService(ClienteRepository clienteRepository, EmpresaRepository empresaRepository) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByEmpresaIdOrderByNomeAsc(SecurityUtils.currentEmpresaId())
                .stream().map(ClienteResponse::from).toList();
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        Empresa empresa = empresaRepository.getReferenceById(SecurityUtils.currentEmpresaId());
        Cliente cliente = Cliente.builder()
                .nome(request.nome())
                .telefone(request.telefone())
                .email(request.email())
                .cpf(request.cpf())
                .endereco(request.endereco())
                .cidade(request.cidade())
                .cep(request.cep())
                .observacoes(request.observacoes())
                .empresa(empresa)
                .build();
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarOuFalhar(id);
        cliente.setNome(request.nome());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());
        cliente.setCpf(request.cpf());
        cliente.setEndereco(request.endereco());
        cliente.setCidade(request.cidade());
        cliente.setCep(request.cep());
        cliente.setObservacoes(request.observacoes());
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = buscarOuFalhar(id);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarOuFalhar(Long id) {
        return clienteRepository.findByIdAndEmpresaId(id, SecurityUtils.currentEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }
}

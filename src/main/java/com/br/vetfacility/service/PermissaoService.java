package com.br.vetfacility.service;

import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.dto.perfil.PermissaoRequest;
import com.br.vetfacility.dto.perfil.PermissaoResponse;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissaoService {

    private final PermissaoRepository permissaoRepository;
    private final PerfilRepository perfilRepository;

    public PermissaoService(PermissaoRepository permissaoRepository, PerfilRepository perfilRepository) {
        this.permissaoRepository = permissaoRepository;
        this.perfilRepository = perfilRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissaoResponse> listar() {
        return permissaoRepository.findAllByOrderByModuloAscCodigoAsc().stream().map(PermissaoResponse::from).toList();
    }

    @Transactional
    public PermissaoResponse criar(PermissaoRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        if (permissaoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Já existe uma permissão com o código '" + codigo + "'.");
        }

        Permissao permissao = Permissao.builder()
                .codigo(codigo)
                .descricao(request.descricao().trim())
                .modulo(request.modulo().trim().toUpperCase())
                .build();

        return PermissaoResponse.from(permissaoRepository.save(permissao));
    }

    @Transactional
    public void remover(Long id) {
        Permissao permissao = permissaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada."));

        if (perfilRepository.existsByPermissoesId(id)) {
            throw new BusinessException("Não é possível remover uma permissão atribuída a um ou mais perfis de acesso.");
        }

        permissaoRepository.delete(permissao);
    }
}

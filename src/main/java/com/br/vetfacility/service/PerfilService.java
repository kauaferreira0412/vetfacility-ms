package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Perfil;
import com.br.vetfacility.domain.Permissao;
import com.br.vetfacility.dto.perfil.PerfilRequest;
import com.br.vetfacility.dto.perfil.PerfilResponse;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.PerfilRepository;
import com.br.vetfacility.repository.PermissaoRepository;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(PerfilRepository perfilRepository, PermissaoRepository permissaoRepository,
                          EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PerfilResponse> listar() {
        return perfilRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(PerfilResponse::from).toList();
    }

    @Transactional
    public PerfilResponse criar(PerfilRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Empresa empresa = empresaRepository.getReferenceById(empresaId);

        if (perfilRepository.findByNomeAndEmpresaId(request.nome(), empresaId).isPresent()) {
            throw new BusinessException("Já existe um perfil com esse nome nesta empresa.");
        }

        Set<Permissao> permissoes = resolverPermissoes(request.permissaoIds());

        Perfil perfil = Perfil.builder()
                .nome(request.nome())
                .empresa(empresa)
                .sistema(false)
                .permissoes(permissoes)
                .build();

        return PerfilResponse.from(perfilRepository.save(perfil));
    }

    @Transactional
    public PerfilResponse atualizar(Long id, PerfilRequest request) {
        Perfil perfil = buscarOuFalhar(id);
        if (perfil.isSistema()) {
            throw new BusinessException("Perfis padrão do sistema (Proprietário/Auxiliar) não podem ser editados.");
        }

        perfil.setNome(request.nome());
        perfil.setPermissoes(resolverPermissoes(request.permissaoIds()));
        return PerfilResponse.from(perfilRepository.save(perfil));
    }

    @Transactional
    public void remover(Long id) {
        Perfil perfil = buscarOuFalhar(id);
        if (perfil.isSistema()) {
            throw new BusinessException("Perfis padrão do sistema (Proprietário/Auxiliar) não podem ser removidos.");
        }
        if (usuarioRepository.existsByPerfilId(id)) {
            throw new BusinessException("Não é possível remover um perfil que possui usuários vinculados.");
        }
        perfilRepository.delete(perfil);
    }

    private Set<Permissao> resolverPermissoes(List<Long> permissaoIds) {
        Set<Permissao> permissoes = new HashSet<>(permissaoRepository.findAllByIdIn(permissaoIds));
        if (permissoes.size() != new HashSet<>(permissaoIds).size()) {
            throw new BusinessException("Uma ou mais permissões informadas são inválidas.");
        }
        return permissoes;
    }

    private Perfil buscarOuFalhar(Long id) {
        return perfilRepository.findByIdAndEmpresaId(id, SecurityUtils.currentEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado."));
    }
}

package com.br.vetfacility.service;

import com.br.vetfacility.domain.Usuario;
import com.br.vetfacility.dto.usuario.UsuarioResponse;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.UsuarioRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public void desativar(Long id) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        if (id.equals(SecurityUtils.currentUsuarioId())) {
            throw new BusinessException("Você não pode desativar o seu próprio usuário.");
        }

        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}

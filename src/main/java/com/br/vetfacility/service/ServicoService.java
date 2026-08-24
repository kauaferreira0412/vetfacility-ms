package com.br.vetfacility.service;

import com.br.vetfacility.dto.servico.ServicoResponse;
import com.br.vetfacility.repository.ServicoRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar() {
        return servicoRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(ServicoResponse::from).toList();
    }
}

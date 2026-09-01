package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.dto.empresa.AtualizarLogotipoRequest;
import com.br.vetfacility.dto.empresa.AtualizarNomeEmpresaRequest;
import com.br.vetfacility.dto.empresa.EmpresaAtualResponse;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private static final List<String> PREFIXOS_PERMITIDOS = List.of(
            "data:image/png;base64,", "data:image/jpeg;base64,", "data:image/jpg;base64,");

    // Cerca de 2MB de imagem original, já considerando a inflação de ~33% do base64.
    private static final int TAMANHO_MAXIMO_BASE64 = 2_800_000;

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public EmpresaAtualResponse obterAtual() {
        return EmpresaAtualResponse.from(buscarEmpresaAtual());
    }

    @Transactional
    public EmpresaAtualResponse atualizarNome(AtualizarNomeEmpresaRequest request) {
        Empresa empresa = buscarEmpresaAtual();
        empresa.setNome(request.nome());
        return EmpresaAtualResponse.from(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaAtualResponse atualizarLogotipo(AtualizarLogotipoRequest request) {
        String logotipo = request.logotipoBase64();

        if (PREFIXOS_PERMITIDOS.stream().noneMatch(logotipo::startsWith)) {
            throw new BusinessException("Formato de imagem inválido. Envie um arquivo PNG ou JPG.");
        }
        if (logotipo.length() > TAMANHO_MAXIMO_BASE64) {
            throw new BusinessException("A imagem é muito grande. Envie um arquivo de até 2MB.");
        }

        Empresa empresa = buscarEmpresaAtual();
        empresa.setLogotipoUrl(logotipo);
        return EmpresaAtualResponse.from(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaAtualResponse removerLogotipo() {
        Empresa empresa = buscarEmpresaAtual();
        empresa.setLogotipoUrl(null);
        return EmpresaAtualResponse.from(empresaRepository.save(empresa));
    }

    private Empresa buscarEmpresaAtual() {
        return empresaRepository.findById(SecurityUtils.currentEmpresaId())
                .orElseThrow(() -> new BusinessException("Empresa do usuário autenticado não encontrada."));
    }
}

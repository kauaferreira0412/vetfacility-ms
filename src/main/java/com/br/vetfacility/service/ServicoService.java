package com.br.vetfacility.service;

import com.br.vetfacility.domain.Produto;
import com.br.vetfacility.domain.Servico;
import com.br.vetfacility.domain.ServicoProduto;
import com.br.vetfacility.domain.ServicoProdutoId;
import com.br.vetfacility.dto.servico.AtualizarProdutosPadraoRequest;
import com.br.vetfacility.dto.servico.ServicoResponse;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.ProdutoRepository;
import com.br.vetfacility.repository.ServicoRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ProdutoRepository produtoRepository;

    public ServicoService(ServicoRepository servicoRepository, ProdutoRepository produtoRepository) {
        this.servicoRepository = servicoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar() {
        return servicoRepository.findAllByEmpresaId(SecurityUtils.currentEmpresaId())
                .stream().map(ServicoResponse::from).toList();
    }

    @Transactional
    public ServicoResponse atualizarProdutosPadrao(Long servicoId, AtualizarProdutosPadraoRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Servico servico = servicoRepository.findByIdAndEmpresaId(servicoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        servico.getProdutosPadrao().clear();
        request.produtos().forEach(item -> {
            Produto produto = produtoRepository.findByIdAndEmpresaId(item.produtoId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
            servico.getProdutosPadrao().add(ServicoProduto.builder()
                    .id(new ServicoProdutoId(servico.getId(), produto.getId()))
                    .servico(servico)
                    .produto(produto)
                    .quantidadePadrao(item.quantidadePadrao())
                    .build());
        });

        return ServicoResponse.from(servicoRepository.save(servico));
    }
}

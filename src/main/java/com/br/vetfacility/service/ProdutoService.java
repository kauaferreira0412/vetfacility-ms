package com.br.vetfacility.service;

import com.br.vetfacility.domain.Empresa;
import com.br.vetfacility.domain.Produto;
import com.br.vetfacility.dto.produto.ProdutoRequest;
import com.br.vetfacility.dto.produto.ProdutoResponse;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.EmpresaRepository;
import com.br.vetfacility.repository.ProdutoRepository;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, EmpresaRepository empresaRepository) {
        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAllByEmpresaIdOrderByNomeAsc(SecurityUtils.currentEmpresaId())
                .stream().map(ProdutoResponse::from).toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Empresa empresa = empresaRepository.getReferenceById(SecurityUtils.currentEmpresaId());
        Produto produto = Produto.builder()
                .nome(request.nome())
                .quantidadeEstoque(request.quantidadeEstoque())
                .quantidadeMinima(request.quantidadeMinima())
                .unidade(request.unidade() == null || request.unidade().isBlank() ? "un" : request.unidade())
                .empresa(empresa)
                .build();
        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarOuFalhar(id);
        produto.setNome(request.nome());
        produto.setQuantidadeEstoque(request.quantidadeEstoque());
        produto.setQuantidadeMinima(request.quantidadeMinima());
        produto.setUnidade(request.unidade() == null || request.unidade().isBlank() ? "un" : request.unidade());
        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public void remover(Long id) {
        produtoRepository.delete(buscarOuFalhar(id));
    }

    private Produto buscarOuFalhar(Long id) {
        return produtoRepository.findByIdAndEmpresaId(id, SecurityUtils.currentEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }
}

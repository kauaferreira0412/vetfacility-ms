package com.br.vetfacility.service;

import com.br.vetfacility.domain.*;
import com.br.vetfacility.dto.agendamento.AgendamentoRequest;
import com.br.vetfacility.dto.agendamento.AgendamentoResponse;
import com.br.vetfacility.dto.agendamento.CancelarAgendamentoRequest;
import com.br.vetfacility.dto.agendamento.ConcluirAgendamentoRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.exception.ResourceNotFoundException;
import com.br.vetfacility.repository.*;
import com.br.vetfacility.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AnimalRepository animalRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, AnimalRepository animalRepository,
                               ServicoRepository servicoRepository, UsuarioRepository usuarioRepository,
                               ProdutoRepository produtoRepository, EmpresaRepository empresaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.animalRepository = animalRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listar(LocalDate de, LocalDate ate) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        if (de != null && ate != null) {
            return agendamentoRepository
                    .findAllByEmpresaIdAndDataHoraBetweenOrderByDataHoraAsc(
                            empresaId, de.atStartOfDay(), ate.atTime(LocalTime.MAX))
                    .stream().map(AgendamentoResponse::from).toList();
        }
        return agendamentoRepository.findAllByEmpresaIdOrderByDataHoraAsc(empresaId)
                .stream().map(AgendamentoResponse::from).toList();
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();

        Animal animal = animalRepository.findByIdAndEmpresaId(request.animalId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado."));
        Servico servico = servicoRepository.findByIdAndEmpresaId(request.servicoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário (executor) não encontrado."));

        validarConflitoDeHorario(empresaId, request.dataHora(), servico.getDuracaoMin(), null);

        Agendamento agendamento = Agendamento.builder()
                .animal(animal)
                .servico(servico)
                .usuario(usuario)
                .dataHora(request.dataHora())
                .observacao(request.observacao())
                .status(StatusAgendamento.AGENDADO)
                .empresa(empresaRepository.getReferenceById(empresaId))
                .build();

        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse reagendar(Long id, AgendamentoRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Agendamento agendamento = buscarOuFalhar(id, empresaId);

        Animal animal = animalRepository.findByIdAndEmpresaId(request.animalId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado."));
        Servico servico = servicoRepository.findByIdAndEmpresaId(request.servicoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário (executor) não encontrado."));

        validarConflitoDeHorario(empresaId, request.dataHora(), servico.getDuracaoMin(), agendamento.getId());

        agendamento.setAnimal(animal);
        agendamento.setServico(servico);
        agendamento.setUsuario(usuario);
        agendamento.setDataHora(request.dataHora());
        agendamento.setObservacao(request.observacao());

        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id, CancelarAgendamentoRequest request) {
        Agendamento agendamento = buscarOuFalhar(id, SecurityUtils.currentEmpresaId());
        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO || agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Esse agendamento já foi concluído ou cancelado.");
        }
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(request.motivo());
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse iniciarAtendimento(Long id) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Agendamento agendamento = buscarOuFalhar(id, empresaId);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new BusinessException("Apenas agendamentos com status AGENDADO podem iniciar o atendimento.");
        }

        agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse concluir(Long id, ConcluirAgendamentoRequest request) {
        Long empresaId = SecurityUtils.currentEmpresaId();
        Agendamento agendamento = buscarOuFalhar(id, empresaId);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO && agendamento.getStatus() != StatusAgendamento.EM_ATENDIMENTO) {
            throw new BusinessException("Apenas agendamentos AGENDADO ou EM_ATENDIMENTO podem ser concluídos.");
        }

        if (request != null && request.produtosConsumidos() != null) {
            request.produtosConsumidos().forEach(consumo -> {
                Produto produto = produtoRepository.findByIdAndEmpresaId(consumo.produtoId(), empresaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

                if (produto.getQuantidadeEstoque().compareTo(consumo.quantidade()) < 0) {
                    throw new BusinessException("Estoque insuficiente do produto '" + produto.getNome() + "'.");
                }
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().subtract(consumo.quantidade()));
                produtoRepository.save(produto);

                agendamento.getProdutosConsumidos().add(AgendamentoProduto.builder()
                        .id(new AgendamentoProduto.Id(agendamento.getId(), produto.getId()))
                        .agendamento(agendamento)
                        .produto(produto)
                        .quantidade(consumo.quantidade())
                        .build());
            });
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    private Agendamento buscarOuFalhar(Long id, Long empresaId) {
        return agendamentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
    }

    private void validarConflitoDeHorario(Long empresaId, LocalDateTime inicio, int duracaoMin, Long idAgendamentoEmEdicao) {
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);
        LocalDateTime inicioDoDia = inicio.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicio.toLocalDate().atTime(LocalTime.MAX);

        List<Agendamento> doDia = agendamentoRepository.findAtivosNoIntervalo(empresaId, inicioDoDia, fimDoDia);

        boolean conflita = doDia.stream()
                .filter(a -> idAgendamentoEmEdicao == null || !a.getId().equals(idAgendamentoEmEdicao))
                .anyMatch(a -> {
                    LocalDateTime existenteInicio = a.getDataHora();
                    LocalDateTime existenteFim = existenteInicio.plusMinutes(a.getServico().getDuracaoMin());
                    return inicio.isBefore(existenteFim) && existenteInicio.isBefore(fim);
                });

        if (conflita) {
            throw new BusinessException("Já existe um agendamento nesse horário. Escolha outro horário.");
        }
    }
}

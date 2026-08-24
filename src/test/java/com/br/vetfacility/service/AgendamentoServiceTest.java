package com.br.vetfacility.service;

import com.br.vetfacility.domain.*;
import com.br.vetfacility.dto.agendamento.AgendamentoRequest;
import com.br.vetfacility.dto.agendamento.CancelarAgendamentoRequest;
import com.br.vetfacility.dto.agendamento.ConcluirAgendamentoRequest;
import com.br.vetfacility.exception.BusinessException;
import com.br.vetfacility.repository.*;
import com.br.vetfacility.support.TestSecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private EmpresaRepository empresaRepository;

    private AgendamentoService service;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new AgendamentoService(agendamentoRepository, animalRepository, servicoRepository,
                usuarioRepository, produtoRepository, empresaRepository);
        TestSecurityContext.autenticarComoUsuarioDaEmpresa(10L, EMPRESA_ID, List.of("AGENDAMENTO_CRIAR"));
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.limpar();
    }

    private Empresa empresa() {
        return Empresa.builder().id(EMPRESA_ID).nome("Pet Smack").build();
    }

    private Cliente cliente() {
        return Cliente.builder().id(1L).nome("Maria").empresa(empresa()).build();
    }

    private Animal animal() {
        return Animal.builder().id(1L).nome("Bidu").cliente(cliente()).empresa(empresa()).build();
    }

    private Servico servico(int duracaoMin) {
        return Servico.builder().id(1L).nome("Banho").duracaoMin(duracaoMin).empresa(empresa()).build();
    }

    private Usuario usuario() {
        Perfil perfil = Perfil.builder().id(1L).nome("Proprietário").sistema(true).build();
        return Usuario.builder().id(10L).nome("Francisco").empresa(empresa()).perfil(perfil).ativo(true).build();
    }

    @Test
    void criar_semConflito_deveSalvarAgendamentoAgendado() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, 10L, dataHora, "Observação");

        when(animalRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(animal()));
        when(servicoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(servico(60)));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario()));
        when(agendamentoRepository.findAtivosNoIntervalo(eq(EMPRESA_ID), any(), any())).thenReturn(List.of());
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        var response = service.criar(request);

        assertThat(response.status()).isEqualTo("AGENDADO");
        assertThat(response.id()).isEqualTo(99L);
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void criar_comHorarioConflitante_deveLancarBusinessException() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, 10L, dataHora, null);

        Agendamento existente = Agendamento.builder()
                .id(5L)
                .dataHora(dataHora.plusMinutes(15)) // começa 15min depois, dentro da duração de 60min do novo
                .servico(servico(60))
                .status(StatusAgendamento.AGENDADO)
                .build();

        when(animalRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(animal()));
        when(servicoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(servico(60)));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario()));
        when(agendamentoRepository.findAtivosNoIntervalo(eq(EMPRESA_ID), any(), any())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("horário");

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void criar_comHorarioAdjacenteSemSobreposicao_naoDeveConflitar() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, 10L, dataHora, null);

        // Agendamento existente das 9h às 10h (60min) - termina exatamente quando o novo começa.
        Agendamento existente = Agendamento.builder()
                .id(5L)
                .dataHora(dataHora.minusMinutes(60))
                .servico(servico(60))
                .status(StatusAgendamento.AGENDADO)
                .build();

        when(animalRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(animal()));
        when(servicoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(servico(60)));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario()));
        when(agendamentoRepository.findAtivosNoIntervalo(eq(EMPRESA_ID), any(), any())).thenReturn(List.of(existente));
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.criar(request);

        assertThat(response.status()).isEqualTo("AGENDADO");
    }

    @Test
    void concluir_comEstoqueSuficiente_deveDarBaixaNoProdutoEConcluir() {
        Agendamento agendamento = Agendamento.builder()
                .id(1L).animal(animal()).servico(servico(60)).usuario(usuario())
                .status(StatusAgendamento.AGENDADO)
                .build();
        Produto produto = Produto.builder()
                .id(1L).nome("Shampoo").quantidadeEstoque(new BigDecimal("5.00"))
                .quantidadeMinima(new BigDecimal("1.00")).unidade("un").empresa(empresa())
                .build();

        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));
        when(produtoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new ConcluirAgendamentoRequest(
                List.of(new ConcluirAgendamentoRequest.ProdutoConsumido(1L, new BigDecimal("2.00"))));

        var response = service.concluir(1L, request);

        assertThat(response.status()).isEqualTo("CONCLUIDO");
        assertThat(produto.getQuantidadeEstoque()).isEqualByComparingTo("3.00");
        verify(produtoRepository).save(produto);
    }

    @Test
    void concluir_comEstoqueInsuficiente_deveLancarBusinessExceptionSemAlterarEstoque() {
        Agendamento agendamento = Agendamento.builder()
                .id(1L).animal(animal()).servico(servico(60)).usuario(usuario())
                .status(StatusAgendamento.EM_ATENDIMENTO)
                .build();
        Produto produto = Produto.builder()
                .id(1L).nome("Shampoo").quantidadeEstoque(new BigDecimal("1.00"))
                .quantidadeMinima(new BigDecimal("1.00")).unidade("un").empresa(empresa())
                .build();

        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));
        when(produtoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(produto));

        var request = new ConcluirAgendamentoRequest(
                List.of(new ConcluirAgendamentoRequest.ProdutoConsumido(1L, new BigDecimal("5.00"))));

        assertThatThrownBy(() -> service.concluir(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        assertThat(produto.getQuantidadeEstoque()).isEqualByComparingTo("1.00");
        verify(produtoRepository, never()).save(any());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void concluir_agendamentoJaCancelado_deveLancarBusinessException() {
        Agendamento agendamento = Agendamento.builder()
                .id(1L).status(StatusAgendamento.CANCELADO).build();
        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.concluir(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGENDADO ou EM_ATENDIMENTO");
    }

    @Test
    void cancelar_devePreencherMotivoEStatus() {
        Agendamento agendamento = Agendamento.builder().id(1L).animal(animal()).servico(servico(60))
                .usuario(usuario()).status(StatusAgendamento.AGENDADO).build();
        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.cancelar(1L, new CancelarAgendamentoRequest("Cliente remarcou"));

        assertThat(response.status()).isEqualTo("CANCELADO");
        assertThat(response.motivoCancelamento()).isEqualTo("Cliente remarcou");
    }

    @Test
    void cancelar_agendamentoJaConcluido_deveLancarBusinessException() {
        Agendamento agendamento = Agendamento.builder().id(1L).status(StatusAgendamento.CONCLUIDO).build();
        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.cancelar(1L, new CancelarAgendamentoRequest("Motivo qualquer")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void iniciarAtendimento_apartirDeAgendado_deveMudarParaEmAtendimento() {
        Agendamento agendamento = Agendamento.builder().id(1L).animal(animal()).servico(servico(60))
                .usuario(usuario()).status(StatusAgendamento.AGENDADO).build();
        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.iniciarAtendimento(1L);

        assertThat(response.status()).isEqualTo("EM_ATENDIMENTO");
    }

    @Test
    void iniciarAtendimento_apartirDeConcluido_deveLancarBusinessException() {
        Agendamento agendamento = Agendamento.builder().id(1L).status(StatusAgendamento.CONCLUIDO).build();
        when(agendamentoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.iniciarAtendimento(1L)).isInstanceOf(BusinessException.class);
    }
}

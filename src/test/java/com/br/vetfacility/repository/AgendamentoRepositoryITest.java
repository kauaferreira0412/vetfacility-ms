package com.br.vetfacility.repository;

import com.br.vetfacility.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AgendamentoRepositoryITest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private Empresa empresa;
    private Animal animal;
    private Servico servico;
    private Usuario usuario;

    private void seedBase() {
        empresa = em.persistAndFlush(Empresa.builder().nome("Pet Smack").build());
        Cliente cliente = em.persistAndFlush(Cliente.builder().nome("Maria").empresa(empresa).build());
        animal = em.persistAndFlush(Animal.builder().nome("Bidu").cliente(cliente).empresa(empresa).build());
        servico = em.persistAndFlush(Servico.builder().nome("Banho").duracaoMin(60).empresa(empresa).build());
        Perfil perfil = em.persistAndFlush(Perfil.builder().nome("Proprietário").empresa(empresa).sistema(true).build());
        usuario = em.persistAndFlush(Usuario.builder().nome("Francisco").email("francisco@teste.com")
                .senhaHash("hash").ativo(true).empresa(empresa).perfil(perfil).build());
    }

    private Agendamento novoAgendamento(LocalDateTime dataHora, StatusAgendamento status) {
        return em.persistAndFlush(Agendamento.builder()
                .animal(animal).servico(servico).usuario(usuario)
                .dataHora(dataHora).status(status).empresa(empresa)
                .build());
    }

    @Test
    void findAtivosNoIntervalo_deveExcluirAgendamentosCancelados() {
        seedBase();
        LocalDate hoje = LocalDate.now().plusDays(1);
        novoAgendamento(hoje.atTime(9, 0), StatusAgendamento.AGENDADO);
        novoAgendamento(hoje.atTime(11, 0), StatusAgendamento.CANCELADO);

        List<Agendamento> ativos = agendamentoRepository.findAtivosNoIntervalo(
                empresa.getId(), hoje.atStartOfDay(), hoje.atTime(LocalTime.MAX));

        assertThat(ativos).hasSize(1);
        assertThat(ativos.get(0).getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
    }

    @Test
    void findAllByEmpresaIdAndDataHoraBetween_deveFiltrarPeloIntervaloInformado() {
        seedBase();
        novoAgendamento(LocalDate.now().atTime(9, 0), StatusAgendamento.AGENDADO);
        novoAgendamento(LocalDate.now().plusDays(10).atTime(9, 0), StatusAgendamento.AGENDADO);

        List<Agendamento> resultado = agendamentoRepository.findAllByEmpresaIdAndDataHoraBetweenOrderByDataHoraAsc(
                empresa.getId(), LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));

        assertThat(resultado).hasSize(1);
    }

    @Test
    void findAllByEmpresaIdOrderByDataHoraAsc_deveOrdenarPorDataCrescente() {
        seedBase();
        LocalDate hoje = LocalDate.now();
        novoAgendamento(hoje.atTime(15, 0), StatusAgendamento.AGENDADO);
        novoAgendamento(hoje.atTime(9, 0), StatusAgendamento.AGENDADO);

        List<Agendamento> resultado = agendamentoRepository.findAllByEmpresaIdOrderByDataHoraAsc(empresa.getId());

        assertThat(resultado).extracting(a -> a.getDataHora().toLocalTime())
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(15, 0));
    }
}

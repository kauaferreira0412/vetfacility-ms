package com.br.vetfacility.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoFluxoITest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenRoot() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"root@vetfacility.local","senha":"TesteRoot123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return extrairToken(result);
    }

    private String extrairToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    @Test
    void fluxoCompleto_deCadastroDeEmpresaAteConclusaoDeAgendamentoComBaixaDeEstoque() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + tokenRoot())
                        .contentType("application/json")
                        .content("""
                                {"nomeEmpresa":"Pet Smack Teste","nomeUsuario":"Dono Teste",
                                 "email":"dono@teste.com","senha":"senha123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenDono = extrairToken(registro);

        MvcResult clienteResult = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType("application/json")
                        .content("""
                                {"nome":"Maria Souza","telefone":"85999990000"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long clienteId = objectMapper.readTree(clienteResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult animalResult = mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType("application/json")
                        .content("""
                                {"nome":"Bidu","especie":"Cachorro","clienteId":%d}
                                """.formatted(clienteId)))
                .andExpect(status().isCreated())
                .andReturn();
        long animalId = objectMapper.readTree(animalResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult servicosResult = mockMvc.perform(get("/api/servicos")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andReturn();
        long servicoId = objectMapper.readTree(servicosResult.getResponse().getContentAsString()).get(0).get("id").asLong();

        MvcResult produtoResult = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType("application/json")
                        .content("""
                                {"nome":"Shampoo Neutro","quantidadeEstoque":2,"quantidadeMinima":1,"unidade":"un"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estoqueBaixo").value(false))
                .andReturn();
        long produtoId = objectMapper.readTree(produtoResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult meResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"dono@teste.com","senha":"senha123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long usuarioId = objectMapper.readTree(meResult.getResponse().getContentAsString()).get("usuario").get("id").asLong();

        String dataHoraFutura = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        MvcResult agendamentoResult = mockMvc.perform(post("/api/agendamentos")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType("application/json")
                        .content("""
                                {"animalId":%d,"servicoId":%d,"usuarioId":%d,"dataHora":"%s","observacao":"Teste automatizado"}
                                """.formatted(animalId, servicoId, usuarioId, dataHoraFutura)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andReturn();
        long agendamentoId = objectMapper.readTree(agendamentoResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/agendamentos/{id}/concluir", agendamentoId)
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType("application/json")
                        .content("""
                                {"produtosConsumidos":[{"produtoId":%d,"quantidade":1}]}
                                """.formatted(produtoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        mockMvc.perform(get("/api/produtos")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeEstoque").value(1))
                .andExpect(jsonPath("$[0].estoqueBaixo").value(true));

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Dono Teste"))
                .andExpect(jsonPath("$[0].perfilNome").value("Proprietário"));
    }

    @Test
    void criarAgendamentoComHorarioConflitante_deveRetornar422() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + tokenRoot())
                        .contentType("application/json")
                        .content("""
                                {"nomeEmpresa":"Pet Smack Conflito","nomeUsuario":"Dono Conflito",
                                 "email":"conflito@teste.com","senha":"senha123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String token = extrairToken(registro);
        long usuarioId = objectMapper.readTree(registro.getResponse().getContentAsString()).get("usuario").get("id").asLong();

        MvcResult clienteResult = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"nome\":\"Cliente Conflito\"}"))
                .andExpect(status().isCreated()).andReturn();
        long clienteId = objectMapper.readTree(clienteResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult animalResult = mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"nome\":\"Animal Conflito\",\"clienteId\":%d}".formatted(clienteId)))
                .andExpect(status().isCreated()).andReturn();
        long animalId = objectMapper.readTree(animalResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult servicosResult = mockMvc.perform(get("/api/servicos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        long servicoId = objectMapper.readTree(servicosResult.getResponse().getContentAsString()).get(0).get("id").asLong();

        String dataHora = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String payload = """
                {"animalId":%d,"servicoId":%d,"usuarioId":%d,"dataHora":"%s"}
                """.formatted(animalId, servicoId, usuarioId, dataHora);

        mockMvc.perform(post("/api/agendamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/agendamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("horário")));
    }

    @Test
    void endpointProtegido_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrarEmpresa_semSerRoot_deveRetornar403() throws Exception {
        MvcResult registro = mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + tokenRoot())
                        .contentType("application/json")
                        .content("""
                                {"nomeEmpresa":"Pet Smack Sem Permissao","nomeUsuario":"Dono",
                                 "email":"semroot@teste.com","senha":"senha123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenComum = extrairToken(registro);

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + tokenComum)
                        .contentType("application/json")
                        .content("""
                                {"nomeEmpresa":"Outra Empresa","nomeUsuario":"Outro Dono",
                                 "email":"outro@teste.com","senha":"senha123"}
                                """))
                .andExpect(status().isForbidden());
    }
}

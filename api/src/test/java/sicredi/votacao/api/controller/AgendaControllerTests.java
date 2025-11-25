package sicredi.votacao.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sicredi.votacao.api.integration.AssociateStatus;
import sicredi.votacao.api.integration.AssociateValidatorClient;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaResponse;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionResponse;
import sicredi.votacao.api.domain.dto.voto.VoteRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendaControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	AssociateValidatorClient associateValidatorClient;

	@Test
	@DisplayName("Cria pauta, abre sessão, vota e apura resultado")
	void fluxoCompletoVotacao() throws Exception {
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Teste", "Descricao teste");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);
		assertThat(createRes.id()).isNotNull();

		// abre sessão (30s)
		var openReq = new OpenSessionRequest(createRes.id(), 30L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// votar SIM (mock como ABLE_TO_VOTE)
		given(associateValidatorClient.checkAssociateStatus("12345678901")).willReturn(AssociateStatus.ABLE_TO_VOTE);
		var voteReq1 = new VoteRequest(openRes.sessionId(), "12345678901", "SIM");
		mockMvc.perform(post("/api/votes/votar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voteReq1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// votar NAO com outro associado (mock como ABLE_TO_VOTE)
		given(associateValidatorClient.checkAssociateStatus("23456789012")).willReturn(AssociateStatus.ABLE_TO_VOTE);
		var voteReq2 = new VoteRequest(openRes.sessionId(), "23456789012", "NAO");
		mockMvc.perform(post("/api/votes/votar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voteReq2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// apurar
		mockMvc.perform(get("/api/v1/agendas/" + createRes.id() + "/resultado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(createRes.id()))
				.andExpect(jsonPath("$.sim").value(1))
				.andExpect(jsonPath("$.nao").value(1));

		// finalizar pauta
		mockMvc.perform(put("/api/v1/agendas/" + createRes.id() + "/finalizar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINISHED"));
	}

	@Test
	@DisplayName("Status default ao criar agenda é NOT_STARTED")
	void statusDefaultAoCriarAgenda() throws Exception {
		var createReq = new CreateAgendaRequest("Pauta Default", "Descricao default");
		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("NOT_STARTED"));
	}

	@Test
	@DisplayName("POST /api/v1/agendas falha com título vazio (400)")
	void createAgenda_titleBlank_returns400() throws Exception {
		var body = new CreateAgendaRequest("", "descricao");
		mockMvc.perform(post("/api/v1/agendas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/v1/agendas falha com título null (400)")
	void createAgenda_titleNull_returns400() throws Exception {
		var json = """
				{"title":null,"description":"descricao"}
				""";
		mockMvc.perform(post("/api/v1/agendas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest());
	}

}

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
import sicredi.votacao.api.service.dto.AgendaDtos;

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
		var createReq = new AgendaDtos.CreateAgendaRequest("Pauta Teste", "Descricao teste");
		var createResStr = mockMvc.perform(post("/api/agenda")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, AgendaDtos.CreateAgendaResponse.class);
		assertThat(createRes.id()).isNotNull();

		// abre sessão (30s)
		var openReq = new AgendaDtos.OpenSessionRequest(createRes.id(), 30L);
		var openResStr = mockMvc.perform(post("/api/agenda/abrir-sessao")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, AgendaDtos.OpenSessionResponse.class);

		// votar SIM (mock como ABLE_TO_VOTE)
		given(associateValidatorClient.checkAssociateStatus("12345678901")).willReturn(AssociateStatus.ABLE_TO_VOTE);
		var voteReq1 = new AgendaDtos.VoteRequest(openRes.sessionId(), "12345678901", "SIM");
		mockMvc.perform(post("/api/agenda/votar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voteReq1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// votar NAO com outro associado (mock como ABLE_TO_VOTE)
		given(associateValidatorClient.checkAssociateStatus("23456789012")).willReturn(AssociateStatus.ABLE_TO_VOTE);
		var voteReq2 = new AgendaDtos.VoteRequest(openRes.sessionId(), "23456789012", "NAO");
		mockMvc.perform(post("/api/agenda/votar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voteReq2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// apurar
		mockMvc.perform(get("/api/agenda/" + createRes.id() + "/resultado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(createRes.id()))
				.andExpect(jsonPath("$.sim").value(1))
				.andExpect(jsonPath("$.nao").value(1));

		// finalizar pauta
		mockMvc.perform(put("/api/agenda/" + createRes.id() + "/finalizar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINISHED"));
	}
}



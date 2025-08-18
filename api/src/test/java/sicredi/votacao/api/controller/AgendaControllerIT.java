package sicredi.votacao.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sicredi.votacao.api.service.dto.AgendaDtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendaControllerIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	@DisplayName("Cria pauta, abre sessão, vota e apura resultado")
	void fluxoCompletoVotacao() throws Exception {
		// cria pauta
		var createReq = new AgendaDtos.CreateAgendaRequest("Pauta Teste", "Descricao teste");
		var createResStr = mockMvc.perform(post("/api/agenda")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isOk())
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

		// votar SIM
		var voteReq1 = new AgendaDtos.VoteRequest(openRes.sessionId(), "11111111111", "SIM");
		mockMvc.perform(post("/api/agenda/votar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voteReq1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// votar NAO com outro associado
		var voteReq2 = new AgendaDtos.VoteRequest(openRes.sessionId(), "22222222222", "NAO");
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
	}
}



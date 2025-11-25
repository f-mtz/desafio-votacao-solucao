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
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaResponse;


import static org.assertj.core.api.Assertions.assertThat;
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
	@DisplayName("Cria pauta com sucesso")
	void criaPauta() throws Exception {
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
	}
}



package sicredi.votacao.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sicredi.votacao.api.service.AgendaService;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaResponse;
import sicredi.votacao.api.domain.dto.agenda.ResultResponse;
// import sicredi.votacao.api.domain.dto.voto.VoteResultResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgendaController.class)
class AgendaControllerWebMvcTest {

	@Autowired 
	MockMvc mockMvc;

	@Autowired 
	ObjectMapper objectMapper;

	@MockBean 
	AgendaService agendaService;

	@Test
	@DisplayName("POST /api/v1/agendas retorna 201 com payload válido")
	void createAgenda_returns201() throws Exception {
		given(agendaService.createAgenda(any())).willReturn(new CreateAgendaResponse(1L, "t", "d", "NOT_STARTED"));
		var body = new CreateAgendaRequest("titulo", "descricao");
		mockMvc.perform(post("/api/v1/agendas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	@DisplayName("PUT /api/v1/agendas/{id}/finalizar retorna 200")
	void finish_returns200() throws Exception {
		given(agendaService.finishAgenda(1L)).willReturn(new CreateAgendaResponse(1L, "t", "d", "FINISHED"));
		mockMvc.perform(put("/api/v1/agendas/1/finalizar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINISHED"));
	}

	@Test
	@DisplayName("GET /api/v1/agendas/{id}/resultado retorna 200")
	void result_returns200() throws Exception {
		given(agendaService.result(1L)).willReturn(new ResultResponse(1L, 2L, 3, 1, "IN_PROGRESS"));
		mockMvc.perform(get("/api/v1/agendas/1/resultado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(1));
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
		// Monta JSON manualmente para enviar title null
		var json = """
				{"title":null,"description":"descricao"}
				""";
		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/v1/agendas falha com descrição vazia (400)")
	void createAgenda_descriptionBlank_returns400() throws Exception {
		var body = new CreateAgendaRequest("titulo", "");
		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/v1/agendas falha com descrição null (400)")
	void createAgenda_descriptionNull_returns400() throws Exception {
		var json = """
				{"title":"titulo","description":null}
				""";
		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}
}



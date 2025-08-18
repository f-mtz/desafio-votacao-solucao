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
import sicredi.votacao.api.service.dto.AgendaDtos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgendaController.class)
class AgendaControllerWebMvcTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@MockBean AgendaService agendaService;

	@Test
	@DisplayName("POST /api/agenda retorna 201 com payload válido")
	void createAgenda_returns201() throws Exception {
		given(agendaService.createAgenda(any())).willReturn(new AgendaDtos.CreateAgendaResponse(1L, "t", "d", "NOT_STARTED"));
		var body = new AgendaDtos.CreateAgendaRequest("titulo", "descricao");
		mockMvc.perform(post("/api/agenda")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	@DisplayName("PUT /api/agenda/{id}/finalizar retorna 200")
	void finish_returns200() throws Exception {
		given(agendaService.finishAgenda(1L)).willReturn(new AgendaDtos.CreateAgendaResponse(1L, "t", "d", "FINISHED"));
		mockMvc.perform(put("/api/agenda/1/finalizar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINISHED"));
	}

	@Test
	@DisplayName("GET /api/agenda/{id}/resultado retorna 200")
	void result_returns200() throws Exception {
		given(agendaService.result(1L)).willReturn(new AgendaDtos.ResultResponse(1L, 2L, 3, 1, "IN_PROGRESS"));
		mockMvc.perform(get("/api/agenda/1/resultado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(1));
	}
}



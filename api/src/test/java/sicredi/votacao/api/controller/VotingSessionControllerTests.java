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
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionResponse;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VotingSessionRepository;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.VotingSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VotingSessionControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	AgendaRepository agendaRepository;
	@Autowired
	VotingSessionRepository votingSessionRepository;

	@Test
	@DisplayName("Abre sessão para uma pauta e valida resultado inicial (0/0)")
	void abrirSessaoEValidarResultadoInicial() throws Exception {
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Sessao", "Descricao sessao");
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
		objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// apurar (sem votos)
		mockMvc.perform(get("/api/v1/agendas/" + createRes.id() + "/resultado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(createRes.id()))
				.andExpect(jsonPath("$.sim").value(0))
				.andExpect(jsonPath("$.nao").value(0));
	}

	@Test
	@DisplayName("Falha ao criar sessão sem agenda - agendaId inexistente deve retornar 404")
	void falhaAoCriarSessaoSemAgenda() throws Exception {
		var openReq = new OpenSessionRequest(999999L, 60L);
		mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("Falha ao criar sessão com fim antes do início - duração inválida (<= 0)")
	void falhaAoCriarSessaoComFimAntesDoInicio() throws Exception {
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Sessao Inválida", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// tenta abrir sessão com duração inválida (0s) -> deve falhar
		var openReq = new OpenSessionRequest(createRes.id(), 0L);
		mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	// --------- openSessionAt (data/hora arbitrária) ----------

	@Test
	@DisplayName("openSessionAt - cria sessão com startTime/endTime e associa à agenda")
	void openSessionAt_criaSessaoComStartEnd() throws Exception {
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Arbitraria", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var json = """
				{"agendaId":%d,"startTime":"2025-01-01T12:00:00Z","endTime":"2025-01-01T13:00:00Z"}
				""".formatted(createRes.id());
		var resStr = mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var res = objectMapper.readValue(resStr, OpenSessionResponse.class);

		var start = java.time.LocalDateTime.parse(res.startTime());
		var end = java.time.LocalDateTime.parse(res.endTime());
		assertThat(start).isBefore(end);

		Agenda agenda = agendaRepository.findById(createRes.id()).orElseThrow();
		VotingSession session = agenda.getVotingSession();
		assertThat(session).isNotNull();
		assertThat(session.getId()).isEqualTo(res.sessionId());
		assertThat(session.getStartTime()).isEqualTo(start);
		assertThat(session.getEndTime()).isEqualTo(end);
	}

	@Test
	@DisplayName("openSessionAt - endTime padrão de 2h quando não enviado")
	void openSessionAt_endPadraoQuandoNaoEnviado() throws Exception {
		var createReq = new CreateAgendaRequest("Pauta Arbitraria 2", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var json = """
				{"agendaId":%d,"startTime":"2025-01-01T12:00:00Z"}
				""".formatted(createRes.id());
		var resStr = mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		var res = objectMapper.readValue(resStr, OpenSessionResponse.class);

		var start = java.time.LocalDateTime.parse(res.startTime());
		var end = java.time.LocalDateTime.parse(res.endTime());
		assertThat(end).isEqualTo(start.plusHours(2));
	}

	@Test
	@DisplayName("openSessionAt - falha quando agenda não existe (404)")
	void openSessionAt_agendaInexistente() throws Exception {
		var json = """
				{"agendaId":999999,"startTime":"2025-01-01T12:00:00Z","endTime":"2025-01-01T14:00:00Z"}
				""";
		mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("openSessionAt - falha quando endTime <= startTime (400)")
	void openSessionAt_endNaoMaiorQueStart() throws Exception {
		var createReq = new CreateAgendaRequest("Pauta Arbitraria 3", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// end igual a start
		var json = """
				{"agendaId":%d,"startTime":"2025-01-01T12:00:00Z","endTime":"2025-01-01T12:00:00Z"}
				""".formatted(createRes.id());
		mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("openSessionAt - falha quando startTime é nulo (400)")
	void openSessionAt_startNulo() throws Exception {
		var createReq = new CreateAgendaRequest("Pauta Arbitraria 4", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var json = """
				{"agendaId":%d,"startTime":null,"endTime":"2025-01-01T14:00:00Z"}
				""".formatted(createRes.id());
		mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("openSessionAt - falha ao abrir nova sessão quando já existe sessão associada")
	void openSessionAt_jaPossuiSessao() throws Exception {
		// cria pauta e abre primeira sessão
		var createReq = new CreateAgendaRequest("Pauta Arbitraria 5", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var first = """
				{"agendaId":%d,"startTime":"2025-01-01T12:00:00Z","endTime":"2025-01-01T14:00:00Z"}
				""".formatted(createRes.id());
		mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(first))
				.andExpect(status().isOk());

		var second = """
				{"agendaId":%d,"startTime":"2025-01-01T15:00:00Z","endTime":"2025-01-01T16:00:00Z"}
				""".formatted(createRes.id());
		mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(second))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("openSessionAt - converte offsets para UTC corretamente (fuso horário)")
	void openSessionAt_conversaoFusoParaUtc() throws Exception {
		var createReq = new CreateAgendaRequest("Pauta Fuso", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// -03:00 deve resultar em UTC +3h
		var body = """
				{"agendaId":%d,"startTime":"2025-01-01T12:00:00-03:00","endTime":"2025-01-01T14:00:00-03:00"}
				""".formatted(createRes.id());
		var resStr = mockMvc.perform(post("/api/v1/session/criar-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		var res = objectMapper.readValue(resStr, OpenSessionResponse.class);

		var startUtc = java.time.OffsetDateTime.parse("2025-01-01T12:00:00-03:00").toInstant().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime();
		var endUtc = java.time.OffsetDateTime.parse("2025-01-01T14:00:00-03:00").toInstant().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime();
		assertThat(java.time.LocalDateTime.parse(res.startTime())).isEqualTo(startUtc);
		assertThat(java.time.LocalDateTime.parse(res.endTime())).isEqualTo(endUtc);
	}

	@Test
	@DisplayName("Criar sessão com início e fim válidos - persiste e associa à agenda")
	void criarSessaoComInicioFimValidos() throws Exception {
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Sessao 2", "Descricao sessao 2");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);
		assertThat(createRes.id()).isNotNull();

		// abre sessão (90s)
		var openReq = new OpenSessionRequest(createRes.id(), 90L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andExpect(jsonPath("$.startTime").exists())
				.andExpect(jsonPath("$.endTime").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// valida start < end
		var start = java.time.LocalDateTime.parse(openRes.startTime());
		var end = java.time.LocalDateTime.parse(openRes.endTime());
		assertThat(start).isBefore(end);

		// valida persistência e associação à agenda
		Agenda agenda = agendaRepository.findById(createRes.id()).orElseThrow();
		VotingSession session = agenda.getVotingSession();
		assertThat(session).isNotNull();
		assertThat(session.getId()).isEqualTo(openRes.sessionId());
		assertThat(session.getStartTime()).isNotNull();
		assertThat(session.getEndTime()).isNotNull();
		assertThat(session.getStartTime()).isBefore(session.getEndTime());
	}

	
}




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
import sicredi.votacao.api.domain.dto.voto.VoteRequest;
import sicredi.votacao.api.repository.VotingSessionRepository;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VoteRepository;
import sicredi.votacao.api.integration.AssociateValidatorClient;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VoteControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	AssociateValidatorClient associateValidatorClient;

	@Autowired
	AgendaRepository agendaRepository;

	@Autowired
	VotingSessionRepository votingSessionRepository;

	@Autowired
	VoteRepository voteRepository;

	@Test
	@DisplayName("Vota SIM e NAO em sessão e confere resultado")
	void votarEmSessaoEApurarResultado() throws Exception {
		// garante associados aptos
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344705"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344706"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);
		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Voto", "Descricao voto");
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

		// votar SIM
		var voteReq1 = new VoteRequest(openRes.sessionId(), "39053344705", "SIM");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voteReq1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// votar NAO com outro associado
		var voteReq2 = new VoteRequest(openRes.sessionId(), "39053344706", "NAO");
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
	}

	@Test
	@DisplayName("Voto em sessão inexistente deve retornar 404")
	void votoEmSessaoInexistente() throws Exception {
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344707"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

		var req = new VoteRequest(999999L, "39053344707", "SIM");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isNotFound());
	}
	@Test
	@DisplayName("Voto sem identificador de associado (vazio) deve falhar com 400")
	void votoSemAssociateIdVazio() throws Exception {
		// cria pauta e abre sessão
		var createReq = new CreateAgendaRequest("Pauta Sem Assoc", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var openReq = new OpenSessionRequest(createRes.id(), 60L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// associateId vazio -> 400 (Bean Validation @NotBlank)
		var body = new VoteRequest(openRes.sessionId(), "", "SIM");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Voto sem identificador de associado (null) deve falhar com 400")
	void votoSemAssociateIdNull() throws Exception {
		// cria pauta e abre sessão
		var createReq = new CreateAgendaRequest("Pauta Sem Assoc 2", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		var openReq = new OpenSessionRequest(createRes.id(), 60L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// JSON manual com associateId null -> 400
		var json = """
				{"sessionId":%d,"associateId":null,"vote":"SIM"}
				""".formatted(openRes.sessionId());
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}
	@Test
	@DisplayName("Voto com tipo inválido (ABSTENCAO) deve falhar com 400")
	void votoTipoInvalidoAbstencao() throws Exception {
		// associado apto
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344705"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Voto Invalido", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// abre sessão
		var openReq = new OpenSessionRequest(createRes.id(), 60L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// tenta votar com tipo inválido
		var invalidVote = new VoteRequest(openRes.sessionId(), "39053344705", "ABSTENCAO");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidVote)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Voto com tipo null deve falhar com 400")
	void votoTipoNullDeveFalhar() throws Exception {
		// associado apto
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344706"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Voto Null", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// abre sessão
		var openReq = new OpenSessionRequest(createRes.id(), 60L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// monta JSON manual para enviar vote: null
		var json = """
				{"sessionId":%d,"associateId":"39053344706","vote":null}
				""".formatted(openRes.sessionId());
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}
    @Test
@DisplayName("Nega voto quando sessão já terminou (FINISHED)")
void negaVotoQuandoSessaoTerminou() throws Exception {
	org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344706"))
			.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

	var agenda = new sicredi.votacao.api.domain.Agenda();
	agenda.setTitle("Pauta");
	agenda.setDescription("Desc");
	agenda.setStatus(sicredi.votacao.api.domain.AgendaStatus.FINISHED);
	agenda = agendaRepository.save(agenda);

	var session = new sicredi.votacao.api.domain.VotingSession();
	session.setAgenda(agenda);
	session.setStartTime(java.time.LocalDateTime.now().minusMinutes(10));
	session.setEndTime(java.time.LocalDateTime.now().minusMinutes(5));
	session = votingSessionRepository.save(session);

	var voteReq = new sicredi.votacao.api.domain.dto.voto.VoteRequest(session.getId(), "39053344706", "NAO");
	mockMvc.perform(post("/api/votes/votar")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(voteReq)))
			.andExpect(status().isBadRequest());
}

@Test
@DisplayName("Nega voto quando sessão ainda não iniciou (NOT_STARTED)")
void negaVotoQuandoSessaoNaoIniciou() throws Exception {
	// mocka associados aptos
	org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344705"))
			.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

	// cria pauta e sessão manualmente antes do start
	var agenda = new sicredi.votacao.api.domain.Agenda();
	agenda.setTitle("Pauta");
	agenda.setDescription("Desc");
	agenda.setStatus(sicredi.votacao.api.domain.AgendaStatus.NOT_STARTED);
	agenda = agendaRepository.save(agenda);

	var session = new sicredi.votacao.api.domain.VotingSession();
	session.setAgenda(agenda);
	session.setStartTime(java.time.LocalDateTime.now().plusMinutes(5));
	session.setEndTime(java.time.LocalDateTime.now().plusMinutes(10));
	session = votingSessionRepository.save(session);

	var voteReq = new sicredi.votacao.api.domain.dto.voto.VoteRequest(session.getId(), "39053344705", "SIM");
	mockMvc.perform(post("/api/votes/votar")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(voteReq)))
			.andExpect(status().isBadRequest());
}

	@Test
	@DisplayName("Usuário só pode votar uma vez por sessão (segundo voto rejeitado com 409)")
	void naoPermiteVotarDuasVezesMesmaSessao() throws Exception {
		// associado apto
		org.mockito.BDDMockito.given(associateValidatorClient.checkAssociateStatus("39053344705"))
				.willReturn(sicredi.votacao.api.integration.AssociateStatus.ABLE_TO_VOTE);

		// cria pauta
		var createReq = new CreateAgendaRequest("Pauta Unica Votacao", "Descricao");
		var createResStr = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		var createRes = objectMapper.readValue(createResStr, CreateAgendaResponse.class);

		// abre sessão
		var openReq = new OpenSessionRequest(createRes.id(), 60L);
		var openResStr = mockMvc.perform(post("/api/v1/session/abrir-sessao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(openReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionId").exists())
				.andReturn().getResponse().getContentAsString();
		var openRes = objectMapper.readValue(openResStr, OpenSessionResponse.class);

		// primeiro voto OK
		var firstVote = new VoteRequest(openRes.sessionId(), "39053344705", "SIM");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(firstVote)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.voteId").exists());

		// segundo voto do mesmo associado na mesma sessão -> 409 CONFLICT
		var secondVote = new VoteRequest(openRes.sessionId(), "39053344705", "NAO");
		mockMvc.perform(post("/api/votes/votar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(secondVote)))
				.andExpect(status().isConflict());
	}
}


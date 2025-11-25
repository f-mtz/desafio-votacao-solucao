package sicredi.votacao.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;
import sicredi.votacao.api.domain.Vote;
import sicredi.votacao.api.domain.VoteType;
import sicredi.votacao.api.domain.VotingSession;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.voto.VoteRequest;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VoteRepository;
import sicredi.votacao.api.repository.VotingSessionRepository;

import sicredi.votacao.api.domain.exception.ApiException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

	@Mock AgendaRepository agendaRepository;
	@Mock VotingSessionRepository votingSessionRepository;
	@Mock VoteRepository voteRepository;
	@Mock AssociateValidatorService validatorService;

	@InjectMocks AgendaService agendaService;
	@InjectMocks VotoService votoService;
	@InjectMocks VotingSessionService sessionService;

	Agenda agenda;

	@BeforeEach
	void setup() {
		agenda = new Agenda();
		agenda.setId(1L);
		agenda.setTitle("Pauta");
		agenda.setDescription("Desc");
		agenda.setStatus(AgendaStatus.NOT_STARTED);
	}

	@Test
	@DisplayName("Cria pauta com sucesso")
	void createAgenda_ok() {
		given(agendaRepository.save(any(Agenda.class))).willAnswer(inv -> {
			Agenda a = inv.getArgument(0);
			a.setId(10L);
			return a;
		});

		var res = agendaService.createAgenda(new CreateAgendaRequest("Titulo", "Descricao"));
		assertThat(res.id()).isEqualTo(10L);
		assertThat(res.status()).isEqualTo(AgendaStatus.NOT_STARTED.name());
		verify(agendaRepository).save(any(Agenda.class));
	}

	@Test
	@DisplayName("Abre sessão com duração default quando não informada")
	void openSession_defaultDuration() {
		given(agendaRepository.findById(1L)).willReturn(Optional.of(agenda));
		given(votingSessionRepository.save(any(VotingSession.class))).willAnswer(inv -> {
			VotingSession s = inv.getArgument(0);
			s.setId(5L);
			return s;
		});

		var res = sessionService.openSession(new OpenSessionRequest(1L, null));
		assertThat(res.sessionId()).isEqualTo(5L);
		assertThat(agenda.getStatus()).isEqualTo(AgendaStatus.IN_PROGRESS);
	}

	@Test
	@DisplayName("Não permite abrir sessão se já existir")
	void openSession_alreadyExists() {
		agenda.setVotingSession(new VotingSession());
		given(agendaRepository.findById(1L)).willReturn(Optional.of(agenda));
		assertThatThrownBy(() -> sessionService.openSession(new OpenSessionRequest(1L, 60L)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Sessão já aberta");
	}

	@Test
	@DisplayName("Voto único por associado e sessão")
	void vote_uniquePerAssociate() {
		VotingSession session = new VotingSession();
		session.setId(2L);
		session.setStartTime(LocalDateTime.now().minusMinutes(1));
		session.setEndTime(LocalDateTime.now().plusMinutes(1));
		given(votingSessionRepository.findById(2L)).willReturn(Optional.of(session));
		given(voteRepository.existsByVotingSession_IdAndAssociateId(2L, "123")).willReturn(false);
		doNothing().when(validatorService).validateAssociateOrThrow("123");
		given(voteRepository.save(any(Vote.class))).willAnswer(inv -> {
			Vote v = inv.getArgument(0);
			v.setId(99L);
			return v;
		});

		var res = votoService.vote(new VoteRequest(2L, "123", "SIM"));
		assertThat(res.voteId()).isEqualTo(99L);
		verify(voteRepository).save(any(Vote.class));
	}

	@Test
	@DisplayName("Rejeita voto duplicado do mesmo associado na mesma sessão")
	void vote_duplicateRejected() {
		VotingSession session = new VotingSession();
		session.setId(2L);
		session.setStartTime(LocalDateTime.now().minusMinutes(1));
		session.setEndTime(LocalDateTime.now().plusMinutes(1));
		given(votingSessionRepository.findById(2L)).willReturn(Optional.of(session));
		given(voteRepository.existsByVotingSession_IdAndAssociateId(2L, "123")).willReturn(true);

		assertThatThrownBy(() -> votoService.vote(new VoteRequest(2L, "123", "SIM")))
				.isInstanceOf(ApiException.class)
				.matches(ex -> ((ApiException) ex).getStatus() == HttpStatus.CONFLICT);
	}

	@Test
	@DisplayName("Apuração retorna totais corretos")
	void result_counts() {
		VotingSession session = new VotingSession();
		session.setId(2L);
		session.setStartTime(LocalDateTime.now().minusMinutes(5));
		session.setEndTime(LocalDateTime.now().plusMinutes(5));
		agenda.setVotingSession(session);
		given(agendaRepository.findById(1L)).willReturn(Optional.of(agenda));
		given(voteRepository.countByVotingSession_IdAndVoteType(2L, VoteType.SIM)).willReturn(3L);
		given(voteRepository.countByVotingSession_IdAndVoteType(2L, VoteType.NAO)).willReturn(1L);

		var res = agendaService.result(1L);
		assertThat(res.sim()).isEqualTo(3);
		assertThat(res.nao()).isEqualTo(1);
	}

	@Test
	@DisplayName("Finaliza pauta atualizando status para FINISHED")
	void finishAgenda_ok() {
		given(agendaRepository.findById(1L)).willReturn(Optional.of(agenda));
		given(agendaRepository.save(any(Agenda.class))).willAnswer(inv -> inv.getArgument(0));
		var res = agendaService.finishAgenda(1L);
		assertThat(res.status()).isEqualTo(AgendaStatus.FINISHED.name());
		assertThat(agenda.getStatus()).isEqualTo(AgendaStatus.FINISHED);
	}
}



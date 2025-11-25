package sicredi.votacao.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;
import sicredi.votacao.api.domain.VotingSession;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionResponse;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionAtRequest;
import sicredi.votacao.api.domain.exception.ApiException;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VotingSessionRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class VotingSessionService {

	private static final Logger log = LoggerFactory.getLogger(VotingSessionService.class);

	@Autowired
	private AgendaRepository agendaRepository;
	@Autowired
	private VotingSessionRepository votingSessionRepository;

	@Transactional
	public OpenSessionResponse openSession(OpenSessionRequest request) {
		log.info("openSession agendaId={} durationSeconds={}", request.agendaId(), request.durationSeconds());
		Agenda agenda = agendaRepository.findById(request.agendaId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		// Regras de validação: duração deve ser positiva quando informada
		if (request.durationSeconds() != null && request.durationSeconds() <= 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Duração inválida: deve ser maior que zero");
		}
		if (agenda.getVotingSession() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Sessão já aberta para esta pauta");
		}
		Duration duration = request.toDurationOrDefault();
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plus(duration);
		VotingSession session = new VotingSession();
		session.setAgenda(agenda);
		session.setStartTime(start);
		session.setEndTime(end);
		session = votingSessionRepository.save(session);
		agenda.setVotingSession(session);
		agenda.setStatus(AgendaStatus.IN_PROGRESS);
		agendaRepository.save(agenda);
		log.info("openSession OK sessionId={} start={} end={}", session.getId(), start, end);
		return new OpenSessionResponse(session.getId(), agenda.getId(), start.toString(), end.toString());
	}

	@Transactional
	public OpenSessionResponse openSessionAt(OpenSessionAtRequest request) {
		log.info("openSessionAt agendaId={} startTime={} endTime={}", request.agendaId(), request.startTime(), request.endTime());
		if (request.startTime() == null || request.startTime().isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "startTime é obrigatório");
		}
		Agenda agenda = agendaRepository.findById(request.agendaId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		// Não permitir abrir nova sessão se já houver sessão associada (regra atual do sistema)
		if (agenda.getVotingSession() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Sessão já aberta para esta pauta");
		}

		LocalDateTime startUtc = toUtcLocalDateTime(request.startTime());
		LocalDateTime endUtc;
		if (request.endTime() == null || request.endTime().isBlank()) {
			endUtc = startUtc.plusHours(2); // duração padrão de 2 horas
		} else {
			endUtc = toUtcLocalDateTime(request.endTime());
		}
		if (!endUtc.isAfter(startUtc)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Horário de término deve ser maior que o horário de início");
		}

		VotingSession session = new VotingSession();
		session.setAgenda(agenda);
		session.setStartTime(startUtc);
		session.setEndTime(endUtc);
		session = votingSessionRepository.save(session);
		agenda.setVotingSession(session);
		agenda.setStatus(AgendaStatus.IN_PROGRESS);
		agendaRepository.save(agenda);
		log.info("openSessionAt OK sessionId={} start={} end={}", session.getId(), startUtc, endUtc);
		return new OpenSessionResponse(session.getId(), agenda.getId(), startUtc.toString(), endUtc.toString());
	}

	private LocalDateTime toUtcLocalDateTime(String isoOffsetDateTime) {
		OffsetDateTime odt = OffsetDateTime.parse(isoOffsetDateTime);
		return LocalDateTime.ofInstant(odt.toInstant(), ZoneOffset.UTC);
	}
}



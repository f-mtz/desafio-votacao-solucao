package sicredi.votacao.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;
import sicredi.votacao.api.domain.Vote;
import sicredi.votacao.api.domain.VoteType;
import sicredi.votacao.api.domain.VotingSession;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VoteRepository;
import sicredi.votacao.api.repository.VotingSessionRepository;
import sicredi.votacao.api.service.dto.AgendaDtos;
import sicredi.votacao.api.service.exception.ApiException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AgendaService {

	private static final Logger log = LoggerFactory.getLogger(AgendaService.class);

	private final AgendaRepository agendaRepository;
	private final VotingSessionRepository votingSessionRepository;
	private final VoteRepository voteRepository;
	private final AssociateValidatorService associateValidatorService;

	public AgendaService(AgendaRepository agendaRepository, VotingSessionRepository votingSessionRepository, VoteRepository voteRepository, AssociateValidatorService associateValidatorService) {
		this.agendaRepository = agendaRepository;
		this.votingSessionRepository = votingSessionRepository;
		this.voteRepository = voteRepository;
		this.associateValidatorService = associateValidatorService;
	}

	@Transactional
	public AgendaDtos.CreateAgendaResponse createAgenda(AgendaDtos.CreateAgendaRequest request) {
		log.info("createAgenda title='{}'", request.title());
		Agenda agenda = new Agenda();
		agenda.setTitle(request.title());
		agenda.setDescription(request.description());
		agenda.setStatus(AgendaStatus.NOT_STARTED);
		agenda = agendaRepository.save(agenda);
		log.info("createAgenda OK id={}", agenda.getId());
		return new AgendaDtos.CreateAgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getStatus().name());
	}

	@Transactional
	public AgendaDtos.OpenSessionResponse openSession(AgendaDtos.OpenSessionRequest request) {
		log.info("openSession agendaId={} durationSeconds={}", request.agendaId(), request.durationSeconds());
		Agenda agenda = agendaRepository.findById(request.agendaId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
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
		return new AgendaDtos.OpenSessionResponse(session.getId(), agenda.getId(), start.toString(), end.toString());
	}

	@Transactional
	public AgendaDtos.VoteResponse vote(AgendaDtos.VoteRequest request) {
		log.info("vote sessionId={} associateId={}", request.sessionId(), request.associateId());
		VotingSession session = votingSessionRepository.findById(request.sessionId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Sessão encerrada ou não iniciada");
		}
		if (voteRepository.existsByVotingSession_IdAndAssociateId(session.getId(), request.associateId())) {
			throw new ApiException(HttpStatus.CONFLICT, "Associado já votou nesta pauta");
		}
		associateValidatorService.validateAssociateOrThrow(request.associateId());
		Vote vote = new Vote();
		vote.setVotingSession(session);
		vote.setAssociateId(request.associateId());
		VoteType type = switch (request.vote().trim().toUpperCase()) {
			case "SIM", "YES" -> VoteType.SIM;
			case "NAO", "NÃO", "NO" -> VoteType.NAO;
			default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Voto inválido. Use 'SIM' ou 'NAO'");
		};
		vote.setVoteType(type);
		vote = voteRepository.save(vote);
		log.info("vote OK id={} type={}", vote.getId(), vote.getVoteType());
		return new AgendaDtos.VoteResponse(vote.getId(), session.getId(), vote.getAssociateId(), vote.getVoteType().name());
	}

	@Transactional(readOnly = true)
	public AgendaDtos.ResultResponse result(Long agendaId) {
		log.info("result agendaId={}", agendaId);
		Agenda agenda = agendaRepository.findById(agendaId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		VotingSession session = agenda.getVotingSession();
		if (session == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Pauta ainda não possui sessão");
		}
		long sim = voteRepository.countByVotingSession_IdAndVoteType(session.getId(), VoteType.SIM);
		long nao = voteRepository.countByVotingSession_IdAndVoteType(session.getId(), VoteType.NAO);
		String status = LocalDateTime.now().isAfter(session.getEndTime()) ? AgendaStatus.FINISHED.name() : agenda.getStatus().name();
		log.info("result OK sessionId={} sim={} nao={} status={}", session.getId(), sim, nao, status);
		return new AgendaDtos.ResultResponse(agenda.getId(), session.getId(), sim, nao, status);
	}

	@Transactional
	public AgendaDtos.CreateAgendaResponse finishAgenda(Long agendaId) {
		log.info("finishAgenda agendaId={}", agendaId);
		Agenda agenda = agendaRepository.findById(agendaId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		agenda.setStatus(AgendaStatus.FINISHED);
		agenda = agendaRepository.save(agenda);
		log.info("finishAgenda OK id={} status={}", agenda.getId(), agenda.getStatus());
		return new AgendaDtos.CreateAgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getStatus().name());
	}
}



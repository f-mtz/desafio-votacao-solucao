package sicredi.votacao.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;
import sicredi.votacao.api.domain.VoteType;
import sicredi.votacao.api.repository.AgendaRepository;
import sicredi.votacao.api.repository.VoteRepository;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaResponse;
import sicredi.votacao.api.domain.dto.agenda.ResultResponse;
import sicredi.votacao.api.domain.exception.ApiException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendaService {

	private static final Logger log = LoggerFactory.getLogger(AgendaService.class);


	@Autowired
	private AgendaRepository agendaRepository;
	@Autowired
	private VoteRepository voteRepository;


	@Transactional
	public CreateAgendaResponse createAgenda(CreateAgendaRequest request) {
		log.info("createAgenda title='{}'", request.title());
		Agenda agenda = new Agenda();
		agenda.setTitle(request.title());
		agenda.setDescription(request.description());
		agenda.setStatus(AgendaStatus.NOT_STARTED);
		agenda = agendaRepository.save(agenda);
		log.info("createAgenda OK id={}", agenda.getId());
		return new CreateAgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getStatus().name());
	}

	@Transactional(readOnly = true)
	public List<CreateAgendaResponse> listInProgress() {
		log.info("listInProgress agendas");
		return agendaRepository.findByStatus(AgendaStatus.IN_PROGRESS)
				.stream()
				.map(agenda -> new CreateAgendaResponse(
						agenda.getId(),
						agenda.getTitle(),
						agenda.getDescription(),
						agenda.getStatus().name()))
				.collect(Collectors.toList());
	}


	@Transactional(readOnly = true)
	public ResultResponse result(Long agendaId) {
		log.info("result agendaId={}", agendaId);
		Agenda agenda = agendaRepository.findById(agendaId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		var session = agenda.getVotingSession();
		if (session == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Pauta ainda não possui sessão");
		}
		long sim = voteRepository.countByVotingSession_IdAndVoteType(session.getId(), VoteType.SIM);
		long nao = voteRepository.countByVotingSession_IdAndVoteType(session.getId(), VoteType.NAO);
		String status = LocalDateTime.now().isAfter(session.getEndTime()) ? AgendaStatus.FINISHED.name() : agenda.getStatus().name();
		log.info("result OK sessionId={} sim={} nao={} status={}", session.getId(), sim, nao, status);
		return new ResultResponse(agenda.getId(), session.getId(), sim, nao, status);
	}

	@Transactional
	public CreateAgendaResponse finishAgenda(Long agendaId) {
		log.info("finishAgenda agendaId={}", agendaId);
		Agenda agenda = agendaRepository.findById(agendaId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agenda não encontrada"));
		agenda.setStatus(AgendaStatus.FINISHED);
		agenda = agendaRepository.save(agenda);
		log.info("finishAgenda OK id={} status={}", agenda.getId(), agenda.getStatus());
		return new CreateAgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getStatus().name());
	}
}



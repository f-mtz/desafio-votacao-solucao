package sicredi.votacao.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sicredi.votacao.api.domain.Vote;
import sicredi.votacao.api.domain.VoteType;
import sicredi.votacao.api.domain.VotingSession;
import sicredi.votacao.api.domain.dto.voto.VoteRequest;
import sicredi.votacao.api.domain.dto.voto.VoteResponse;
import sicredi.votacao.api.domain.exception.ApiException;
import sicredi.votacao.api.repository.VoteRepository;
import sicredi.votacao.api.repository.VotingSessionRepository;

import java.time.LocalDateTime;

@Service
public class VotoService {

	private static final Logger log = LoggerFactory.getLogger(VotoService.class);

	@Autowired
	private VotingSessionRepository votingSessionRepository;
	@Autowired
	private VoteRepository voteRepository;
	@Autowired
	private AssociateValidatorService associateValidatorService;


	@Transactional
	public VoteResponse vote(VoteRequest request) {
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
		return new VoteResponse(vote.getId(), session.getId(), vote.getAssociateId(), vote.getVoteType().name());
	}
}



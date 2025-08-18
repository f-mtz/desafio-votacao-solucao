package sicredi.votacao.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;
import sicredi.votacao.api.domain.Vote;
import sicredi.votacao.api.domain.VoteType;
import sicredi.votacao.api.domain.VotingSession;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/teste-domain")
public class DomainTestController {

	private static final Logger log = LoggerFactory.getLogger(DomainTestController.class);

	@GetMapping
	public Agenda exemploVotacao() {
		log.info("[DomainTestController] exemploVotacao");
		Agenda agenda = new Agenda();
		agenda.setId(1L);
		agenda.setTitle("Assembleia Geral 2025");
		agenda.setDescription("Votação da pauta de orçamento anual");
		agenda.setStatus(AgendaStatus.IN_PROGRESS);

		VotingSession session = new VotingSession();
		session.setId(10L);
		session.setStartTime(LocalDateTime.now().minusMinutes(5));
		session.setEndTime(LocalDateTime.now().plusMinutes(55));

		Vote voto1 = new Vote();
		voto1.setId(1000L);
		voto1.setAssociateId("12345678901");
		voto1.setVoteType(VoteType.SIM);

		Vote voto2 = new Vote();
		voto2.setId(1001L);
		voto2.setAssociateId("98765432100");
		voto2.setVoteType(VoteType.NAO);

		Set<Vote> votes = new HashSet<>();
		votes.add(voto1);
		votes.add(voto2);
		session.setVotes(votes);

		// Evita ciclo de serialização: agenda -> session -> agenda
		agenda.setVotingSession(session);
		// session.setAgenda(agenda); // intencionalmente não setado

		return agenda;
	}
}



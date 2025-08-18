package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sicredi.votacao.api.domain.VotingSession;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {
}



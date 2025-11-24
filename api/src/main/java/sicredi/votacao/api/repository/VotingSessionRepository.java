package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sicredi.votacao.api.domain.VotingSession;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {
}



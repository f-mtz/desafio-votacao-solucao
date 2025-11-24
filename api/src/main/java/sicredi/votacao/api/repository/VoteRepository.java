package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sicredi.votacao.api.domain.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVotingSession_IdAndAssociateId(Long votingSessionId, String associateId);
    long countByVotingSession_IdAndVoteType(Long votingSessionId, sicredi.votacao.api.domain.VoteType voteType);
}



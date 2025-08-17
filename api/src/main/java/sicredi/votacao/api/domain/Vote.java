package sicredi.votacao.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Vote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "session_id")
	private VotingSession votingSession;

	@NotBlank
	private String associateId;

	@Enumerated(EnumType.STRING)
	private VoteType voteType;

	public Vote() {
	}

	public Vote(Long id, VotingSession votingSession, String associateId, VoteType voteType) {
		this.id = id;
		this.votingSession = votingSession;
		this.associateId = associateId;
		this.voteType = voteType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VotingSession getVotingSession() {
		return votingSession;
	}

	public void setVotingSession(VotingSession votingSession) {
		this.votingSession = votingSession;
	}

	public String getAssociateId() {
		return associateId;
	}

	public void setAssociateId(String associateId) {
		this.associateId = associateId;
	}

	public VoteType getVoteType() {
		return voteType;
	}

	public void setVoteType(VoteType voteType) {
		this.voteType = voteType;
	}
}



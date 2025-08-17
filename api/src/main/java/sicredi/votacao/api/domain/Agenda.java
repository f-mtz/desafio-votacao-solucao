package sicredi.votacao.api.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Agenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	@Enumerated(EnumType.STRING)
	private AgendaStatus status = AgendaStatus.NOT_STARTED;

	@OneToOne(mappedBy = "agenda", cascade = CascadeType.ALL)
	private VotingSession votingSession;

	public Agenda() {
	}

	public Agenda(Long id, String title, String description, AgendaStatus status, VotingSession votingSession) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.status = status;
		this.votingSession = votingSession;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AgendaStatus getStatus() {
		return status;
	}

	public void setStatus(AgendaStatus status) {
		this.status = status;
	}

	public VotingSession getVotingSession() {
		return votingSession;
	}

	public void setVotingSession(VotingSession votingSession) {
		this.votingSession = votingSession;
	}
}



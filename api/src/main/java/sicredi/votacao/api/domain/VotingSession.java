package sicredi.votacao.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class VotingSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "agenda_id")
	private Agenda agenda;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	@OneToMany(mappedBy = "votingSession", cascade = jakarta.persistence.CascadeType.ALL)
	private Set<Vote> votes = new HashSet<>();

	public VotingSession() {
	}

	public VotingSession(Long id, Agenda agenda, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.agenda = agenda;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Set<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Set<Vote> votes) {
		this.votes = votes;
	}
}



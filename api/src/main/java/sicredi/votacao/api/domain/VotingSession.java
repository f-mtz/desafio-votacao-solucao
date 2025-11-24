package sicredi.votacao.api.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
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
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class VotingSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "agenda_id")
	@JsonBackReference("agenda-session")
	private Agenda agenda;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	@OneToMany(mappedBy = "votingSession", cascade = CascadeType.ALL)
	@JsonManagedReference("session-votes")
	private Set<Vote> votes = new HashSet<>();


}



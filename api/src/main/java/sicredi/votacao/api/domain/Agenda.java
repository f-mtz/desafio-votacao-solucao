package sicredi.votacao.api.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
	@JsonManagedReference("agenda-session")
	private VotingSession votingSession;

}



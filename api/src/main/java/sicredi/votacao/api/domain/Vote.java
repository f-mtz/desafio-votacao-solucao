package sicredi.votacao.api.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vote", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "associate_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Vote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "session_id")
	@JsonBackReference("session-votes")
	private VotingSession votingSession;

	@NotBlank
	@Column(name = "associate_id", nullable = false)
	private String associateId;

	@Enumerated(EnumType.STRING)
	private VoteType voteType;

}



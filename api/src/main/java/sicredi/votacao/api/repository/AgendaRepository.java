package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sicredi.votacao.api.domain.Agenda;
import sicredi.votacao.api.domain.AgendaStatus;

import java.util.List;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
	List<Agenda> findByStatus(AgendaStatus status);
}



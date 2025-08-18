package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sicredi.votacao.api.domain.Agenda;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
}



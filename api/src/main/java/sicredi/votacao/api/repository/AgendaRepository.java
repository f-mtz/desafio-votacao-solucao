package sicredi.votacao.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sicredi.votacao.api.domain.Agenda;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
}



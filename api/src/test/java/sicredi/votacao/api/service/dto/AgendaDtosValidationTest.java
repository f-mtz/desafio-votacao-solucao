package sicredi.votacao.api.service.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.voto.VoteRequest;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgendaDtosValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CreateAgendaRequest requer title e description não vazios")
    void createAgendaRequest_notBlank() {
        CreateAgendaRequest req = new CreateAgendaRequest("", " ");
        Set<ConstraintViolation<CreateAgendaRequest>> v = validator.validate(req);
        assertThat(v).hasSizeGreaterThanOrEqualTo(1);
        assertThat(v.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString))
                .containsAnyOf("title", "description");
    }

    @Test
    @DisplayName("VoteRequest requer associateId e vote não vazios")
    void voteRequest_notBlank() {
        VoteRequest req = new VoteRequest(1L, "", "");
        Set<ConstraintViolation<VoteRequest>> v = validator.validate(req);
        assertThat(v).hasSizeGreaterThanOrEqualTo(1);
        assertThat(v.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString))
                .containsAnyOf("associateId", "vote");
    }

    @Test
    @DisplayName("OpenSessionRequest.toDurationOrDefault retorna 60s quando null ou <=0")
    void openSessionRequest_defaultDuration() {
        OpenSessionRequest r1 = new OpenSessionRequest(1L, null);
        OpenSessionRequest r2 = new OpenSessionRequest(1L, 0L);
        OpenSessionRequest r3 = new OpenSessionRequest(1L, -5L);
        assertThat(r1.toDurationOrDefault()).isEqualTo(Duration.ofMinutes(1));
        assertThat(r2.toDurationOrDefault()).isEqualTo(Duration.ofMinutes(1));
        assertThat(r3.toDurationOrDefault()).isEqualTo(Duration.ofMinutes(1));
    }
}



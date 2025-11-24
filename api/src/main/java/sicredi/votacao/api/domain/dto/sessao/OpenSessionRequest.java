package sicredi.votacao.api.domain.dto.sessao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

@Schema(name = "OpenSessionRequest", description = "Dados para abertura de sessão de votação")
public record OpenSessionRequest(
		@Schema(description = "ID da pauta", example = "1") Long agendaId,
		@Schema(description = "Duração da sessão em segundos (default 60s)", example = "60") Long durationSeconds
) {
	public Duration toDurationOrDefault() {
		return durationSeconds == null || durationSeconds <= 0 ? Duration.ofMinutes(1) : Duration.ofSeconds(durationSeconds);
	}
}



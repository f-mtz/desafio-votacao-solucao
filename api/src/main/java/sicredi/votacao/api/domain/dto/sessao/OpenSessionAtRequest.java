package sicredi.votacao.api.domain.dto.sessao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OpenSessionAtRequest", description = "Dados para abertura de sessão com data/hora arbitrária")
public record OpenSessionAtRequest(
		@Schema(description = "ID da pauta", example = "1") Long agendaId,
		@Schema(description = "Início da sessão em ISO-8601 com offset (ex.: 2025-01-01T12:00:00-03:00)", example = "2025-01-01T12:00:00Z") String startTime,
		@Schema(description = "Fim da sessão em ISO-8601 com offset (opcional). Se ausente, aplica 2 horas de duração", example = "2025-01-01T14:00:00Z") String endTime
) {}





package sicredi.votacao.api.domain.dto.sessao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OpenSessionResponse", description = "Resposta da abertura de sessão")
public record OpenSessionResponse(
		@Schema(description = "ID da sessão", example = "10") Long sessionId,
		@Schema(description = "ID da pauta", example = "1") Long agendaId,
		@Schema(description = "Início da sessão", example = "2025-08-17T20:00:00") String startTime,
		@Schema(description = "Fim da sessão", example = "2025-08-17T21:00:00") String endTime
) {}



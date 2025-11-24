package sicredi.votacao.api.domain.dto.agenda;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResultResponse", description = "Resultado da votação da pauta")
public record ResultResponse(
		@Schema(description = "ID da pauta", example = "1") Long agendaId,
		@Schema(description = "ID da sessão", example = "10") Long sessionId,
		@Schema(description = "Quantidade de votos SIM", example = "42") long sim,
		@Schema(description = "Quantidade de votos NAO", example = "27") long nao,
		@Schema(description = "Status da pauta", example = "IN_PROGRESS") String status
) {}



package sicredi.votacao.api.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;

public class AgendaDtos {

	@Schema(name = "CreateAgendaRequest", description = "Dados para criação de uma pauta")
	public record CreateAgendaRequest(
			@Schema(description = "Título da pauta", example = "Assembleia Geral 2025") @NotBlank String title,
			@Schema(description = "Descrição da pauta", example = "Votação do orçamento anual") @NotBlank String description
	) {}

	@Schema(name = "CreateAgendaResponse", description = "Resposta da criação da pauta")
	public record CreateAgendaResponse(
			@Schema(description = "Identificador da pauta", example = "1") Long id,
			@Schema(description = "Título", example = "Assembleia Geral 2025") String title,
			@Schema(description = "Descrição", example = "Votação do orçamento anual") String description,
			@Schema(description = "Status da pauta", example = "NOT_STARTED") String status
	) {}

	@Schema(name = "OpenSessionRequest", description = "Dados para abertura de sessão de votação")
	public record OpenSessionRequest(
			@Schema(description = "ID da pauta", example = "1") Long agendaId,
			@Schema(description = "Duração da sessão em segundos (default 60s)", example = "60") Long durationSeconds
	) {
		public Duration toDurationOrDefault() {
			return durationSeconds == null || durationSeconds <= 0 ? Duration.ofMinutes(1) : Duration.ofSeconds(durationSeconds);
		}
	}

	@Schema(name = "OpenSessionResponse", description = "Resposta da abertura de sessão")
	public record OpenSessionResponse(
			@Schema(description = "ID da sessão", example = "10") Long sessionId,
			@Schema(description = "ID da pauta", example = "1") Long agendaId,
			@Schema(description = "Início da sessão", example = "2025-08-17T20:00:00") String startTime,
			@Schema(description = "Fim da sessão", example = "2025-08-17T21:00:00") String endTime
	) {}

	@Schema(name = "VoteRequest", description = "Dados do voto")
	public record VoteRequest(
			@Schema(description = "ID da sessão", example = "10") Long sessionId,
			@Schema(description = "Identificador do associado (CPF ou ID)", example = "11111111111") @NotBlank String associateId,
			@Schema(description = "Voto do associado (SIM/NAO)", example = "SIM", allowableValues = {"SIM", "NAO"}) @NotBlank String vote
	) {}

	@Schema(name = "VoteResponse", description = "Resposta do voto registrado")
	public record VoteResponse(
			@Schema(description = "ID do voto", example = "1000") Long voteId,
			@Schema(description = "ID da sessão", example = "10") Long sessionId,
			@Schema(description = "ID do associado", example = "11111111111") String associateId,
			@Schema(description = "Voto computado", example = "SIM") String vote
	) {}

	@Schema(name = "ResultResponse", description = "Resultado da votação da pauta")
	public record ResultResponse(
			@Schema(description = "ID da pauta", example = "1") Long agendaId,
			@Schema(description = "ID da sessão", example = "10") Long sessionId,
			@Schema(description = "Quantidade de votos SIM", example = "42") long sim,
			@Schema(description = "Quantidade de votos NAO", example = "27") long nao,
			@Schema(description = "Status da pauta", example = "IN_PROGRESS") String status
	) {}
}



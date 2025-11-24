package sicredi.votacao.api.domain.dto.agenda;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateAgendaResponse", description = "Resposta da criação da pauta")
public record CreateAgendaResponse(
		@Schema(description = "Identificador da pauta", example = "1") Long id,
		@Schema(description = "Título", example = "Assembleia Geral 2025") String title,
		@Schema(description = "Descrição", example = "Votação do orçamento anual") String description,
		@Schema(description = "Status da pauta", example = "NOT_STARTED") String status
) {}



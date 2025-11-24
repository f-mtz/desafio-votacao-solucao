package sicredi.votacao.api.domain.dto.agenda;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateAgendaRequest", description = "Dados para criação de uma pauta")
public record CreateAgendaRequest(
		@Schema(description = "Título da pauta", example = "Assembleia Geral 2025") @NotBlank String title,
		@Schema(description = "Descrição da pauta", example = "Votação do orçamento anual") @NotBlank String description
) {}



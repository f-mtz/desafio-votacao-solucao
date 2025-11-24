package sicredi.votacao.api.domain.dto.voto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "VoteRequest", description = "Dados do voto")
public record VoteRequest(
		@Schema(description = "ID da sessão", example = "10") Long sessionId,
		@Schema(description = "Identificador do associado (CPF ou ID)", example = "11111111111") @NotBlank String associateId,
		@Schema(description = "Voto do associado (SIM/NAO)", example = "SIM", allowableValues = {"SIM", "NAO"}) @NotBlank String vote
) {}



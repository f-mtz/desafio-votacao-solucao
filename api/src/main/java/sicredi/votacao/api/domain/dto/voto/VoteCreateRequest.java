package sicredi.votacao.api.domain.dto.voto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "VoteCreateRequest", description = "Dados do voto para criação via sessão na URL")
public record VoteCreateRequest(
		@Schema(description = "Identificador do associado (CPF ou ID)", example = "11111111111") @NotBlank String associateId,
		@Schema(description = "Voto do associado (SIM/NAO)", example = "SIM", allowableValues = {"SIM", "NAO"}) @NotBlank String vote
) {}



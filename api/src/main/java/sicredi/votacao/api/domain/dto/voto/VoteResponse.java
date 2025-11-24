package sicredi.votacao.api.domain.dto.voto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VoteResponse", description = "Resposta do voto registrado")
public record VoteResponse(
		@Schema(description = "ID do voto", example = "1000") Long voteId,
		@Schema(description = "ID da sessão", example = "10") Long sessionId,
		@Schema(description = "ID do associado", example = "11111111111") String associateId,
		@Schema(description = "Voto computado", example = "SIM") String vote
) {}



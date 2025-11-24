package sicredi.votacao.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.service.VotoService;
import sicredi.votacao.api.domain.dto.voto.VoteCreateRequest;
import sicredi.votacao.api.domain.dto.voto.VoteRequest;
import sicredi.votacao.api.domain.dto.voto.VoteResponse;

@RestController
@RequestMapping("/api/votes")
public class VotoController {

	private final VotoService votoService;

	public VotoController(VotoService votoService) {
		this.votoService = votoService;
	}

	@Operation(
			summary = "Registra um voto",
			description = "Registra voto SIM/NAO para um associado na sessão",
			responses = @ApiResponse(responseCode = "200", description = "Voto computado",
					content = @Content(schema = @Schema(implementation = VoteResponse.class)))
	)
	@PostMapping("/votar")
	public ResponseEntity<VoteResponse> vote(
			@Validated @RequestBody
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do voto",
					required = true,
					content = @Content(
							schema = @Schema(implementation = VoteRequest.class),
							examples = @ExampleObject(value = "{\"sessionId\":10,\"associateId\":\"11111111111\",\"vote\":\"SIM\"}")
					)
			) VoteRequest request) {
		return ResponseEntity.ok(votoService.vote(request));
	}

	@Operation(
			summary = "Registra um voto (REST) por sessão",
			description = "Cria voto em uma sessão informada na URL",
			responses = @ApiResponse(responseCode = "200", description = "Voto computado",
					content = @Content(schema = @Schema(implementation = VoteResponse.class)))
	)
	@PostMapping("/{sessionId}/votos")
	public ResponseEntity<VoteResponse> voteBySessionPath(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do voto (sem sessionId)",
					required = true,
					content = @Content(
							schema = @Schema(implementation = VoteCreateRequest.class),
							examples = @ExampleObject(value = "{\"associateId\":\"11111111111\",\"vote\":\"SIM\"}")
					)
			) @Validated @RequestBody VoteCreateRequest request,
			@org.springframework.web.bind.annotation.PathVariable Long sessionId) {
		var combined = new VoteRequest(sessionId, request.associateId(), request.vote());
		return ResponseEntity.ok(votoService.vote(combined));
	}
}



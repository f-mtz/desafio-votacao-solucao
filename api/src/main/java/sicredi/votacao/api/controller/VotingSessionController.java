package sicredi.votacao.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionRequest;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionResponse;
import sicredi.votacao.api.domain.dto.sessao.OpenSessionAtRequest;
import sicredi.votacao.api.service.VotingSessionService;

@RestController
@RequestMapping("/api/v1/session")
public class VotingSessionController {

	@Autowired
	private VotingSessionService votingSessionService;

	@Operation(
			summary = "Abre uma sessão de votação",
			description = "Abre sessão com duração informada em segundos ou 60s por padrão",
			responses = @ApiResponse(responseCode = "200", description = "Sessão aberta",
					content = @Content(schema = @Schema(implementation = OpenSessionResponse.class)))
	)
	@PostMapping("/abrir-sessao")
	public ResponseEntity<OpenSessionResponse> openSession(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados para abertura da sessão",
					required = true,
					content = @Content(
							schema = @Schema(implementation = OpenSessionRequest.class),
							examples = @ExampleObject(value = "{\"agendaId\":1,\"durationSeconds\":60}")
					)
			) @RequestBody OpenSessionRequest request) {
		return ResponseEntity.ok(votingSessionService.openSession(request));
	}

	@Operation(
			summary = "Abre uma sessão com data/hora arbitrária para agendamentos futuros",
			description = "Cria sessão com startTime/endTime informados (ISO-8601 com offset). Se endTime não for enviado, aplica duração padrão de 2h a partir de startTime",
			responses = @ApiResponse(responseCode = "200", description = "Sessão aberta",
					content = @Content(schema = @Schema(implementation = OpenSessionResponse.class)))
	)
	@PostMapping("/criar-sessao")
	public ResponseEntity<OpenSessionResponse> openSessionAt(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados para criação arbitrária da sessão",
					required = true,
					content = @Content(
							schema = @Schema(implementation = OpenSessionAtRequest.class),
							examples = @ExampleObject(value = "{\"agendaId\":1,\"startTime\":\"2025-01-01T12:00:00Z\",\"endTime\":\"2025-01-01T14:00:00Z\"}")
					)
			) @RequestBody OpenSessionAtRequest request) {
		return ResponseEntity.ok(votingSessionService.openSessionAt(request));
	}
}



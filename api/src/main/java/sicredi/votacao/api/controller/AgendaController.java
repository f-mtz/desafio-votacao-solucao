package sicredi.votacao.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.service.AgendaService;
import sicredi.votacao.api.service.dto.AgendaDtos;
import sicredi.votacao.api.service.exception.ApiException;

import java.util.Map;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

	private final AgendaService agendaService;

	public AgendaController(AgendaService agendaService) {
		this.agendaService = agendaService;
	}

	@Operation(
			summary = "Cria uma nova pauta",
			description = "Cadastra uma pauta com título e descrição",
			responses = @ApiResponse(responseCode = "201", description = "Pauta criada",
					content = @Content(schema = @Schema(implementation = AgendaDtos.CreateAgendaResponse.class)))
	)
	@PostMapping
	public ResponseEntity<AgendaDtos.CreateAgendaResponse> create(
			@Validated @RequestBody
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados da pauta",
					required = true,
					content = @Content(
							schema = @Schema(implementation = AgendaDtos.CreateAgendaRequest.class),
							examples = @ExampleObject(value = "{\"title\":\"Assembleia Geral 2026\",\"description\":\"Votação do orçamento anual\"}")
					)
			) AgendaDtos.CreateAgendaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(agendaService.createAgenda(request));
	}

	@Operation(
			summary = "Abre uma sessão de votação",
			description = "Abre sessão com duração informada em segundos ou 60s por padrão",
			responses = @ApiResponse(responseCode = "200", description = "Sessão aberta",
					content = @Content(schema = @Schema(implementation = AgendaDtos.OpenSessionResponse.class)))
	)
	@PostMapping("/abrir-sessao")
	public ResponseEntity<AgendaDtos.OpenSessionResponse> openSession(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados para abertura da sessão",
					required = true,
					content = @Content(
							schema = @Schema(implementation = AgendaDtos.OpenSessionRequest.class),
							examples = @ExampleObject(value = "{\"agendaId\":1,\"durationSeconds\":60}")
					)
			) @RequestBody AgendaDtos.OpenSessionRequest request) {
		return ResponseEntity.ok(agendaService.openSession(request));
	}

	@Operation(
			summary = "Registra um voto",
			description = "Registra voto SIM/NAO para um associado na sessão",
			responses = @ApiResponse(responseCode = "200", description = "Voto computado",
					content = @Content(schema = @Schema(implementation = AgendaDtos.VoteResponse.class)))
	)
	@PostMapping("/votar")
	public ResponseEntity<AgendaDtos.VoteResponse> vote(
			@Validated @RequestBody
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados do voto",
					required = true,
					content = @Content(
							schema = @Schema(implementation = AgendaDtos.VoteRequest.class),
							examples = @ExampleObject(value = "{\"sessionId\":10,\"associateId\":\"11111111111\",\"vote\":\"SIM\"}")
					)
			) AgendaDtos.VoteRequest request) {
		return ResponseEntity.ok(agendaService.vote(request));
	}

	@Operation(
			summary = "Resultado da votação",
			description = "Retorna contagem de votos por opção e status",
			responses = @ApiResponse(responseCode = "200", description = "Resultado",
					content = @Content(schema = @Schema(implementation = AgendaDtos.ResultResponse.class)))
	)
	@GetMapping("/{agendaId}/resultado")
	public ResponseEntity<AgendaDtos.ResultResponse> result(@PathVariable Long agendaId) {
		return ResponseEntity.ok(agendaService.result(agendaId));
	}

	@Operation(
			summary = "Finaliza uma pauta",
			description = "Atualiza o status da pauta para FINISHED",
			responses = @ApiResponse(responseCode = "200", description = "Pauta finalizada",
					content = @Content(schema = @Schema(implementation = AgendaDtos.CreateAgendaResponse.class)))
	)
	@PutMapping("/{agendaId}/finalizar")
	public ResponseEntity<AgendaDtos.CreateAgendaResponse> finish(@PathVariable Long agendaId) {
		return ResponseEntity.ok(agendaService.finishAgenda(agendaId));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(Map.of(
				"error", ex.getMessage(),
				"status", ex.getStatus().value()
		));
	}
}



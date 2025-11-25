package sicredi.votacao.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.service.AgendaService;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaRequest;
import sicredi.votacao.api.domain.dto.agenda.CreateAgendaResponse;
import sicredi.votacao.api.domain.dto.agenda.ResultResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {

	@Autowired
	private final AgendaService agendaService;

	public AgendaController(AgendaService agendaService) {
		this.agendaService = agendaService;
	}

	@Operation(
			summary = "Cria uma nova pauta",
			description = "Cadastra uma pauta com título e descrição",
			responses = @ApiResponse(responseCode = "201", description = "Pauta criada",
					content = @Content(schema = @Schema(implementation = CreateAgendaResponse.class)))
	)
	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
	public CreateAgendaResponse create(
			@Validated @RequestBody
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Dados da pauta",
					required = true,
					content = @Content(
							schema = @Schema(implementation = CreateAgendaRequest.class),
							examples = @ExampleObject(value = "{\"title\":\"Assembleia Geral 2026\",\"description\":\"Votação do orçamento anual\"}")
					)
			) CreateAgendaRequest request) {
		return agendaService.createAgenda(request);
	}

	@Operation(
			summary = "Resultado da votação",
			description = "Retorna contagem de votos por opção e status",
			responses = @ApiResponse(responseCode = "200", description = "Resultado",
					content = @Content(schema = @Schema(implementation = ResultResponse.class)))
	)
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/{agendaId}/resultado")
	public ResultResponse result(@PathVariable Long agendaId) {
		return agendaService.result(agendaId);
	}

	@Operation(
			summary = "Finaliza uma pauta",
			description = "Atualiza o status da pauta para FINISHED",
			responses = @ApiResponse(responseCode = "200", description = "Pauta finalizada",
					content = @Content(schema = @Schema(implementation = CreateAgendaResponse.class)))
	)
	@PutMapping("/{agendaId}/finalizar")
	public ResponseEntity<CreateAgendaResponse> finish(@PathVariable Long agendaId) {
		return ResponseEntity.ok(agendaService.finishAgenda(agendaId));
	}

	@Operation(
			summary = "Lista pautas em andamento",
			description = "Retorna todas as pautas com status IN_PROGRESS",
			responses = @ApiResponse(responseCode = "200", description = "Lista de pautas",
					content = @Content(schema = @Schema(implementation = CreateAgendaResponse.class)))
	)
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/in-progress")
	public List<CreateAgendaResponse> listInProgress() {
		return agendaService.listInProgress();
	}
	
	

}



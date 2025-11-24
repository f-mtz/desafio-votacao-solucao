package sicredi.votacao.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sicredi.votacao.api.integration.AssociateNotFoundException;
import sicredi.votacao.api.integration.AssociateStatus;
import sicredi.votacao.api.integration.AssociateValidatorClient;
import sicredi.votacao.api.domain.exception.ApiException;

import java.util.Map;

@RestController
@RequestMapping("/api/associates")
public class AssociateController {

	private final AssociateValidatorClient associateValidatorClient;
	private static final Logger log = LoggerFactory.getLogger(AssociateController.class);

	public AssociateController(AssociateValidatorClient associateValidatorClient) {
		this.associateValidatorClient = associateValidatorClient;
	}

	@Operation(
		summary = "Verifica status de CPF do associado",
		description = "Retorna se o associado está apto a votar (200) ou, se não estiver/CPF inválido, retorna 404 com payload estruturado.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Associado apto",
					content = @Content(schema = @Schema(implementation = StatusResponse.class))),
			@ApiResponse(responseCode = "404", description = "Associado não encontrado ou não apto",
					content = @Content(schema = @Schema(implementation = ErrorStatusResponse.class)))
		}
	)
	@GetMapping("/{associateId}/status")
	public ResponseEntity<?> checkStatus(@PathVariable String associateId) {
		log.info("Check status associateId={}", associateId);
		AssociateStatus status = associateValidatorClient.checkAssociateStatus(associateId);
		if (status == AssociateStatus.UNABLE_TO_VOTE) {
			log.info("Associate UNABLE_TO_VOTE associateId={}", associateId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorStatusResponse("Associado não está apto a votar", HttpStatus.NOT_FOUND.value(), status.name()));
		}
		log.info("Associate ABLE_TO_VOTE associateId={}", associateId);
		return ResponseEntity.ok(new StatusResponse(status.name()));
	}

	public record StatusResponse(String status) {}

	public record ErrorStatusResponse(String message, int code, String status) {}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorStatusResponse> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus())
				.body(new ErrorStatusResponse(ex.getMessage(), ex.getStatus().value(), AssociateStatus.UNABLE_TO_VOTE.name()));
	}

	@ExceptionHandler(AssociateNotFoundException.class)
	public ResponseEntity<ErrorStatusResponse> handleAssociateNotFound(AssociateNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorStatusResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), AssociateStatus.UNABLE_TO_VOTE.name()));
	}
}



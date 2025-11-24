package sicredi.votacao.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sicredi.votacao.api.domain.exception.ApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(Map.of(
				"error", ex.getMessage(),
				"status", ex.getStatus().value()
		));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
		var errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.groupingBy(
						e -> e.getField(),
						Collectors.mapping(e -> e.getDefaultMessage(), Collectors.toList())
				));
		Map<String, Object> body = new HashMap<>();
		body.put("error", "Dados inválidos");
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("fields", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
				"error", "Erro interno do servidor",
				"status", HttpStatus.INTERNAL_SERVER_ERROR.value()
		));
	}
}



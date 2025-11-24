package sicredi.votacao.api.integration;

import org.springframework.http.HttpStatus;
import sicredi.votacao.api.domain.exception.ApiException;

public class AssociateNotFoundException extends ApiException {
	public AssociateNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}



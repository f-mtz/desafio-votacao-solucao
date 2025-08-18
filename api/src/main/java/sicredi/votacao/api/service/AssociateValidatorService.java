package sicredi.votacao.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sicredi.votacao.api.integration.AssociateStatus;
import sicredi.votacao.api.integration.AssociateValidatorClient;
import sicredi.votacao.api.service.exception.ApiException;
import org.springframework.http.HttpStatus;

@Service
public class AssociateValidatorService {

	private static final Logger log = LoggerFactory.getLogger(AssociateValidatorService.class);
	private final AssociateValidatorClient associateValidatorClient;

	public AssociateValidatorService(AssociateValidatorClient associateValidatorClient) {
		this.associateValidatorClient = associateValidatorClient;
	}

	public void validateAssociateOrThrow(String associateId) {
		AssociateStatus status = associateValidatorClient.checkAssociateStatus(associateId);
		if (status == AssociateStatus.UNABLE_TO_VOTE) {
			log.info("Associado [{}] não apto a votar (UNABLE_TO_VOTE)", associateId);
			// Requisito do bônus: quando UNABLE_TO_VOTE, retornar 404 também
			throw new ApiException(HttpStatus.NOT_FOUND, "Associado não está apto a votar");
		}
		log.info("Associado [{}] apto a votar (ABLE_TO_VOTE)", associateId);
	}
}



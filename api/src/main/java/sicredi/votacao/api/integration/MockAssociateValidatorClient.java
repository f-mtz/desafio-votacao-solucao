package sicredi.votacao.api.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Primary
public class MockAssociateValidatorClient implements AssociateValidatorClient {

	private static final Logger log = LoggerFactory.getLogger(MockAssociateValidatorClient.class);
	private final Random random = new Random();

	@Override
	public AssociateStatus checkAssociateStatus(String associateId) {
		if (!isValidCPF(associateId)) {
			log.warn("Validação CPF: inválido [{}]", associateId);
			throw new AssociateNotFoundException("CPF inválido");
		}
		AssociateStatus decision = random.nextBoolean() ? AssociateStatus.ABLE_TO_VOTE : AssociateStatus.UNABLE_TO_VOTE;
		log.info("Validação CPF: [{}] -> {}", associateId, decision);
		return decision;
	}

	private boolean isValidCPF(String cpf) {
		return cpf != null && cpf.matches("\\d{11}") && !cpf.matches("(\\d)\\1{10}");
	}
}



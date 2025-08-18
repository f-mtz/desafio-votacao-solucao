package sicredi.votacao.api.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class OpenApiConfig {

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;

	@Bean
	public OpenAPI apiDocs() {
		return new OpenAPI()
				.addServersItem(new Server().url(baseUrl))
				.info(new Info()
						.title("Desafio Votação API")
						.description("API para gerenciamento de pautas, sessões e votos")
						.version("v1")
						.license(new License().name("MIT"))
						.contact(new Contact().name("Sicredi Desafio")))
				.externalDocs(new ExternalDocumentation()
						.description("Repositório")
						.url("https://example.com"));
	}
}



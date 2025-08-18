package sicredi.votacao.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {

	private final String baseUrl;

	public UrlBuilder(@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
		this.baseUrl = trimTrailingSlash(baseUrl);
	}

	public String build(String path) {
		String normalized = path == null ? "" : path.trim();
		if (normalized.isEmpty()) {
			return baseUrl;
		}
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		return baseUrl + normalized;
	}

	private String trimTrailingSlash(String url) {
		if (url == null || url.isBlank()) {
			return "";
		}
		while (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}
}



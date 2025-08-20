package com.gutendx.service;

import com.gutendx.dto.GutendxApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GutendxApiService {

    private static final Logger logger = LoggerFactory.getLogger(GutendxApiService.class);

    @Value("${gutendx.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public GutendxApiService() {
        this.restTemplate = new RestTemplate();
    }

    public GutendxApiResponse searchBooks(String query) throws ApiException {
        try {
            logger.info("Buscando libros con query: {}", query);

            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/books")
                    .queryParam("search", query)
                    .build()
                    .toUriString();

            logger.debug("URL de búsqueda: {}", url);

            ResponseEntity<GutendxApiResponse> response = restTemplate.getForEntity(
                    url, GutendxApiResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Búsqueda exitosa. Libros encontrados: {}", response.getBody().getCount());
                return response.getBody();
            } else {
                throw new ApiException("Respuesta inválida de la API de Gutendx");
            }

        } catch (RestClientException e) {
            logger.error("Error al comunicarse con la API de Gutendx", e);
            throw new ApiException("Error de conexión con la API de Gutendx: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al buscar libros", e);
            throw new ApiException("Error inesperado al buscar libros: " + e.getMessage(), e);
        }
    }
}

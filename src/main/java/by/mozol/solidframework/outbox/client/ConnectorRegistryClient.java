package by.mozol.solidframework.outbox.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectorRegistryClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${kafka.connect.url}")
    private String connectUrl;

    @Getter
    @Value("${kafka.connect.connector.name}")
    private String connectorName;

    @Value("${kafka.connect.connector.config-file}")
    private String configFilePath;

    public Mono<Void> waitForConnectHealth() {
        return webClient.get()
                .uri(connectUrl + "/")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(3))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(r -> log.warn("Kafka Connect not ready, retry {}/10", r.totalRetries() + 1)))
                .then()
                .doOnSuccess(v -> log.info("Kafka Connect is healthy at {}", connectUrl));
    }

    public Mono<Void> deleteConnectorIfExists() {
        return webClient.get()
                .uri(connectUrl + "/connectors/" + connectorName)
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.info("Connector '{}' exists. Deleting...", connectorName);
                        return webClient.delete()
                                .uri(connectUrl + "/connectors/" + connectorName)
                                .retrieve()
                                .toBodilessEntity()
                                .then(Mono.fromRunnable(() -> log.info("Deleted existing connector '{}'", connectorName)));
                    } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.info("Connector '{}' does not exist. Skipping delete.", connectorName);
                        return Mono.empty();
                    } else {
                        return Mono.error(new RuntimeException("Unexpected response: " + response.getStatusCode()));
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Error checking/deleting connector (might not exist): {}", e.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    public Mono<Void> createConnectorFromFile() {
        Resource resource = resourceLoader.getResource("file:" + configFilePath);
        if (!resource.exists()) {
            return Mono.error(new IOException("Connector config file not found: " + configFilePath));
        }

        try {
            JsonNode config = objectMapper.readTree(resource.getInputStream());
            log.info("Loaded connector config from {}", configFilePath);

            return webClient.post()
                    .uri(connectUrl + "/connectors")
                    .bodyValue(config)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> log.info("Successfully created connector '{}'", connectorName))
                    .then();

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read connector config file: " + configFilePath, e));
        }
    }
}
package by.mozol.solidframework.outbox.startup;

import by.mozol.solidframework.outbox.client.ConnectorRegistryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectorSetupService {
    private final ConnectorRegistryClient connectorRegistryClient;

    @EventListener(ApplicationReadyEvent.class)
    public void setupConnectorOnStartup() {
        log.info("Starting Kafka Connect connector setup...");

        connectorRegistryClient.waitForConnectHealth()
                .then(connectorRegistryClient.deleteConnectorIfExists())
                .then(connectorRegistryClient.createConnectorFromFile())
                .doOnSuccess(v -> log.info("Connector '{}' successfully configured.", connectorRegistryClient.getConnectorName()))
                .doOnError(e -> log.error("Failed to configure connector: {}", e.getMessage()))
                .block();
    }
}
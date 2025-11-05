package by.mozol.solidframework.outbox.config;

import io.debezium.config.Configuration;
import io.debezium.connector.postgresql.PostgresConnector;
import io.debezium.connector.postgresql.PostgresConnectorConfig;
import io.debezium.transforms.outbox.EventRouter;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class OutboxConnectorConfig {

    @Bean
    public static Configuration debeziumConnectorConfiguration() {
        return Configuration.create()
                // Connector identity
                .with("name", "outbox-connector")

                // Core connector
                .with("connector.class", PostgresConnector.class)
                .with("tasks.max", 1)

                // PostgreSQL connection
                .with(PostgresConnectorConfig.HOSTNAME, "postgres")
                .with(PostgresConnectorConfig.PORT, 5432)
                .with(PostgresConnectorConfig.USER, "user")
                .with(PostgresConnectorConfig.PASSWORD, "password")
                .with(PostgresConnectorConfig.DATABASE_NAME, "mydb")
                .with("database.server.name", "dbserver1")

                // Table filtering
                .with(PostgresConnectorConfig.TABLE_INCLUDE_LIST, "public.outbox_events")

                // Logical decoding
                .with(PostgresConnectorConfig.PLUGIN_NAME, "pgoutput")

                // === Outbox EventRouter SMT === (only for kafka connect)
//                .with("transforms", "outbox")
//                .with("transforms.outbox.type", EventRouter.class)
//                .with("transforms.outbox.route.topic.replacement", "outbox.events")
//                .with("transforms.outbox.route.by.field", "aggregatetype")
//                .with("transforms.outbox.table.field.event.key", "aggregateid")
//                .with("transforms.outbox.table.field.event.type", "type")
//                .with("transforms.outbox.table.field.event.payload", "payload")

                // Internal topic prefix (for Debezium metadata)
                .with("topic.prefix", "solid")

                .build();
    }
}
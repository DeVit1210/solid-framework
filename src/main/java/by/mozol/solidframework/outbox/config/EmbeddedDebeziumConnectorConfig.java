package by.mozol.solidframework.outbox.config;

import io.debezium.connector.postgresql.PostgresConnectorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@org.springframework.context.annotation.Configuration
public class EmbeddedDebeziumConnectorConfig {

    @Value("${debezium.kafka.offset-file:/tmp/debezium-offsets.dat}")
    private String offsetFile;

    @Bean
    public static Properties embeddedDebeziumConnectorConfiguration() {
        Properties props = new Properties();

        // Connector identity
        props.setProperty("name", "outbox-connector");

        // Core connector
        props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
        props.setProperty("tasks.max", "1");

        props.setProperty(PostgresConnectorConfig.HOSTNAME.name(), "postgres");
        props.setProperty(PostgresConnectorConfig.PORT.name(), "5433");
        props.setProperty(PostgresConnectorConfig.USER.name(), "user");
        props.setProperty(PostgresConnectorConfig.PASSWORD.name(), "password");
        props.setProperty(PostgresConnectorConfig.DATABASE_NAME.name(), "mydb");

        // === Table Filtering ===
        props.setProperty(PostgresConnectorConfig.TABLE_INCLUDE_LIST.name(), "public.outbox_events");

        // === Logical Decoding ===
        props.setProperty(PostgresConnectorConfig.PLUGIN_NAME.name(), "pgoutput");

        // === Outbox EventRouter SMT ===
        props.setProperty("transforms", "outbox");
        props.setProperty("transforms.outbox.type", "io.debezium.transforms.outbox.EventRouter");
        props.setProperty("transforms.outbox.route.topic.replacement", "outbox.events");
        props.setProperty("transforms.outbox.route.by.field", "aggregatetype");
        props.setProperty("transforms.outbox.table.field.event.key", "aggregateid");
        props.setProperty("transforms.outbox.table.field.event.type", "type");
        props.setProperty("transforms.outbox.table.field.event.payload", "payload");

        // Internal topic prefix
        props.setProperty("topic.prefix", "solid");

        return props;
    }
}
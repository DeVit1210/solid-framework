package by.mozol.solidframework.outbox.runner;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class DebeziumEmbeddedRunner implements ApplicationRunner {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Properties embeddedDebeziumConnectorConfiguration;

    private static void accept(ChangeEvent<String, String> event) {
        System.out.println(event.value());
    }

    @Override
    public void run(ApplicationArguments args) {
        try (DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(embeddedDebeziumConnectorConfiguration)
                .notifying(DebeziumEmbeddedRunner::accept)
                .build()) {

            try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
                executor.execute(engine);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    // This is where the magic happens
//    private void handleAndTransformRecord(SourceRecord record) {
//        if (record.value() == null) return;
//
//        Struct valueStruct = (Struct) record.value();
//        Struct after = valueStruct.getStruct("after");
//        if (after == null) return;
//
//        try {
//            // === Extract fields (same as EventRouter config) ===
//            String aggregateType = after.getString(outboxConfig.getRouteByField());  // "aggregatetype"
//            String aggregateId = after.getString(outboxConfig.getKeyField());     // "aggregateid"
//            String eventType = after.getString(outboxConfig.getTypeField());    // "type"
//            String payloadJson = after.getString(outboxConfig.getPayloadField()); // "payload"
//
//            if (aggregateType == null || payloadJson == null) return;
//
//            // === Build transformed topic ===
//            String targetTopic = outboxConfig.getTopicPrefix() + "." + aggregateType.toLowerCase();
//
//            // === Build headers (like EventRouter) ===
//            Headers headers = new RecordHeaders();
//            headers.add("eventType", eventType.getBytes(StandardCharsets.UTF_8));
//
//            // === Create NEW SourceRecord with transformed data ===
//            SourceRecord transformedRecord = new SourceRecord(
//                    record.sourcePartition(),
//                    record.sourceOffset(),
//                    targetTopic,                     // ← routed topic
//                    null,                            // partition (null = use Kafka default)
//                    aggregateId,                     // ← key
//                    null,                            // key schema
//                    payloadJson,                     // ← clean payload
//                    null,                            // value schema
//                    record.timestamp(),
//                    headers                          // ← event type header
//            );
//
//            // === Now you have a "SMT-transformed" record! ===
//            System.out.println("Transformed Record:");
//            System.out.println("  Topic: " + transformedRecord.topic());
//            System.out.println("  Key: " + transformedRecord.key());
//            System.out.println("  Value: " + transformedRecord.value());
//            transformedRecord.headers().forEach(h ->
//                    System.out.println("  Header: " + h.key() + " = " + new String(h.value()))
//            );
//
//            // === Send to Kafka ===
//            kafkaTemplate.send(transformedRecord);
//
//        } catch (Exception e) {
//            System.err.println("Transformation failed: " + e.getMessage());
//        }
//    }
//
//    @PreDestroy
//    public void stop() {
//        // Engine stop logic
//    }
}
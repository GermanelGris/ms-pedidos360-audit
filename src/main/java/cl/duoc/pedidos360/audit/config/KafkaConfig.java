package cl.duoc.pedidos360.audit.config;

import java.time.Duration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String AUDIT_TIMELINE = "audit.timeline";

    /** Dead Letter Topic propio de este consumidor. */
    public static final String DEAD_LETTER_TOPIC = "audit.timeline.audit.DLT";

    @Bean
    NewTopic auditTimelineTopic() {
        return TopicBuilder.name(AUDIT_TIMELINE)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "compact,delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(30).toMillis()))
                .build();
    }

    @Bean
    NewTopic auditDeadLetterTopic() {
        return TopicBuilder.name(DEAD_LETTER_TOPIC)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(14).toMillis()))
                .build();
    }

    /** 2 reintentos y luego DLT con el mensaje original y los metadatos del error. */
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(DEAD_LETTER_TOPIC, record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }
}

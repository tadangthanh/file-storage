package vn.thanh.metadataservice.kafkaconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    @Value("${app.kafka.metadata-update-topic}")
    private String metadataUpdateTopic;
    @Value("${app.kafka.metadata-cleanup-topic}")
    private String metadataCleanupTopic;
    @Value("${app.kafka.metadata-delete-topic}")
    private String metadataDeleteTopic;

    @Bean
    NewTopic metadataUpdateTopic() {
        // topicname, partition number,replication number (thuong bang so broker server)
        return new NewTopic(metadataUpdateTopic, 2, (short) 3);
    }

    @Bean
    NewTopic metadataCleanupTopic() {
        // topicname, partition number,replication number (thuong bang so broker server)
        return new NewTopic(metadataCleanupTopic, 2, (short) 3);
    }

    @Bean
    NewTopic metadataDeleteTopic() {
        // topicname, partition number,replication number (thuong bang so broker server)
        return new NewTopic(metadataDeleteTopic, 2, (short) 3);
    }

    // cái này sẽ dùng in memory retry, tức là k tạo topic retry
//    @Bean
//    DefaultErrorHandler errorHandler(KafkaOperations<String, Object> template) {
//        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
//                template,
//                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
//        );
//        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
//    }

}

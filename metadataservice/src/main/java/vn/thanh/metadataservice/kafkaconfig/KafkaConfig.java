package vn.thanh.metadataservice.kafkaconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Value("${app.kafka.metadata-update-topic}")
    private String metadataUpdateTopic;
    @Value("${app.kafka.metadata-cleanup-topic}")
    private String metadataCleanupTopic;
    @Value("${app.kafka.delete-metadata-topic}")
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
}

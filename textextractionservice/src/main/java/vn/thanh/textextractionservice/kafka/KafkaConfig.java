package vn.thanh.textextractionservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Value("${app.kafka.document-extracted-topic}")
    private String documentExtractedTopic;

    @Bean
    NewTopic documentExtractedTopic() {
        // topicname, partition number,replication number (thuong bang so broker server)
        return new NewTopic(documentExtractedTopic, 2, (short) 3);
    }
}

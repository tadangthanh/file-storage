package vn.thanh.storageservice.kafkaconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    NewTopic metadata() {
        // topicname, partition number,replication number (thuong bang so broker server)
        return new NewTopic("metadata", 2, (short) 3);
    }
}

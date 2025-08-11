package vn.thanh.storageservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.dto.MetadataUpdate;
import vn.thanh.storageservice.service.IKafkaService;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements IKafkaService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void eventUpdateMetadata(MetadataUpdate metadataUpdate) {
        log.info("sen event to metadata service to update: {}",metadataUpdate.toString());
        kafkaTemplate.send("metadata",metadataUpdate).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("send event success");
            } else {
                log.error("Send Kafka fail: metadataId={}, payload={}, reason={}",
                        metadataUpdate.getId(), metadataUpdate, ex.getMessage());

                System.err.println("Gửi thất bại: " + ex.getMessage());
                // retry / log / lưu DB
            }
        });;
    }
}

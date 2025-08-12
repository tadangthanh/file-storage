package vn.thanh.metadataservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent extends BaseEntity {
    private String aggregateType; // ví dụ: METADATA

    private String topic;

    private String messageKey; // optional

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status; // PENDING, SUCCESS, FAILED

    private int retryCount;

    private LocalDateTime lastAttemptAt;
}

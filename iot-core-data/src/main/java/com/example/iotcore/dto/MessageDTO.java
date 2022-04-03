package com.example.iotcore.dto;

import com.example.iotcore.domain.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link Message} entity.
 */
@Schema(name = "MessageDTO", description = "A DTO for the Message entity.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO implements Serializable {

    private Long id;

    private String content;

    private Instant createdTimeStamp;

    private Long deviceId;

    private Long topicId;
}

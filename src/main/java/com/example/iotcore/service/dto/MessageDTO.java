package com.example.iotcore.service.dto;

import com.example.iotcore.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link Message} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO implements Serializable {

    private Long id;

    private String content;

    private Instant createdTimeStamp;

    private Integer messageType;

    private DeviceDTO device;

    private TopicDTO topic;
}

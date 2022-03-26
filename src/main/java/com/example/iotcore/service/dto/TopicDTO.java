package com.example.iotcorenew.service.dto;

import com.example.iotcorenew.domain.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link Topic} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicDTO implements Serializable {

    private Long id;

    private String name;
}

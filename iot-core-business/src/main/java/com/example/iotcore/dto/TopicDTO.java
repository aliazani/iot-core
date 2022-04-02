package com.example.iotcore.dto;

import com.example.iotcore.domain.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "TopicDTO", description = "A DTO for the Topic entity.")
public class TopicDTO implements Serializable {

    private Long id;

    private String name;
}

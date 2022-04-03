package com.example.iotcore.mapper;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Topic} and its DTO {@link TopicDTO}.
 */
@Mapper(componentModel = "spring")
public interface TopicMapper extends EntityMapper<TopicDTO, Topic> {
}

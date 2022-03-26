package com.example.iotcore.service.mapper;

import com.example.iotcore.domain.Message;
import com.example.iotcore.service.dto.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring", uses = {DeviceMapper.class, TopicMapper.class})
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    @Mapping(target = "device", source = "device", qualifiedByName = "id")
    @Mapping(target = "topic", source = "topic", qualifiedByName = "id")
    MessageDTO toDto(Message s);
}

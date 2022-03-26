package com.example.iotcorenew.service.mapper;

import com.example.iotcorenew.domain.Message;
import com.example.iotcorenew.service.dto.MessageDTO;
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

package com.example.iotcore.mapper;

import com.example.iotcore.domain.Message;
import com.example.iotcore.dto.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring", uses = {DeviceMapper.class, TopicMapper.class})
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    MessageDTO toDto(Message s);
}

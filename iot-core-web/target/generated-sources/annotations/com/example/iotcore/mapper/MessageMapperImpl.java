package com.example.iotcore.mapper;

import com.example.iotcore.domain.Message;
import com.example.iotcore.dto.MessageDTO;
import com.example.iotcore.dto.MessageDTO.MessageDTOBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-03-27T03:15:34+0430",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.2 (Oracle Corporation)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private TopicMapper topicMapper;

    @Override
    public Message toEntity(MessageDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Message message = new Message();

        message.setId( dto.getId() );
        message.setContent( dto.getContent() );
        message.setCreatedTimeStamp( dto.getCreatedTimeStamp() );
        message.setMessageType( dto.getMessageType() );
        message.setDevice( deviceMapper.toEntity( dto.getDevice() ) );
        message.setTopic( topicMapper.toEntity( dto.getTopic() ) );

        return message;
    }

    @Override
    public List<Message> toEntity(List<MessageDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Message> list = new ArrayList<Message>( dtoList.size() );
        for ( MessageDTO messageDTO : dtoList ) {
            list.add( toEntity( messageDTO ) );
        }

        return list;
    }

    @Override
    public List<MessageDTO> toDto(List<Message> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<MessageDTO> list = new ArrayList<MessageDTO>( entityList.size() );
        for ( Message message : entityList ) {
            list.add( toDto( message ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Message entity, MessageDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getContent() != null ) {
            entity.setContent( dto.getContent() );
        }
        if ( dto.getCreatedTimeStamp() != null ) {
            entity.setCreatedTimeStamp( dto.getCreatedTimeStamp() );
        }
        if ( dto.getMessageType() != null ) {
            entity.setMessageType( dto.getMessageType() );
        }
        if ( dto.getDevice() != null ) {
            entity.setDevice( deviceMapper.toEntity( dto.getDevice() ) );
        }
        if ( dto.getTopic() != null ) {
            entity.setTopic( topicMapper.toEntity( dto.getTopic() ) );
        }
    }

    @Override
    public MessageDTO toDto(Message s) {
        if ( s == null ) {
            return null;
        }

        MessageDTOBuilder messageDTO = MessageDTO.builder();

        messageDTO.device( deviceMapper.toDtoId( s.getDevice() ) );
        messageDTO.topic( topicMapper.toDtoId( s.getTopic() ) );
        messageDTO.id( s.getId() );
        messageDTO.content( s.getContent() );
        messageDTO.createdTimeStamp( s.getCreatedTimeStamp() );
        messageDTO.messageType( s.getMessageType() );

        return messageDTO.build();
    }
}

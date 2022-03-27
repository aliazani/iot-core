package com.example.iotcore.mapper;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.dto.TopicDTO.TopicDTOBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-03-27T03:15:34+0430",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.2 (Oracle Corporation)"
)
@Component
public class TopicMapperImpl implements TopicMapper {

    @Override
    public Topic toEntity(TopicDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Topic topic = new Topic();

        topic.setId( dto.getId() );
        topic.setName( dto.getName() );

        return topic;
    }

    @Override
    public TopicDTO toDto(Topic entity) {
        if ( entity == null ) {
            return null;
        }

        TopicDTOBuilder topicDTO = TopicDTO.builder();

        topicDTO.id( entity.getId() );
        topicDTO.name( entity.getName() );

        return topicDTO.build();
    }

    @Override
    public List<Topic> toEntity(List<TopicDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Topic> list = new ArrayList<Topic>( dtoList.size() );
        for ( TopicDTO topicDTO : dtoList ) {
            list.add( toEntity( topicDTO ) );
        }

        return list;
    }

    @Override
    public List<TopicDTO> toDto(List<Topic> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<TopicDTO> list = new ArrayList<TopicDTO>( entityList.size() );
        for ( Topic topic : entityList ) {
            list.add( toDto( topic ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Topic entity, TopicDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
    }

    @Override
    public TopicDTO toDtoId(Topic topic) {
        if ( topic == null ) {
            return null;
        }

        TopicDTOBuilder topicDTO = TopicDTO.builder();

        topicDTO.id( topic.getId() );

        return topicDTO.build();
    }
}

package com.example.iotcore.service.impl;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.mapper.TopicMapper;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Topic}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class TopicServiceImpl implements TopicService {
    private final TopicRepository topicRepository;

    private final TopicMapper topicMapper;

    @Override
    public TopicDTO save(TopicDTO topicDTO) {
        log.debug("Request to save Topic : {}", topicDTO);

        Topic topic = topicMapper.toEntity(topicDTO);
        topic = topicRepository.save(topic);

        return topicMapper.toDto(topic);
    }

    @Override
    public Optional<TopicDTO> partialUpdate(TopicDTO topicDTO) {
        log.debug("Request to partially update Topic : {}", topicDTO);

        return topicRepository
                .findById(topicDTO.getId())
                .map(existingTopic -> {
                    topicMapper.partialUpdate(existingTopic, topicDTO);

                    return existingTopic;
                })
                .map(topicRepository::save)
                .map(topicMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TopicDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Topics");

        return topicRepository.findAll(pageable).map(topicMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TopicDTO> findOne(Long id) {
        log.debug("Request to get Topic : {}", id);

        return topicRepository.findById(id).map(topicMapper::toDto);
    }

    @Override
    public Optional<TopicDTO> findOneByName(String name) {
        log.debug("Request to get Topic : {}", name);
        Optional<Topic> topic = topicRepository.findByNameIgnoreCase(name);

        return Optional.ofNullable(
                topicMapper.toDto(topic.orElse(null))
        );
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Topic : {}", id);

        topicRepository.deleteById(id);
    }
}

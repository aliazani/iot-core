package com.example.iotcore.service.async.impl;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.mapper.TopicMapper;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.async.TopicServiceAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service Implementation for managing {@link Topic}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
@Profile("async")
public class TopicServiceAsyncAsyncImpl implements TopicServiceAsync {

    private final TopicRepository topicRepository;

    private final TopicMapper topicMapper;

    @Async
    @Override
    public CompletableFuture<TopicDTO> save(TopicDTO topicDTO) {
        log.debug("Request to save Topic : {}", topicDTO);

        return CompletableFuture.supplyAsync(
                () -> topicMapper.toDto(topicRepository.save(topicMapper.toEntity(topicDTO)))
        );
    }

    @Async
    @Override
    public CompletableFuture<Optional<TopicDTO>> partialUpdate(TopicDTO topicDTO) {
        log.debug("Request to partially update Topic : {}", topicDTO);

        return CompletableFuture.completedFuture(
                topicRepository
                        .findById(topicDTO.getId())
                        .map(existingTopic -> {
                            topicMapper.partialUpdate(existingTopic, topicDTO);

                            return existingTopic;
                        })
                        .map(topicRepository::save)
                        .map(topicMapper::toDto));
    }

    @Async
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Page<TopicDTO>> findAll(Pageable pageable) {
        log.debug("Request to get all Topics");

        return CompletableFuture.completedFuture(
                topicRepository.findAll(pageable).map(topicMapper::toDto)
        );
    }

    @Async("taskExecutor")
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Optional<TopicDTO>> findOne(Long id) {
        log.debug("Request to get Topic : {}", id);

        return CompletableFuture.supplyAsync(() ->
            topicRepository.findById(id).map(topicMapper::toDto)
        );
    }

    @Async
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Topic : {}", id);

        CompletableFuture.runAsync(() -> topicRepository.deleteById(id));
    }
}


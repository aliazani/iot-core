package com.example.iotcore.service.async.impl;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service Interface for managing {@link Topic}.
 */
@Profile("async")
public interface TopicServiceAsync {
    /**
     * Save a topic.
     *
     * @param topicDTO the entity to save.
     * @return the persisted entity.
     */
    CompletableFuture<TopicDTO> save(TopicDTO topicDTO);

    /**
     * Partially updates a topic.
     *
     * @param topicDTO the entity to update partially.
     * @return the persisted entity.
     */
    CompletableFuture<Optional<TopicDTO>> partialUpdate(TopicDTO topicDTO);

    /**
     * Get all the topics.
     *
     * @return the list of entities.
     */
    CompletableFuture<Page<TopicDTO>> findAll(Pageable pageable);

    /**
     * Get the "id" topic.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    CompletableFuture<Optional<TopicDTO>> findOne(Long id);

    /**
     * Delete the "id" topic.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

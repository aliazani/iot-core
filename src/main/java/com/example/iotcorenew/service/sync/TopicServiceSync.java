package com.example.iotcorenew.service.sync;

import com.example.iotcorenew.domain.Topic;
import com.example.iotcorenew.service.dto.TopicDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service Interface for managing {@link Topic}.
 */
public interface TopicServiceSync {
    /**
     * Save a topic.
     *
     * @param topicDTO the entity to save.
     * @return the persisted entity.
     */
    TopicDTO save(TopicDTO topicDTO);

    /**
     * Partially updates a topic.
     *
     * @param topicDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TopicDTO> partialUpdate(TopicDTO topicDTO);

    /**
     * Get all the topics.
     *
     * @return the list of entities.
     */
    Page<TopicDTO> findAll(Pageable pageable);

    /**
     * Get the "id" topic.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TopicDTO> findOne(Long id);

    /**
     * Delete the "id" topic.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

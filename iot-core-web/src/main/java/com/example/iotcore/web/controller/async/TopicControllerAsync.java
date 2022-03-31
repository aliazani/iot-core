package com.example.iotcore.web.controller.async;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.service.async.TopicServiceAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing {@link Topic}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Profile("async")
public class TopicControllerAsync {
    private static final String ENTITY_NAME = "topic";

    private final TopicServiceAsync topicServiceAsync;

    /**
     * {@code POST  /topics} : Create a new topic.
     *
     * @param topicDTO the topicDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new topicDTO, or with status {@code 400 (Bad Request)} if the topic has already an ID.
     */
    @PostMapping("/topics")
    public CompletableFuture<ResponseEntity<TopicDTO>> createTopic(@Valid @RequestBody TopicDTO topicDTO) {
        log.debug("REST request to save Topic : {}", topicDTO);

        CompletableFuture<TopicDTO> result = topicServiceAsync.save(topicDTO);

        return result.thenApply(topicDTO1 ->
        {
            try {
                return ResponseEntity.created(new URI("/api/topics/" + topicDTO1.getId())).body(topicDTO1);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return ResponseEntity.badRequest().build();
        });
    }

    /**
     * {@code PUT  /topics/:id} : Updates an existing topic.
     *
     * @param id       the id of the topicDTO to save.
     * @param topicDTO the topicDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topicDTO,
     * or with status {@code 400 (Bad Request)} if the topicDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the topicDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/topics/{id}")
    public CompletableFuture<ResponseEntity<TopicDTO>> updateTopic(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to update Topic : {}, {}", id, topicDTO);

        CompletableFuture<TopicDTO> result = topicServiceAsync.save(topicDTO);

        return result.thenApply(topicDTO1 -> ResponseEntity.ok().body(topicDTO1));
    }

    /**
     * {@code PATCH  /topics/:id} : Partial updates given fields of an existing topic, field will ignore if it is null
     *
     * @param id       the id of the topicDTO to save.
     * @param topicDTO the topicDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topicDTO,
     * or with status {@code 400 (Bad Request)} if the topicDTO is not valid,
     * or with status {@code 404 (Not Found)} if the topicDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the topicDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/topics/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public CompletableFuture<ResponseEntity<Optional<TopicDTO>>> partialUpdateTopic(@PathVariable(value = "id", required = false) final Long id, @NotNull @RequestBody TopicDTO topicDTO) throws URISyntaxException, ExecutionException, InterruptedException {
        log.debug("REST request to partial update Topic partially : {}, {}", id, topicDTO);

        CompletableFuture<Optional<TopicDTO>> result = topicServiceAsync.partialUpdate(topicDTO);

        return result.thenApply(optionalTopicDTO -> ResponseEntity.ok().body(optionalTopicDTO));
    }

    /**
     * {@code GET  /topics} : get all the topics.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of topics in body.
     */
    @GetMapping("/topics")
    public CompletableFuture<Object> getAllTopics(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all Topics");

        CompletableFuture<Page<TopicDTO>> pageCompletableFuture = topicServiceAsync.findAll(pageable);

        return pageCompletableFuture.thenApply(page -> ResponseEntity.ok().body(page.getContent()));
    }

    /**
     * {@code GET  /topics/:id} : get the "id" topic.
     *
     * @param id the id of the topicDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the topicDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/topics/{id}")
    public CompletableFuture<ResponseEntity<Optional<TopicDTO>>> getTopic(@PathVariable Long id) {
        log.debug("REST request to get Topic : {}", id);

        CompletableFuture<Optional<TopicDTO>> topicDTO = topicServiceAsync.findOne(id);

        return topicDTO.thenApply(optionalTopicDTO -> ResponseEntity.ok().body(optionalTopicDTO));

    }

    /**
     * {@code DELETE  /topics/:id} : delete the "id" topic.
     *
     * @param id the id of the topicDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/topics/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTopic(@PathVariable Long id) {
        log.debug("REST request to delete Topic : {}", id);

        topicServiceAsync.delete(id);

        return CompletableFuture.completedFuture(ResponseEntity.noContent().build());
    }
}

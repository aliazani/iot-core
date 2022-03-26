package com.example.iotcore.web.controller.sync;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.service.dto.TopicDTO;
import com.example.iotcore.service.sync.TopicServiceSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link Topic}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TopicControllerSync {
    private static final String ENTITY_NAME = "topic";

    private final TopicServiceSync topicService;

//    private final TopicRepository topicRepository;

    /**
     * {@code POST  /topics} : Create a new topic.
     *
     * @param topicDTO the topicDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new topicDTO, or with status {@code 400 (Bad Request)} if the topic has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/topics")
    public ResponseEntity<TopicDTO> createTopic(@Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to save Topic : {}", topicDTO);

//        if (topicDTO.getId() != null) {
//            throw new BadRequestAlertException("A new topic cannot already have an ID", ENTITY_NAME, "idexists");
//        }
        TopicDTO result = topicService.save(topicDTO);

        return ResponseEntity.created(new URI("/api/topics/" + result.getId())).body(result);
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
    public ResponseEntity<TopicDTO> updateTopic(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to update Topic : {}, {}", id, topicDTO);

//        if (topicDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, topicDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!topicRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }

        TopicDTO result = topicService.save(topicDTO);

        return ResponseEntity.ok().body(result);
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
    public ResponseEntity<TopicDTO> partialUpdateTopic(@PathVariable(value = "id", required = false) final Long id, @NotNull @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to partial update Topic partially : {}, {}", id, topicDTO);

//        if (topicDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        if (!Objects.equals(id, topicDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
//        }
//
//        if (!topicRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }

        Optional<TopicDTO> result = topicService.partialUpdate(topicDTO);

        return ResponseEntity.ok().body(result.orElse(null));
    }

    /**
     * {@code GET  /topics} : get all the topics.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of topics in body.
     */
    @GetMapping("/topics")
    public ResponseEntity<List<TopicDTO>> getAllTopics(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all Topics");

        Page<TopicDTO> page = topicService.findAll(pageable);

        return ResponseEntity.ok().body(page.getContent());
    }

    /**
     * {@code GET  /topics/:id} : get the "id" topic.
     *
     * @param id the id of the topicDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the topicDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicDTO> getTopic(@PathVariable Long id) {
        log.debug("REST request to get Topic : {}", id);

        Optional<TopicDTO> topicDTO = topicService.findOne(id);

        return ResponseEntity.ok().body(topicDTO.orElse(null));
    }

    /**
     * {@code DELETE  /topics/:id} : delete the "id" topic.
     *
     * @param id the id of the topicDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.debug("REST request to delete Topic : {}", id);

        topicService.delete(id);

        return ResponseEntity.noContent().build();
    }

}

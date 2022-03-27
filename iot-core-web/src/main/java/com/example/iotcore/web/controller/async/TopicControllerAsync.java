package com.example.iotcore.web.controller.async;

import com.example.iotcore.domain.Topic;

/**
 * REST controller for managing {@link Topic}.
 */
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api")
public class TopicControllerAsync {
//    private static final String ENTITY_NAME = "topic";
//
//    private final TopicServiceAsync topicServiceAsync;
//
////    private final TopicRepository topicRepository;
//
//    /**
//     * {@code POST  /topics} : Create a new topic.
//     *
//     * @param topicDTO the topicDTO to create.
//     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new topicDTO, or with status {@code 400 (Bad Request)} if the topic has already an ID.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PostMapping("/topics")
//    public ResponseEntity<CompletableFuture<TopicDTO>> createTopic(@Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
//        log.debug("REST request to save Topic : {}", topicDTO);
//
////        if (topicDTO.getId() != null) {
////            throw new BadRequestAlertException("A new topic cannot already have an ID", ENTITY_NAME, "idexists");
////        }
//        CompletableFuture<TopicDTO> result = topicServiceAsync.save(topicDTO);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(result);
//    }
//
//    /**
//     * {@code PUT  /topics/:id} : Updates an existing topic.
//     *
//     * @param id       the id of the topicDTO to save.
//     * @param topicDTO the topicDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topicDTO,
//     * or with status {@code 400 (Bad Request)} if the topicDTO is not valid,
//     * or with status {@code 500 (Internal Server Error)} if the topicDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PutMapping("/topics/{id}")
//    public ResponseEntity<TopicDTO> updateTopic(
//            @PathVariable(value = "id", required = false) final Long id,
//            @Valid @RequestBody TopicDTO topicDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to update Topic : {}, {}", id, topicDTO);
//
////        if (topicDTO.getId() == null) {
////            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
////        }
////        if (!Objects.equals(id, topicDTO.getId())) {
////            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
////        }
////
////        if (!topicRepository.existsById(id)) {
////            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
////        }
//
//        TopicDTO result = topicServiceAsync.save(topicDTO);
//
//        return ResponseEntity
//                .ok()
//                .body(result);
//    }
//
//    /**
//     * {@code PATCH  /topics/:id} : Partial updates given fields of an existing topic, field will ignore if it is null
//     *
//     * @param id       the id of the topicDTO to save.
//     * @param topicDTO the topicDTO to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topicDTO,
//     * or with status {@code 400 (Bad Request)} if the topicDTO is not valid,
//     * or with status {@code 404 (Not Found)} if the topicDTO is not found,
//     * or with status {@code 500 (Internal Server Error)} if the topicDTO couldn't be updated.
//     * @throws URISyntaxException if the Location URI syntax is incorrect.
//     */
//    @PatchMapping(value = "/topics/{id}", consumes = {"application/json", "application/merge-patch+json"})
//    public ResponseEntity<TopicDTO> partialUpdateTopic(
//            @PathVariable(value = "id", required = false) final Long id,
//            @NotNull @RequestBody TopicDTO topicDTO
//    ) throws URISyntaxException {
//        log.debug("REST request to partial update Topic partially : {}, {}", id, topicDTO);
//
////        if (topicDTO.getId() == null) {
////            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
////        }
////        if (!Objects.equals(id, topicDTO.getId())) {
////            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
////        }
////
////        if (!topicRepository.existsById(id)) {
////            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
////        }
//
//        Optional<TopicDTO> result = topicServiceAsync.partialUpdate(topicDTO);
//
//        return ResponseEntity.ok().body(result.orElse(null));
//    }
//
//    /**
//     * {@code GET  /topics} : get all the topics.
//     *
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of topics in body.
//     */
//    @GetMapping("/topics")
//    public CompletableFuture<LinkedList<TopicDTO>> getAllTopics() {
//        log.debug("REST request to get all Topics");
//
//        return topicServiceAsync.findAll();
//    }
//
//    /**
//     * {@code GET  /topics/:id} : get the "id" topic.
//     *
//     * @param id the id of the topicDTO to retrieve.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the topicDTO, or with status {@code 404 (Not Found)}.
//     */
//    @GetMapping("/topics/{id}")
//    public ResponseEntity<TopicDTO> getTopic(@PathVariable Long id) {
//        log.debug("REST request to get Topic : {}", id);
//
//        Optional<TopicDTO> topicDTO = topicServiceAsync.findOne(id);
//
//        return ResponseEntity.ok().body(topicDTO.orElse(null));
//    }
//
//    /**
//     * {@code DELETE  /topics/:id} : delete the "id" topic.
//     *
//     * @param id the id of the topicDTO to delete.
//     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
//     */
//    @DeleteMapping("/topics/{id}")
//    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
//        log.debug("REST request to delete Topic : {}", id);
//
//        topicServiceAsync.delete(id);
//
//        return ResponseEntity
//                .noContent()
//                .build();
//    }

}

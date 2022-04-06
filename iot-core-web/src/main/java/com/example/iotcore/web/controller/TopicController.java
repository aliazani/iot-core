package com.example.iotcore.web.controller;

import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.TopicService;
import com.example.iotcore.util.HeaderUtil;
import com.example.iotcore.util.PaginationUtil;
import com.example.iotcore.util.ResponseUtil;
import com.example.iotcore.web.errors.BadRequestAlertException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Topic}.
 */
@Tag(name = "Topic", description = "Topic Management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TopicController {

    private static final String ENTITY_NAME = "topic";
    private final TopicService topicService;
    private final TopicRepository topicRepository;
    @Value("${clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /topics} : Create a new topic.
     *
     * @param topicDTO the topicDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new topicDTO, or with status {@code 400 (Bad Request)} if the topic has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */

    @Operation(summary = "Create new topic", description = "Create new topic",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Topic created successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class))}
                            , headers = @Header(name = "Location", description = "The URL of the created topic")
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (A new topic cannot already have an ID /" +
                                    " Topic with same name already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "DeviceDTO",
                    value = "{\"name\":\"string\"}")}))
    @PostMapping("/topics")
    public ResponseEntity<TopicDTO> createTopic(@Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to save Topic : {}", topicDTO);

        if (topicDTO.getId() != null)
            throw new BadRequestAlertException("A new %s cannot already have an ID".formatted(ENTITY_NAME)
                    , ENTITY_NAME, "idexists");
        if (topicRepository.findByNameIgnoreCase(topicDTO.getName()).isPresent())
            throw new BadRequestAlertException("A new %s cannot have existed name".formatted(ENTITY_NAME)
                    , ENTITY_NAME, "nameexists");

        TopicDTO result = topicService.save(topicDTO);

        return ResponseEntity
                .created(new URI("/api/topics/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /topics/:id} : Updates an existing topic.
     *
     * @param id       the id of the topicDTO to save.
     * @param topicDTO the topicDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topicDTO,
     * or with status {@code 400 (Bad Request)} if the topicDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the topicDTO couldn't be updated.
     */

    @Operation(summary = "Update a topic", description = "Update a topic",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Topic updated successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Topic is not valid /" +
                                    " Topic with same name already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PutMapping("/topics/{id}")
    public ResponseEntity<TopicDTO> updateTopic(@PathVariable(value = "id", required = false) final Long id,
                                                @Valid @RequestBody TopicDTO topicDTO) {
        log.debug("REST request to update Topic : {}, {}", id, topicDTO);

        checkIdValidity(topicDTO, id);
        if (topicRepository.findByNameIgnoreCase(topicDTO.getName()).isPresent())
            throw new BadRequestAlertException("name already exists"
                    , ENTITY_NAME, "nameexists");

        TopicDTO result = topicService.save(topicDTO);

        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        topicDTO.getId().toString()))
                .body(result);
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
     */

    @Operation(summary = "Patch a topic", description = "Patch a topic",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Topic patched successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Topic is not valid " +
                                    "/ Topic with same name already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Not found (to patch)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PatchMapping(value = "/topics/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<TopicDTO> partialUpdateTopic(@PathVariable(value = "id", required = false) final Long id,
                                                       @NotNull @RequestBody TopicDTO topicDTO) {
        log.debug("REST request to partial update Topic partially : {}, {}", id, topicDTO);

        checkIdValidity(topicDTO, id);
        if (topicRepository.findByNameIgnoreCase(topicDTO.getName()).isPresent())
            throw new BadRequestAlertException("name already exists"
                    , ENTITY_NAME, "nameexists");

        Optional<TopicDTO> result = topicService.partialUpdate(topicDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true,
                        ENTITY_NAME, topicDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /topics} : get all the topics.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of topics in body.
     */
    @Operation(summary = "Get all topics paged", description = "Get all topics paged",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a page of topics",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/topics")
    public ResponseEntity<List<TopicDTO>> getAllTopics(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Topics");

        Page<TopicDTO> page = topicService.findAll(pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /topics/:id} : get the "id" topic.
     *
     * @param id the id of the topicDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the topicDTO, or with status {@code 404 (Not Found)}.
     */

    @Operation(summary = "Find a topic by ID", description = "Find a topic by ID",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a topic",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Topic Not found",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicDTO> getTopic(@PathVariable Long id) {
        log.debug("REST request to get Topic : {}", id);

        Optional<TopicDTO> topicDTO = topicService.findOne(id);

        return ResponseUtil.wrapOrNotFound(topicDTO);
    }

    /**
     * {@code DELETE  /topics/:id} : delete the "id" topic.
     *
     * @param id the id of the topicDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a topic", description = "Delete a topic",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Topic successfully deleted",
                            content = {@Content(mediaType = "application/json")}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.debug("REST request to delete Topic : {}", id);

        topicService.delete(id);

        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                        ENTITY_NAME, id.toString()))
                .build();
    }

    private void checkIdValidity(TopicDTO topicDTO, Long id) {
        if (topicDTO.getId() == null)
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        if (!Objects.equals(id, topicDTO.getId()))
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");

        if (!topicRepository.existsById(id))
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    }
}

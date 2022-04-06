package com.example.iotcore.web.controller;

import com.example.iotcore.domain.Message;
import com.example.iotcore.dto.MessageDTO;
import com.example.iotcore.repository.MessageRepository;
import com.example.iotcore.service.MessageService;
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
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Message}.
 */
@Tag(name = "Message", description = "Message Management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MessageController {


    private static final String ENTITY_NAME = "message";
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    @Value("${clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /messages} : Create a new message.
     *
     * @param messageDTO the messageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageDTO, or with status {@code 400 (Bad Request)} if the message has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create new message", description = "Create new message",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Message created successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDTO.class))}
                            , headers = @Header(name = "Location", description = "The URL of the created message")
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (A new message cannot already have an ID)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "MessageDTO",
                    value = "{\"content\": \"string\",\"deviceId\": 0,\"topicId\": 0}")}))
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> createMessage(@Valid @RequestBody MessageDTO messageDTO) throws URISyntaxException {
        log.debug("REST request to save Message : {}", messageDTO);

        if (messageDTO.getId() != null)
            throw new BadRequestAlertException("A new %s cannot already have an ID".formatted(ENTITY_NAME)
                    , ENTITY_NAME, "idexists");

        if (messageDTO.getCreatedTimeStamp() == null)
            messageDTO.setCreatedTimeStamp(Instant.now());

        MessageDTO result = messageService.save(messageDTO);

        return ResponseEntity
                .created(new URI("/api/messages/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /messages/:id} : Updates an existing message.
     *
     * @param id         the id of the messageDTO to save.
     * @param messageDTO the messageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageDTO,
     * or with status {@code 400 (Bad Request)} if the messageDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the messageDTO couldn't be updated.
     */
    @Operation(summary = "Update a message", description = "Update a message",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Message updated successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Message is not valid)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "MessageDTO",
                    value = "{\"id\": 0,\"content\": \"string\",\"deviceId\": 0,\"topicId\": 0}")}))
    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> updateMessage(@PathVariable(value = "id", required = false) final Long id,
                                                    @Valid @RequestBody MessageDTO messageDTO) {
        log.debug("REST request to update Message : {}, {}", id, messageDTO);

        checkIdValidity(messageDTO, id);
        MessageDTO result = messageService.save(messageDTO);

        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        messageDTO.getId().toString()))
                .body(result);
    }

    /**
     * {@code PATCH  /messages/:id} : Partial updates given fields of an existing message, field will ignore if it is null
     *
     * @param id         the id of the messageDTO to save.
     * @param messageDTO the messageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated messageDTO,
     * or with status {@code 400 (Bad Request)} if the messageDTO is not valid,
     * or with status {@code 404 (Not Found)} if the messageDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the messageDTO couldn't be updated.
     */
    @Operation(summary = "Patch a message", description = "Patch a message",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Message patched successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Message is not valid)",
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "MessageDTO",
                    value = "{\"id\": 0,\"content\": \"string\",\"deviceId\": 0,\"topicId\": 0}")}))
    @PatchMapping(value = "/messages/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<MessageDTO> partialUpdateMessage(@PathVariable(value = "id", required = false) final Long id,
                                                           @NotNull @RequestBody MessageDTO messageDTO) {
        log.debug("REST request to partial update Message partially : {}, {}", id, messageDTO);

        checkIdValidity(messageDTO, id);
        Optional<MessageDTO> result = messageService.partialUpdate(messageDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        messageDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /messages} : get all the messages.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messages in body.
     */
    @Operation(summary = "Get all messages paged", description = "Get all messages paged",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a page of messages",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getAllMessages(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Messages");

        Page<MessageDTO> page = messageService.findAll(pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /messages/:id} : get the "id" message.
     *
     * @param id the id of the messageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Find a message by ID", description = "Find a message by ID",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a message",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDTO.class))}
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
    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> getMessage(@PathVariable Long id) {
        log.debug("REST request to get Message : {}", id);

        Optional<MessageDTO> messageDTO = messageService.findOne(id);

        return ResponseUtil.wrapOrNotFound(messageDTO);
    }

    /**
     * {@code DELETE  /messages/:id} : delete the "id" message.
     *
     * @param id the id of the messageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a message", description = "Delete a message",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Message successfully deleted",
                            content = {@Content(mediaType = "application/json")}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        log.debug("REST request to delete Message : {}", id);

        messageService.delete(id);

        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                        ENTITY_NAME, id.toString()))
                .build();
    }

    private void checkIdValidity(MessageDTO messageDTO, Long id) {
        if (messageDTO.getId() == null)
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        if (!Objects.equals(id, messageDTO.getId()))
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");

        if (!messageRepository.existsById(id))
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    }
}

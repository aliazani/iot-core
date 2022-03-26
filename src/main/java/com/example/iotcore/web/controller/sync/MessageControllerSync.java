package com.example.iotcore.web.controller.sync;

import com.example.iotcore.domain.Message;
import com.example.iotcore.service.dto.MessageDTO;
import com.example.iotcore.service.sync.MessageServiceSync;
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
 * REST controller for managing {@link Message}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MessageControllerSync {

    private static final String ENTITY_NAME = "message";

    private final MessageServiceSync messageService;


    /**
     * {@code POST  /messages} : Create a new message.
     *
     * @param messageDTO the messageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new messageDTO, or with status {@code 400 (Bad Request)} if the message has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> createMessage(@Valid @RequestBody MessageDTO messageDTO) throws URISyntaxException {
        log.debug("REST request to save Message : {}", messageDTO);

        MessageDTO result = messageService.save(messageDTO);

        return ResponseEntity
                .created(new URI("/api/messages/" + result.getId()))
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
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> updateMessage(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody MessageDTO messageDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Message : {}, {}", id, messageDTO);

        MessageDTO result = messageService.save(messageDTO);

        return ResponseEntity
                .ok()
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
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/messages/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<MessageDTO> partialUpdateMessage(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody MessageDTO messageDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Message partially : {}, {}", id, messageDTO);

        Optional<MessageDTO> result = messageService.partialUpdate(messageDTO);

        return ResponseEntity.ok()
                .body(result.orElse(null));
    }

    /**
     * {@code GET  /messages} : get all the messages.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messages in body.
     */
    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getAllMessages(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Messages");

        Page<MessageDTO> page = messageService.findAll(pageable);

        return ResponseEntity.ok().body(page.getContent());
    }

    /**
     * {@code GET  /messages/:id} : get the "id" message.
     *
     * @param id the id of the messageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the messageDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> getMessage(@PathVariable Long id) {
        log.debug("REST request to get Message : {}", id);

        Optional<MessageDTO> messageDTO = messageService.findOne(id);

        return ResponseEntity.ok()
                .body(messageDTO.orElse(null));
    }

    /**
     * {@code DELETE  /messages/:id} : delete the "id" message.
     *
     * @param id the id of the messageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        log.debug("REST request to delete Message : {}", id);

        messageService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

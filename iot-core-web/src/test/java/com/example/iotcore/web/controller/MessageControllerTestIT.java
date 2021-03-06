package com.example.iotcore.web.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.domain.Message;
import com.example.iotcore.dto.DeviceDTO;
import com.example.iotcore.dto.MessageDTO;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.repository.MessageRepository;
import com.example.iotcore.service.MessageService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final MessageDTO MESSAGE_DTO_1 = new MessageDTO();
    private static final MessageDTO MESSAGE_DTO_2 = new MessageDTO();
    private static final String ENTITY_NAME = "message";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MessageService messageService;

    @Autowired
    MessageRepository messageRepository;

    MessageDTO messageDTO3;

    @Value("${application.clientApp.name}")
    private String applicationName;


    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        MESSAGE_DTO_1.setId(1L);
        MESSAGE_DTO_1.setContent("message1");
        MESSAGE_DTO_1.setCreatedTimeStamp(Instant.parse("2015-04-13T11:43:47.00Z"));
        MESSAGE_DTO_1.setDevice(DeviceDTO.builder()
                .id(1L)
                .macAddress("00:00:00:00:00:00")
                .build());
        MESSAGE_DTO_1.setTopic(TopicDTO.builder()
                .id(1L)
                .name("topic1")
                .build());

        MESSAGE_DTO_2.setId(2L);
        MESSAGE_DTO_2.setContent("message2");
        MESSAGE_DTO_2.setCreatedTimeStamp(Instant.parse("2016-04-13T11:43:47.00Z"));
        MESSAGE_DTO_2.setDevice(DeviceDTO.builder()
                .id(2L)
                .macAddress("11:11:11:11:11:11")
                .build());
        MESSAGE_DTO_2.setTopic(TopicDTO.builder()
                .id(2L)
                .name("topic2")
                .build());


        messageDTO3 = MessageDTO.builder()
                .id(3L)
                .content("Forth Message")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(1L).build())
                .topic(TopicDTO.builder().id(2L).build())
                .build();
    }

    @Test
    void createMessage() throws Exception {
        // given
        MessageDTO messageDTO = MessageDTO.builder()
                .content(messageDTO3.getContent())
                .device(messageDTO3.getDevice())
                .topic(messageDTO3.getTopic())
                .createdTimeStamp(messageDTO3.getCreatedTimeStamp())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(messageDTO3.getId().intValue()))
                .andExpect(jsonPath("$.content").value(messageDTO3.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(messageDTO3.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(messageDTO3.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(messageDTO3.getTopic().getId()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.created".formatted(applicationName, ENTITY_NAME)))
        ;

        // then
        assertThat(messageRepository.existsById(messageDTO3.getId())).isTrue();
        assertThat(messageRepository.findAll()).hasSize(3);
    }

    @Test
    void createMessage_nonExistingTimeStamp() throws Exception {
        // given
        MessageDTO messageDTO = MessageDTO.builder()
                .content(messageDTO3.getContent())
                .device(messageDTO3.getDevice())
                .topic(messageDTO3.getTopic())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(messageDTO3.getId().intValue() + 1))
                .andExpect(jsonPath("$.content").value(messageDTO3.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").isNotEmpty())
                .andExpect(jsonPath("$.device.id").value(messageDTO3.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(messageDTO3.getTopic().getId()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.created".formatted(applicationName, ENTITY_NAME)))
        ;

        // then
        assertThat(messageRepository.existsById(messageDTO3.getId() + 1)).isTrue();
        assertThat(messageRepository.findAll()).hasSize(3);
    }

    @Test
    void createMessage_existingId() throws Exception {
        // given
        MessageDTO messageDTO = MessageDTO.builder()
                .id(messageDTO3.getId())
                .content(messageDTO3.getContent())
                .device(messageDTO3.getDevice())
                .topic(messageDTO3.getTopic())
                .createdTimeStamp(messageDTO3.getCreatedTimeStamp())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idexists"))
        ;

        // then
        assertThat(messageRepository.existsById(messageDTO3.getId())).isFalse();
        assertThat(messageRepository.findAll()).hasSize(2);
    }

    @Test
    void updateMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(MESSAGE_DTO_1.getId())
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL_ID, updatedMessageDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedMessageDTO.getId().intValue()))
                .andExpect(jsonPath("$.content").value(updatedMessageDTO.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(updatedMessageDTO.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(updatedMessageDTO.getDevice().getId()))
                .andExpect(jsonPath("$.device.macAddress").value(MESSAGE_DTO_1.getDevice().getMacAddress()))
                .andExpect(jsonPath("$.topic.id").value(updatedMessageDTO.getTopic().getId()))
                .andExpect(jsonPath("$.topic.name").value(MESSAGE_DTO_1.getTopic().getName()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Message message = messageRepository.findById(MESSAGE_DTO_1.getId()).get();
        assertThat(message.getContent()).isEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isEqualTo(updatedMessageDTO.getCreatedTimeStamp());
    }

    @Test
    void updateMessage_idNull() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(null)
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Message message = messageRepository.getById(MESSAGE_DTO_1.getId());
        assertThat(message.getContent()).isNotEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isNotEqualTo(updatedMessageDTO.getCreatedTimeStamp());
    }

    @Test
    void updateMessage_idMismatchMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(MESSAGE_DTO_2.getId())
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Message message = messageRepository.getById(MESSAGE_DTO_1.getId());
        assertThat(message.getContent()).isNotEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isNotEqualTo(updatedMessageDTO.getCreatedTimeStamp());
    }

    @Test
    void updateMessage_nonExistingMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(messageDTO3.getId())
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL_ID, messageDTO3.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Message> message = messageRepository.findById(messageDTO3.getId());
        assertThat(message).isNotPresent();
    }

    @Test
    void partialUpdateMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(MESSAGE_DTO_1.getId())
                .content("new message 1")
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(patch(ENTITY_API_URL_ID, updatedMessageDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedMessageDTO.getId().intValue()))
                .andExpect(jsonPath("$.content").value(updatedMessageDTO.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(MESSAGE_DTO_1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(updatedMessageDTO.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(updatedMessageDTO.getTopic().getId()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Message message = messageRepository.findById(MESSAGE_DTO_1.getId()).get();
        assertThat(message.getContent()).isEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isEqualTo(MESSAGE_DTO_1.getCreatedTimeStamp());
    }

    @Test
    void partialUpdateMessage_idNull() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(null)
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(patch(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Message message = messageRepository.getById(MESSAGE_DTO_1.getId());
        assertThat(message.getContent()).isNotEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isNotEqualTo(updatedMessageDTO.getCreatedTimeStamp());
    }

    @Test
    void partialUpdateMessage_idMismatchMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(MESSAGE_DTO_2.getId())
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(patch(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Message message = messageRepository.getById(MESSAGE_DTO_1.getId());
        assertThat(message.getContent()).isNotEqualTo(updatedMessageDTO.getContent());
        assertThat(message.getCreatedTimeStamp()).isNotEqualTo(updatedMessageDTO.getCreatedTimeStamp());
    }

    @Test
    void partialUpdateMessage_nonExistingMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(messageDTO3.getId())
                .content("new message 1")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(MESSAGE_DTO_1.getDevice().getId()).build())
                .topic(TopicDTO.builder().id(MESSAGE_DTO_1.getTopic().getId()).build())
                .build();

        // when
        mockMvc.perform(patch(ENTITY_API_URL_ID, messageDTO3.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Message> message = messageRepository.findById(messageDTO3.getId());
        assertThat(message).isNotPresent();
    }

    @Test
    void getAllMessages() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL + "?sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(MESSAGE_DTO_1.getId().intValue()))
                .andExpect(jsonPath("$.[0].content").value(MESSAGE_DTO_1.getContent()))
                .andExpect(jsonPath("$.[0].createdTimeStamp").value(MESSAGE_DTO_1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.[0].device.id").value(MESSAGE_DTO_1.getDevice().getId()))
                .andExpect(jsonPath("$.[0].topic.id").value(MESSAGE_DTO_1.getTopic().getId()))

                .andExpect(jsonPath("$.[1].id").value(MESSAGE_DTO_2.getId().intValue()))
                .andExpect(jsonPath("$.[1].content").value(MESSAGE_DTO_2.getContent()))
                .andExpect(jsonPath("$.[1].createdTimeStamp").value(MESSAGE_DTO_2.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.[1].device.id").value(MESSAGE_DTO_2.getDevice().getId()))
                .andExpect(jsonPath("$.[1].topic.id").value(MESSAGE_DTO_2.getTopic().getId()))
        ;

        // then
    }

    @Test
    void getMessage() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(MESSAGE_DTO_1.getId().intValue()))
                .andExpect(jsonPath("$.content").value(MESSAGE_DTO_1.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(MESSAGE_DTO_1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(MESSAGE_DTO_1.getDevice().getId()))
                .andExpect(jsonPath("$.device.macAddress").value(MESSAGE_DTO_1.getDevice().getMacAddress()))
                .andExpect(jsonPath("$.topic.id").value(MESSAGE_DTO_1.getTopic().getId()))
                .andExpect(jsonPath("$.topic.name").value(MESSAGE_DTO_1.getTopic().getName()))
        ;

        // then
    }

    @Test
    void getMessage_nonExistingMessage() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, messageDTO3.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.path").value(ENTITY_API_URL + "/" + messageDTO3.getId()))
        ;

        // then
    }

    @Test
    void deleteMessage() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, MESSAGE_DTO_1.getId()))
                .andExpect(status().isNoContent())
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.deleted".formatted(applicationName, ENTITY_NAME)));

        // then
        assertThat(messageRepository.findById(MESSAGE_DTO_1.getId())).isEmpty();
        assertThat(messageRepository.findAll()).hasSize(1);
    }
}
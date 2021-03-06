package com.example.iotcore.web.controller;

import com.example.iotcore.config.SecurityConfiguration;
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
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MessageController.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = MessageController.class)
class MessageControllerTest {
    private static final String ENTITY_API_URL = "/api/messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MessageService messageService;

    @MockBean
    MessageRepository messageRepository;

    MessageDTO messageDTO1;

    MessageDTO messageDTO2;

    List<MessageDTO> messageDTOs;

    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        messageDTO1 = MessageDTO.builder()
                .id(1L)
                .content("First Message")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(1L).build())
                .topic(TopicDTO.builder().id(1L).build())
                .build();


        messageDTO2 = MessageDTO.builder()
                .id(2L)
                .content("Second Message")
                .createdTimeStamp(Instant.now())
                .device(DeviceDTO.builder().id(2L).build())
                .topic(TopicDTO.builder().id(2L).build())
                .build();

        messageDTOs = List.of(messageDTO1, messageDTO2);
    }

    @Test
    void createMessage() throws Exception {
        // given
        MessageDTO messageDTO = MessageDTO.builder()
                .content(messageDTO1.getContent())
                .device(messageDTO1.getDevice())
                .topic(messageDTO1.getTopic())
                .build();
        given(messageService.save(any(MessageDTO.class))).willReturn(messageDTO1);

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(messageDTO1.getId().intValue()))
                .andExpect(jsonPath("$.content").value(messageDTO1.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(messageDTO1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(messageDTO1.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(messageDTO1.getTopic().getId()))
        ;

        // then
        verify(messageService, times(1)).save(any(MessageDTO.class));
    }

    @Test
    void updateMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(messageDTO1.getId())
                .content(messageDTO1.getContent())
                .createdTimeStamp(Instant.now())
                .device(messageDTO1.getDevice())
                .topic(messageDTO1.getTopic())
                .build();

        given(messageService.save(any(MessageDTO.class))).willReturn(updatedMessageDTO);
        given(messageRepository.existsById(updatedMessageDTO.getId())).willReturn(true);

        // when
        mockMvc.perform(put(ENTITY_API_URL_ID, updatedMessageDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMessageDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(updatedMessageDTO)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedMessageDTO.getId().intValue()))
                .andExpect(jsonPath("$.content").value(updatedMessageDTO.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(updatedMessageDTO.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(updatedMessageDTO.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(updatedMessageDTO.getTopic().getId()))
        ;

        // then
        verify(messageService, times(1)).save(any(MessageDTO.class));
    }

    @Test
    void partialUpdateMessage() throws Exception {
        // given
        MessageDTO updatedMessageDTO = MessageDTO.builder()
                .id(messageDTO1.getId())
                .content("Updated Message")
                .device(messageDTO1.getDevice())
                .createdTimeStamp(messageDTO1.getCreatedTimeStamp())
                .topic(messageDTO1.getTopic())
                .build();
        given(messageService.partialUpdate(any(MessageDTO.class))).willReturn(Optional.of(updatedMessageDTO));
        given(messageRepository.existsById(updatedMessageDTO.getId())).willReturn(true);

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
                .andExpect(jsonPath("$.createdTimeStamp").value(updatedMessageDTO.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(updatedMessageDTO.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(updatedMessageDTO.getTopic().getId()))
        ;

        // then
        verify(messageService, times(1)).partialUpdate(any(MessageDTO.class));
    }

    @Test
    void getAllMessages() throws Exception {
        // given
        Page<MessageDTO> messageDTOPage = new PageImpl<>(messageDTOs);
        given(messageService.findAll(any(Pageable.class))).willReturn(messageDTOPage);

        // when
        mockMvc.perform(get(ENTITY_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(messageDTO1.getId().intValue()))
                .andExpect(jsonPath("$.[0].content").value(messageDTO1.getContent()))
                .andExpect(jsonPath("$.[0].createdTimeStamp").value(messageDTO1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.[0].device.id").value(messageDTO1.getDevice().getId()))
                .andExpect(jsonPath("$.[0].topic.id").value(messageDTO1.getTopic().getId()))

                .andExpect(jsonPath("$.[1].id").value(messageDTO2.getId().intValue()))
                .andExpect(jsonPath("$.[1].content").value(messageDTO2.getContent()))
                .andExpect(jsonPath("$.[1].createdTimeStamp").value(messageDTO2.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.[1].device.id").value(messageDTO2.getDevice().getId()))
                .andExpect(jsonPath("$.[1].topic.id").value(messageDTO2.getTopic().getId()))
        ;

        // then
        verify(messageService, times(1)).findAll(any());
    }

    @Test
    void getMessage() throws Exception {
        // given
        given(messageService.findOne(anyLong())).willReturn(Optional.of(messageDTO1));
        String foundResult = objectMapper.writeValueAsString(messageDTO1);

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isOk())
                .andExpect(content().string(foundResult))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(messageDTO1.getId().intValue()))
                .andExpect(jsonPath("$.content").value(messageDTO1.getContent()))
                .andExpect(jsonPath("$.createdTimeStamp").value(messageDTO1.getCreatedTimeStamp().toString()))
                .andExpect(jsonPath("$.device.id").value(messageDTO1.getDevice().getId()))
                .andExpect(jsonPath("$.topic.id").value(messageDTO1.getTopic().getId()))
        ;

        // then
        verify(messageService, times(1)).findOne(anyLong());
    }

    @Test
    void deleteMessage() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isNoContent());

        // then
        verify(messageService, times(1)).delete(anyLong());
    }
}
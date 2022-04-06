package com.example.iotcore.web.controller.sync;

import com.example.iotcore.config.SecurityConfiguration;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.TopicService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TopicControllerSync.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = TopicControllerSync.class)
class TopicControllerSyncTest {
    private static final String ENTITY_API_URL = "/api/topics";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TopicService topicService;

    @MockBean
    TopicRepository topicRepository;

    TopicDTO topicDTO1;

    TopicDTO topicDTO2;

    List<TopicDTO> topicDTOs;


    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        topicDTO1 = TopicDTO.builder()
                .id(1L)
                .name("topic1")
                .build();


        topicDTO2 = TopicDTO.builder()
                .id(2L)
                .name("topic2")
                .build();

        topicDTOs = List.of(topicDTO1, topicDTO2);
    }

    @Test
    void createTopic() throws Exception {
        // given
        TopicDTO topicDTO = TopicDTO.builder()
                .name(topicDTO1.getName())
                .build();
        given(topicService.save(any(TopicDTO.class))).willReturn(topicDTO1);

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(topicDTO1.getId().intValue()))
                .andExpect(jsonPath("$.name").value(topicDTO1.getName()));

        // then
        verify(topicService, times(1)).save(any(TopicDTO.class));
    }

    @Test
    void updateTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(topicDTO1.getId())
                .name("New Topic")
                .build();
        given(topicService.save(any(TopicDTO.class))).willReturn(updatedTopicDTO);
        given(topicRepository.existsById(updatedTopicDTO.getId())).willReturn(true);

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, topicDTO1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedTopicDTO.getId().intValue()))
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()));

        // then
        verify(topicService, times(1)).save(any(TopicDTO.class));
    }

    @Test
    void partialUpdateTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(topicDTO1.getId())
                .name("New Topic")
                .build();
        given(topicService.partialUpdate(any(TopicDTO.class))).willReturn(Optional.of(updatedTopicDTO));
        given(topicRepository.existsById(updatedTopicDTO.getId())).willReturn(true);

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, updatedTopicDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedTopicDTO.getId().intValue()))
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()));

        // then
        verify(topicService, times(1)).partialUpdate(any(TopicDTO.class));
    }

    @Test
    void getAllTopics() throws Exception {
        // given
        Page<TopicDTO> topicDTOPage = new PageImpl<>(topicDTOs);
        given(topicService.findAll(any(Pageable.class))).willReturn(topicDTOPage);

        // when
        mockMvc.perform(get(ENTITY_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(topicDTO1.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(topicDTO1.getName())))
                .andExpect(jsonPath("$.[*].id").value(hasItem(topicDTO2.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(topicDTO2.getName())))
        ;

        // then
        verify(topicService, times(1)).findAll(any());
    }

    @Test
    void getTopic() throws Exception {
        // given
        given(topicService.findOne(anyLong())).willReturn(Optional.of(topicDTO1));
        String foundResult = objectMapper.writeValueAsString(topicDTO1);

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isOk())
                .andExpect(content().string(foundResult))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(topicDTO1.getId().intValue()))
                .andExpect(jsonPath("$.name").value(topicDTO1.getName()));

        // then
        verify(topicService, times(1)).findOne(anyLong());
    }

    @Test
    void deleteTopic() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isNoContent());

        // then
        verify(topicService, times(1)).delete(anyLong());
    }
}
package com.example.iotcore.web.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.TopicService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopicControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/topics";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final TopicDTO TOPIC_1 = new TopicDTO();
    private static final TopicDTO TOPIC_2 = new TopicDTO();
    private static final TopicDTO TOPIC_3 = new TopicDTO();

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TopicService topicService;

    @Autowired
    TopicRepository topicRepository;

    TopicDTO topicDTO4;

    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        TOPIC_1.setId(1L);
        TOPIC_1.setName("topic1");

        TOPIC_2.setId(2L);
        TOPIC_2.setName("topic2");

        TOPIC_3.setId(3L);
        TOPIC_3.setName("topic3");

        topicDTO4 = TopicDTO.builder()
                .id(4L)
                .name("topic4")
                .build();
    }

    @Test
    @Transactional
    void createTopic() throws Exception {
        // given
        TopicDTO topicDTO = TopicDTO.builder()
                .name(topicDTO4.getName())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(topicDTO4.getId().intValue()))
                .andExpect(jsonPath("$.name").value(topicDTO4.getName()));

        // then
        assertThat(topicRepository.existsById(topicDTO4.getId())).isTrue();
        assertThat(topicRepository.findAll()).hasSize(4);
    }

    @Test
    @Transactional
    void updateTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_1.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedTopicDTO.getId().intValue()))
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()));

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isEqualTo(updatedTopicDTO.getName());
    }

    @Test
    @Transactional
    void partialUpdateTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_1.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedTopicDTO.getId().intValue()))
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()));

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void getAllTopics() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(TOPIC_1.getId().intValue()))
                .andExpect(jsonPath("$.[0].name").value(TOPIC_1.getName()))
                .andExpect(jsonPath("$.[1].id").value(TOPIC_2.getId().intValue()))
                .andExpect(jsonPath("$.[1].name").value(TOPIC_2.getName()))
                .andExpect(jsonPath("$.[2].id").value(TOPIC_3.getId().intValue()))
                .andExpect(jsonPath("$.[2].name").value(TOPIC_3.getName()))
        ;

        // then
    }

    @Test
    void getTopic() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, TOPIC_1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(TOPIC_1.getId().intValue()))
                .andExpect(jsonPath("$.name").value(TOPIC_1.getName()));

        // then
    }

    @Test
    @Transactional
    void deleteTopic() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, TOPIC_3.getId()))
                .andExpect(status().isNoContent());

        // then
        assertThat(topicRepository.findById(TOPIC_3.getId())).isEmpty();
        assertThat(topicRepository.findAll()).hasSize(2);
    }
}
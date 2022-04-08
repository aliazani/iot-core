package com.example.iotcore.web.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.domain.Topic;
import com.example.iotcore.dto.TopicDTO;
import com.example.iotcore.repository.TopicRepository;
import com.example.iotcore.service.TopicService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TopicControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/topics";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final TopicDTO TOPIC_1 = new TopicDTO();
    private static final TopicDTO TOPIC_2 = new TopicDTO();
    private static final TopicDTO TOPIC_3 = new TopicDTO();
    private static final String ENTITY_NAME = "topic";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TopicService topicService;

    @Autowired
    TopicRepository topicRepository;

    TopicDTO topicDTO4;
    @Value("${application.clientApp.name}")
    private String applicationName;

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
                .andExpect(jsonPath("$.name").value(topicDTO4.getName()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.created".formatted(applicationName, ENTITY_NAME)))
        ;

        // then
        assertThat(topicRepository.existsById(topicDTO4.getId())).isTrue();
        assertThat(topicRepository.findAll()).hasSize(4);
    }

    @Test
    void createTopic_existingId() throws Exception {
        // given
        TopicDTO topicDTO = TopicDTO.builder()
                .id(topicDTO4.getId())
                .name(topicDTO4.getName())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idexists"))
        ;

        // then
        assertThat(topicRepository.existsById(topicDTO4.getId())).isFalse();
        assertThat(topicRepository.findAll()).hasSize(3);
    }

    @Test
    void createTopic_existingName() throws Exception {
        // given
        TopicDTO topicDTO = TopicDTO.builder()
                .name(TOPIC_1.getName())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("nameexists"))
        ;

        // then
        assertThat(topicRepository.existsById(topicDTO4.getId())).isFalse();
        assertThat(topicRepository.findAll()).hasSize(3);
    }

    @Test
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
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void updateTopic_idNull() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(null)
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Topic topic = topicRepository.getById(TOPIC_1.getId());
        assertThat(topic.getName()).isNotEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void updateTopic_idMismatchTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_2.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Topic topic = topicRepository.getById(TOPIC_1.getId());
        assertThat(topic.getName()).isNotEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void updateTopic_nonExistingTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(topicDTO4.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, topicDTO4.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Topic> topic = topicRepository.findById(topicDTO4.getId());
        assertThat(topic).isNotPresent();
    }


    @Test
    void updateTopic_alreadyExistsName() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_1.getId())
                .name(TOPIC_2.getName())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("nameexists"))
        ;

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isNotEqualTo(TOPIC_2.getName());
    }

    @Test
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
                .andExpect(jsonPath("$.name").value(updatedTopicDTO.getName()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void partialUpdateTopic_idNull() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(null)
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Topic topic = topicRepository.getById(TOPIC_1.getId());
        assertThat(topic.getName()).isNotEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void partialUpdateTopic_idMismatchTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_2.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Topic topic = topicRepository.getById(TOPIC_1.getId());
        assertThat(topic.getName()).isNotEqualTo(updatedTopicDTO.getName());
    }

    @Test
    void partialUpdateTopic_nonExistingTopic() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(topicDTO4.getId())
                .name("updatedTopic1")
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, topicDTO4.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Topic> topic = topicRepository.findById(topicDTO4.getId());
        assertThat(topic).isNotPresent();
    }


    @Test
    void partialUpdateTopic_alreadyExistsName() throws Exception {
        // given
        TopicDTO updatedTopicDTO = TopicDTO.builder()
                .id(TOPIC_1.getId())
                .name(TOPIC_2.getName())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, TOPIC_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTopicDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("nameexists"))
        ;

        // then
        Topic topic = topicRepository.findById(TOPIC_1.getId()).get();
        assertThat(topic.getName()).isNotEqualTo(TOPIC_2.getName());
    }

    @Test
    void getAllTopics() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL + "?sort=id,asc"))
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
    void getTopic_noneExistingTopic() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, topicDTO4.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.path").value(ENTITY_API_URL + "/" + topicDTO4.getId()))
        ;

        // then
    }

    @Test
    void deleteTopic() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, TOPIC_3.getId()))
                .andExpect(status().isNoContent())
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.deleted".formatted(applicationName, ENTITY_NAME)));

        // then
        assertThat(topicRepository.findById(TOPIC_3.getId())).isEmpty();
        assertThat(topicRepository.findAll()).hasSize(2);
    }
}
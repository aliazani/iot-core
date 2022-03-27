package com.example.iotcore.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicDTOTest {
    TopicDTO topicDTO1;
    TopicDTO topicDTO2;

    @BeforeEach
    void setup() {
        topicDTO1 = new TopicDTO();
        topicDTO2 = new TopicDTO();
    }

    @Test
    void equalDevices() {
        topicDTO1.setId(1L);
        topicDTO2.setId(topicDTO1.getId());

        assertThat(topicDTO1).isEqualTo(topicDTO2);
    }

    @Test
    void differentDevices() {
        topicDTO1.setId(1L);
        topicDTO2.setId(2L);
        assertThat(topicDTO1).isNotEqualTo(topicDTO2);
        topicDTO1.setId(null);
        assertThat(topicDTO1).isNotEqualTo(topicDTO2);
    }

}
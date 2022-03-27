package com.example.iotcore.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicTest {
    Topic topic1;
    Topic topic2;

    @BeforeEach
    void setup() {
        topic1 = new Topic();
        topic2 = new Topic();
    }

    @Test
    void equalDevices() {
        topic1.setId(1L);
        topic2.setId(topic1.getId());

        assertThat(topic1).isEqualTo(topic2);
    }

    @Test
    void differentDevices() {
        topic1.setId(1L);
        topic2.setId(2L);
        assertThat(topic1).isNotEqualTo(topic2);
        topic1.setId(null);
        assertThat(topic1).isNotEqualTo(topic2);
    }

}
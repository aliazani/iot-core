package com.example.iotcore.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {
    Message message1;
    Message message2;

    @BeforeEach
    void setup() {
        message1 = new Message();
        message2 = new Message();
    }

    @Test
    void equalDevices() {
        message1.setId(1L);
        message2.setId(message1.getId());

        assertThat(message1).isEqualTo(message2);
    }

    @Test
    void differentDevices() {
        message1.setId(1L);
        message2.setId(2L);
        assertThat(message1).isNotEqualTo(message2);
        message1.setId(null);
        assertThat(message1).isNotEqualTo(message2);
    }
}
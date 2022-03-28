package com.example.iotcorebusiness.dto;

import com.example.iotcore.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDTOTest {
    MessageDTO messageDTO1;
    MessageDTO messageDTO2;

    @BeforeEach
    void setup() {
        messageDTO1 = new MessageDTO();
        messageDTO2 = new MessageDTO();
    }

    @Test
    void equalMessageDTOs() {
        messageDTO1.setId(1L);
        messageDTO2.setId(messageDTO1.getId());

        assertThat(messageDTO1).isEqualTo(messageDTO2);
    }

    @Test
    void unequalMessageDTOs() {
        messageDTO1.setId(1L);
        messageDTO2.setId(2L);
        assertThat(messageDTO1).isNotEqualTo(messageDTO2);
        messageDTO1.setId(null);
        assertThat(messageDTO1).isNotEqualTo(messageDTO2);
    }
}
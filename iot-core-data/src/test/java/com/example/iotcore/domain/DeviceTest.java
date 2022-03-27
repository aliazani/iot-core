package com.example.iotcore.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTest {
    Device device1;
    Device device2;

    @BeforeEach
    void setup() {
        device1 = new Device();
        device2 = new Device();
    }

    @Test
    void equalDevices() {
        device1.setId(1L);
        device2.setId(device1.getId());

        assertThat(device1).isEqualTo(device2);
    }

    @Test
    void differentDevices() {
        device1.setId(1L);
        device2.setId(2L);
        assertThat(device1).isNotEqualTo(device2);
        device1.setId(null);
        assertThat(device1).isNotEqualTo(device2);
    }
}
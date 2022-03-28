package com.example.iotcorebusiness.dto;

import com.example.iotcore.dto.DeviceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDTOTest {
    DeviceDTO deviceDTO1;
    DeviceDTO deviceDTO2;

    @BeforeEach
    void setup() {
        deviceDTO1 = new DeviceDTO();
        deviceDTO2 = new DeviceDTO();
    }

    @Test
    void equalDeviceDTOs() {
        deviceDTO1.setId(1L);
        deviceDTO2.setId(deviceDTO1.getId());

        assertThat(deviceDTO1).isEqualTo(deviceDTO2);
    }

    @Test
    void unequalDeviceDTOs() {
        deviceDTO1.setId(1L);
        deviceDTO2.setId(2L);
        assertThat(deviceDTO1).isNotEqualTo(deviceDTO2);
        deviceDTO1.setId(null);
        assertThat(deviceDTO1).isNotEqualTo(deviceDTO2);
    }
}
package com.example.iotcore.web.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.domain.Device;
import com.example.iotcore.dto.DeviceDTO;
import com.example.iotcore.repository.DeviceRepository;
import com.example.iotcore.service.DeviceService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMVC tests for the {@link DeviceController} REST controller.
 */
@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeviceControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/devices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final DeviceDTO DEVICE_DTO_1 = new DeviceDTO();
    private static final DeviceDTO DEVICE_DTO_2 = new DeviceDTO();
    private static final DeviceDTO DEVICE_DTO_3 = new DeviceDTO();

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DeviceService deviceService;

    @Autowired
    DeviceRepository deviceRepository;

    DeviceDTO deviceDTO4;

    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        DEVICE_DTO_1.setId(1L);
        DEVICE_DTO_1.setMacAddress("00:00:00:00:00:00");

        DEVICE_DTO_2.setId(2L);
        DEVICE_DTO_2.setMacAddress("11:11:11:11:11:11");

        DEVICE_DTO_3.setId(3L);
        DEVICE_DTO_3.setMacAddress("22:22:22:22:22:22");

        deviceDTO4 = DeviceDTO.builder()
                .id(4L)
                .macAddress(UUID.randomUUID().toString())
                .build();
    }

    @Test
    @Transactional
    void createDevice() throws Exception {
        // given
        DeviceDTO deviceDTO = DeviceDTO.builder()
                .macAddress(deviceDTO4.getMacAddress())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(deviceDTO4.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(deviceDTO4.getMacAddress()));

        // then
        assertThat(deviceRepository.existsById(deviceDTO4.getId())).isTrue();
        assertThat(deviceRepository.findAll()).hasSize(4);
    }

    @Test
    @Transactional
    void updateDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_1.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedDeviceDTO.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(updatedDeviceDTO.getMacAddress()));

        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    @Transactional
    void partialUpdateDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_1.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, updatedDeviceDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedDeviceDTO.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(updatedDeviceDTO.getMacAddress()));

        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    void getAllDevices() throws Exception {
        // given
        // when
        mockMvc.perform(get(ENTITY_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(DEVICE_DTO_1.getId().intValue()))
                .andExpect(jsonPath("$.[0].macAddress").value(DEVICE_DTO_1.getMacAddress()))
                .andExpect(jsonPath("$.[1].id").value(DEVICE_DTO_2.getId().intValue()))
                .andExpect(jsonPath("$.[1].macAddress").value(DEVICE_DTO_2.getMacAddress()))
                .andExpect(jsonPath("$.[2].id").value(DEVICE_DTO_3.getId().intValue()))
                .andExpect(jsonPath("$.[2].macAddress").value(DEVICE_DTO_3.getMacAddress()))
        ;

        // then
    }

    @Test
    void getDevice() throws Exception {
        // given
        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, DEVICE_DTO_1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(DEVICE_DTO_1.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(DEVICE_DTO_1.getMacAddress()));

        // then
    }

    @Test
    @Transactional
    void deleteDevice() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, DEVICE_DTO_3.getId()))
                .andExpect(status().isNoContent());

        // then
        assertThat(deviceRepository.findById(DEVICE_DTO_3.getId())).isEmpty();
        assertThat(deviceRepository.findAll()).hasSize(2);
    }
}
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
@Transactional
class DeviceControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/devices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final DeviceDTO DEVICE_DTO_1 = new DeviceDTO();
    private static final DeviceDTO DEVICE_DTO_2 = new DeviceDTO();
    private static final DeviceDTO DEVICE_DTO_3 = new DeviceDTO();
    private static final String ENTITY_NAME = "device";
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    DeviceService deviceService;
    @Autowired
    DeviceRepository deviceRepository;
    DeviceDTO deviceDTO4;
    @Value("${application.clientApp.name}")
    private String applicationName;

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
                .andExpect(jsonPath("$.macAddress").value(deviceDTO4.getMacAddress()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.created".formatted(applicationName, ENTITY_NAME)));

        // then
        assertThat(deviceRepository.existsById(deviceDTO4.getId())).isTrue();
        assertThat(deviceRepository.findAll()).hasSize(4);
    }

    @Test
    void createDevice_exitingId() throws Exception {
        // given
        DeviceDTO deviceDTO = DeviceDTO.builder()
                .id(deviceDTO4.getId())
                .macAddress(deviceDTO4.getMacAddress())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idexists"))
        ;

        // then
        assertThat(deviceRepository.existsById(deviceDTO4.getId())).isFalse();
        assertThat(deviceRepository.findAll()).hasSize(3);
    }

    @Test
    void createDevice_exitingMacAddress() throws Exception {
        // given
        DeviceDTO deviceDTO = DeviceDTO.builder()
                .macAddress(DEVICE_DTO_1.getMacAddress())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("macaddressexists"))
        ;

        // then
        assertThat(deviceRepository.existsById(deviceDTO4.getId())).isFalse();
        assertThat(deviceRepository.findAll()).hasSize(3);
    }

    @Test
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
                .andExpect(jsonPath("$.macAddress").value(updatedDeviceDTO.getMacAddress()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isEqualTo(updatedDeviceDTO.getMacAddress());
    }


    @Test
    void updateDevice_idNull() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(null)
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Device device = deviceRepository.getById(DEVICE_DTO_1.getId());
        assertThat(device.getMacAddress()).isNotEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    void updateDevice_idMismatchDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_2.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Device device = deviceRepository.getById(DEVICE_DTO_1.getId());
        assertThat(device.getMacAddress()).isNotEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    void updateDevice_nonExistingDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(deviceDTO4.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, deviceDTO4.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Device> device = deviceRepository.findById(deviceDTO4.getId());
        assertThat(device).isNotPresent();
    }

    @Test
    void updateDevice_alreadyExistsMacAddress() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_1.getId())
                .macAddress(DEVICE_DTO_2.getMacAddress())
                .build();

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("macaddressexists"))
        ;

        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isNotEqualTo(DEVICE_DTO_2.getMacAddress());
    }

    @Test
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
                .andExpect(jsonPath("$.macAddress").value(updatedDeviceDTO.getMacAddress()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.updated".formatted(applicationName, ENTITY_NAME)));

        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isEqualTo(updatedDeviceDTO.getMacAddress());
    }


    @Test
    void partialUpdateDevice_idNull() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(null)
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnull"))
        ;

        // then
        Device device = deviceRepository.getById(DEVICE_DTO_1.getId());
        assertThat(device.getMacAddress()).isNotEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    void partialUpdateDevice_idMismatchDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_2.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idinvalid"))
        ;

        // then
        Device device = deviceRepository.getById(DEVICE_DTO_1.getId());
        assertThat(device.getMacAddress()).isNotEqualTo(updatedDeviceDTO.getMacAddress());
    }

    @Test
    void partialUpdateDevice_nonExistingDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(deviceDTO4.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, updatedDeviceDTO.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("idnotfound"))
        ;

        // then
        Optional<Device> device = deviceRepository.findById(deviceDTO4.getId());
        assertThat(device).isNotPresent();
    }

    @Test
    void partialUpdateDevice_alreadyExistsMacAddress() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(DEVICE_DTO_1.getId())
                .macAddress(DEVICE_DTO_2.getMacAddress())
                .build();

        // when
        mockMvc.perform(
                        patch(ENTITY_API_URL_ID, DEVICE_DTO_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value(ENTITY_NAME))
                .andExpect(jsonPath("$.errorKey").value("macaddressexists"))
        ;

        // then
        // then
        Device device = deviceRepository.findById(DEVICE_DTO_1.getId()).get();
        assertThat(device.getMacAddress()).isNotEqualTo(DEVICE_DTO_2.getMacAddress());
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
    void getDevice_noneExistingDevice() throws Exception {
        // given
        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, deviceDTO4.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.path").value(ENTITY_API_URL + deviceDTO4.getId()))
        ;

        // then
    }

    @Test
    void deleteDevice() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, DEVICE_DTO_3.getId()))
                .andExpect(status().isNoContent())
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.%s.deleted".formatted(applicationName, ENTITY_NAME)));

        // then
        assertThat(deviceRepository.findById(DEVICE_DTO_3.getId())).isEmpty();
        assertThat(deviceRepository.findAll()).hasSize(2);
    }
}
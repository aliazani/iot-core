package com.example.iotcore.web.controller.sync;

import com.example.iotcore.config.SecurityConfiguration;
import com.example.iotcore.dto.DeviceDTO;
import com.example.iotcore.repository.DeviceRepository;
import com.example.iotcore.service.sync.DeviceServiceSync;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMVC tests for the {@link DeviceControllerSync} REST controller.
 */
@WebMvcTest(controllers = DeviceControllerSync.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = DeviceControllerSync.class)
class DeviceControllerSyncTest {
    private static final String ENTITY_API_URL = "/api/devices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeviceServiceSync deviceServiceSync;

    @MockBean
    DeviceRepository deviceRepository;

    DeviceDTO deviceDTO1;

    DeviceDTO deviceDTO2;

    List<DeviceDTO> deviceDTOs;

    @BeforeEach
    void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        deviceDTO1 = DeviceDTO.builder()
                .id(1L)
                .macAddress(UUID.randomUUID().toString())
                .build();


        deviceDTO2 = DeviceDTO.builder()
                .id(2L)
                .macAddress(UUID.randomUUID().toString())
                .build();

        deviceDTOs = List.of(deviceDTO1, deviceDTO2);
    }

    @Test
    void createDevice() throws Exception {
        // given
        DeviceDTO deviceDTO = DeviceDTO.builder()
                .macAddress(deviceDTO1.getMacAddress())
                .build();
        given(deviceServiceSync.save(any(DeviceDTO.class))).willReturn(deviceDTO1);

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(deviceDTO1.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(deviceDTO1.getMacAddress()));

        // then
        verify(deviceServiceSync, times(1)).save(any(DeviceDTO.class));
    }

    @Test
    void updateDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(deviceDTO1.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();

        given(deviceServiceSync.save(any(DeviceDTO.class))).willReturn(updatedDeviceDTO);
        given(deviceRepository.existsById(updatedDeviceDTO.getId())).willReturn(true);

        // when
        mockMvc.perform(
                        put(ENTITY_API_URL_ID, deviceDTO1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDeviceDTO))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedDeviceDTO.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(updatedDeviceDTO.getMacAddress()));

        // then
        verify(deviceServiceSync, times(1)).save(any(DeviceDTO.class));
    }

    @Test
    void partialUpdateDevice() throws Exception {
        // given
        DeviceDTO updatedDeviceDTO = DeviceDTO.builder()
                .id(deviceDTO1.getId())
                .macAddress(UUID.randomUUID().toString())
                .build();
        given(deviceServiceSync.partialUpdate(any(DeviceDTO.class))).willReturn(Optional.of(updatedDeviceDTO));
        given(deviceRepository.existsById(updatedDeviceDTO.getId())).willReturn(true);

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
        verify(deviceServiceSync, times(1)).partialUpdate(any(DeviceDTO.class));
    }

    @Test
    void getAllDevices() throws Exception {
        // given
        Page<DeviceDTO> deviceDTOPage = new PageImpl<>(deviceDTOs);
        given(deviceServiceSync.findAll(any(Pageable.class))).willReturn(deviceDTOPage);

        // when
        mockMvc.perform(get(ENTITY_API_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(deviceDTO1.getId().intValue())))
                .andExpect(jsonPath("$.[*].macAddress").value(hasItem(deviceDTO1.getMacAddress())))
                .andExpect(jsonPath("$.[*].id").value(hasItem(deviceDTO2.getId().intValue())))
                .andExpect(jsonPath("$.[*].macAddress").value(hasItem(deviceDTO2.getMacAddress())))
        ;

        // then
        verify(deviceServiceSync, times(1)).findAll(any());
    }

    @Test
    void getDevice() throws Exception {
        // given
        given(deviceServiceSync.findOne(anyLong())).willReturn(Optional.of(deviceDTO1));
        String foundResult = objectMapper.writeValueAsString(deviceDTO1);

        // when
        mockMvc.perform(get(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isOk())
                .andExpect(content().string(foundResult))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(deviceDTO1.getId().intValue()))
                .andExpect(jsonPath("$.macAddress").value(deviceDTO1.getMacAddress()));

        // then
        verify(deviceServiceSync, times(1)).findOne(anyLong());
    }

    @Test
    void deleteDevice() throws Exception {
        // given

        // when
        mockMvc.perform(delete(ENTITY_API_URL_ID, anyLong()))
                .andExpect(status().isNoContent());

        // then
        verify(deviceServiceSync, times(1)).delete(anyLong());
    }
}
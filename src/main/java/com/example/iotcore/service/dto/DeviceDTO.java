package com.example.iotcore.service.dto;

import com.example.iotcore.domain.Device;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link Device} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceDTO implements Serializable {

    private Long id;

    private String macAddress;
}

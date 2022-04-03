package com.example.iotcore.dto;

import com.example.iotcore.domain.Device;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link Device} entity.
 */
@Schema(name = "DeviceDTO", description = "A DTO for the Device entity.")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceDTO implements Serializable {

    private Long id;

    private String macAddress;
}

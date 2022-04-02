package com.example.iotcore.mapper;


import com.example.iotcore.domain.Device;
import com.example.iotcore.dto.DeviceDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Device} and its DTO {@link DeviceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper extends EntityMapper<DeviceDTO, Device> {
}

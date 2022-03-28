package com.example.iotcore.mapper;


import com.example.iotcore.domain.Device;
import com.example.iotcore.dto.DeviceDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Device} and its DTO {@link DeviceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper extends EntityMapper<DeviceDTO, Device> {
    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DeviceDTO toDtoId(Device device);
}

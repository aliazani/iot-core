package com.example.iotcore.mapper;

import com.example.iotcore.domain.Device;
import com.example.iotcore.domain.Device.DeviceBuilder;
import com.example.iotcore.dto.DeviceDTO;
import com.example.iotcore.dto.DeviceDTO.DeviceDTOBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-03-27T03:15:34+0430",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.2 (Oracle Corporation)"
)
@Component
public class DeviceMapperImpl implements DeviceMapper {

    @Override
    public Device toEntity(DeviceDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DeviceBuilder device = Device.builder();

        device.id( dto.getId() );
        device.macAddress( dto.getMacAddress() );

        return device.build();
    }

    @Override
    public DeviceDTO toDto(Device entity) {
        if ( entity == null ) {
            return null;
        }

        DeviceDTOBuilder deviceDTO = DeviceDTO.builder();

        deviceDTO.id( entity.getId() );
        deviceDTO.macAddress( entity.getMacAddress() );

        return deviceDTO.build();
    }

    @Override
    public List<Device> toEntity(List<DeviceDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Device> list = new ArrayList<Device>( dtoList.size() );
        for ( DeviceDTO deviceDTO : dtoList ) {
            list.add( toEntity( deviceDTO ) );
        }

        return list;
    }

    @Override
    public List<DeviceDTO> toDto(List<Device> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<DeviceDTO> list = new ArrayList<DeviceDTO>( entityList.size() );
        for ( Device device : entityList ) {
            list.add( toDto( device ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(Device entity, DeviceDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getMacAddress() != null ) {
            entity.setMacAddress( dto.getMacAddress() );
        }
    }

    @Override
    public DeviceDTO toDtoId(Device device) {
        if ( device == null ) {
            return null;
        }

        DeviceDTOBuilder deviceDTO = DeviceDTO.builder();

        deviceDTO.id( device.getId() );

        return deviceDTO.build();
    }
}

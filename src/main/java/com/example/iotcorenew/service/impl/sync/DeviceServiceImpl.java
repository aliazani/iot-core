package com.example.iotcorenew.service.impl.sync;

import com.example.iotcorenew.domain.Device;
import com.example.iotcorenew.repository.DeviceRepository;
import com.example.iotcorenew.service.dto.DeviceDTO;
import com.example.iotcorenew.service.mapper.DeviceMapper;
import com.example.iotcorenew.service.sync.DeviceServiceSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Device}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DeviceServiceImpl implements DeviceServiceSync {
    private final DeviceRepository deviceRepository;

    private final DeviceMapper deviceMapper;

    @Override
    public DeviceDTO save(DeviceDTO deviceDTO) {
        log.debug("Request to save Device : {}", deviceDTO);

        Device device = deviceMapper.toEntity(deviceDTO);
        device = deviceRepository.save(device);

        return deviceMapper.toDto(device);
    }

    @Override
    public Optional<DeviceDTO> partialUpdate(DeviceDTO deviceDTO) {
        log.debug("Request to partially update Device : {}", deviceDTO);

        return deviceRepository
                .findById(deviceDTO.getId())
                .map(existingDevice -> {
                    deviceMapper.partialUpdate(existingDevice, deviceDTO);

                    return existingDevice;
                })
                .map(deviceRepository::save)
                .map(deviceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Devices");

        return deviceRepository.findAll(pageable).map(deviceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeviceDTO> findOne(Long id) {
        log.debug("Request to get Device : {}", id);

        return deviceRepository.findById(id).map(deviceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Device : {}", id);

        deviceRepository.deleteById(id);
    }
}

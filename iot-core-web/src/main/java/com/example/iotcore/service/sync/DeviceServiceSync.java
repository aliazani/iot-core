package com.example.iotcore.service.sync;

import com.example.iotcore.domain.Device;
import com.example.iotcore.dto.DeviceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link Device}.
 */
public interface DeviceServiceSync {
    /**
     * Save a device.
     *
     * @param deviceDTO the entity to save.
     * @return the persisted entity.
     */
    DeviceDTO save(DeviceDTO deviceDTO);

    /**
     * Partially updates a device.
     *
     * @param deviceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DeviceDTO> partialUpdate(DeviceDTO deviceDTO);

    /**
     * Get all the devices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DeviceDTO> findAll(Pageable pageable);

    /**
     * Get the "id" device.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DeviceDTO> findOne(Long id);

    /**
     * Delete the "id" device.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

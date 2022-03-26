package com.example.iotcorenew.web.controller.sync;

import com.example.iotcorenew.domain.Device;
import com.example.iotcorenew.service.dto.DeviceDTO;
import com.example.iotcorenew.service.sync.DeviceServiceSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link Device}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DeviceControllerSync {
    private static final String ENTITY_NAME = "device";
    private final DeviceServiceSync deviceService;

//    private final DeviceRepository deviceRepository;

    /**
     * {@code POST  /devices} : Create a new device.
     *
     * @param deviceDTO the deviceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deviceDTO, or with status {@code 400 (Bad Request)} if the device has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/devices")
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceDTO deviceDTO) throws URISyntaxException {
        log.debug("REST request to save Device : {}", deviceDTO);

//        if (deviceDTO.getId() != null) {
//            throw new BadRequestAlertException("A new device cannot already have an ID", ENTITY_NAME, "idexists");
//        }
        DeviceDTO result = deviceService.save(deviceDTO);

        return ResponseEntity
                .created(new URI("/api/devices/" + result.getId()))
                .body(result);
    }

    /**
     * {@code PUT  /devices/:id} : Updates an existing device.
     *
     * @param id        the id of the deviceDTO to save.
     * @param deviceDTO the deviceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deviceDTO,
     * or with status {@code 400 (Bad Request)} if the deviceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deviceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody DeviceDTO deviceDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Device : {}, {}", id, deviceDTO);

        if (deviceDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deviceDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

//        if (!deviceRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }

        DeviceDTO result = deviceService.save(deviceDTO);

        return ResponseEntity
                .ok()
                .body(result);
    }

    /**
     * {@code PATCH  /devices/:id} : Partial updates given fields of an existing device, field will ignore if it is null
     *
     * @param id        the id of the deviceDTO to save.
     * @param deviceDTO the deviceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deviceDTO,
     * or with status {@code 400 (Bad Request)} if the deviceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deviceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deviceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/devices/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<DeviceDTO> partialUpdateDevice(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody DeviceDTO deviceDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Device partially : {}, {}", id, deviceDTO);
        if (deviceDTO.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deviceDTO.getId())) {
//            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

//        if (!deviceRepository.existsById(id)) {
//            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
//        }

        Optional<DeviceDTO> result = deviceService.partialUpdate(deviceDTO);

        return ResponseEntity.ok().body(result.orElse(null));
    }

    /**
     * {@code GET  /devices} : get all the devices.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of devices in body.
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceDTO>> getAllDevices(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Devices");

        Page<DeviceDTO> page = deviceService.findAll(pageable);

        return ResponseEntity.ok().body(page.getContent());
    }

    /**
     * {@code GET  /devices/:id} : get the "id" device.
     *
     * @param id the id of the deviceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deviceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        log.debug("REST request to get Device : {}", id);

        Optional<DeviceDTO> deviceDTO = deviceService.findOne(id);

        return ResponseEntity.ok().body(deviceDTO.orElse(null));
    }

    /**
     * {@code DELETE  /devices/:id} : delete the "id" device.
     *
     * @param id the id of the deviceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.debug("REST request to delete Device : {}", id);

        deviceService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

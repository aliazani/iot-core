package com.example.iotcore.web.controller;

import com.example.iotcore.domain.Device;
import com.example.iotcore.dto.DeviceDTO;
import com.example.iotcore.repository.DeviceRepository;
import com.example.iotcore.service.DeviceService;
import com.example.iotcore.util.HeaderUtil;
import com.example.iotcore.util.PaginationUtil;
import com.example.iotcore.util.ResponseUtil;
import com.example.iotcore.web.errors.BadRequestAlertException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
@Tag(name = "Device", description = "Device Management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DeviceController {

    private static final String ENTITY_NAME = "device";
    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    @Value("${application.clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /devices} : Create a new device.
     *
     * @param deviceDTO the deviceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deviceDTO, or with status {@code 400 (Bad Request)} if the device has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create new device", description = "Create new device",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Device created successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceDTO.class))}
                            , headers = @Header(name = "Location", description = "The URL of the created device")
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (A new device cannot already have an ID" +
                                    " / a device with the same mac address already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "DeviceDTO",
                    value = "{\"macAddress\":\"string\"}")}))
    @PostMapping("/devices")
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceDTO deviceDTO) throws URISyntaxException {
        log.debug("REST request to save Device : {}", deviceDTO);

        if (deviceDTO.getId() != null)
            throw new BadRequestAlertException("A new %s cannot already have an ID".formatted(ENTITY_NAME)
                    , ENTITY_NAME, "idexists");
        if (deviceRepository.findByMacAddressIgnoreCase(deviceDTO.getMacAddress()).isPresent())
            throw new BadRequestAlertException("A new %s cannot have existed mac address".formatted(ENTITY_NAME)
                    , ENTITY_NAME, "macaddressexists");

        DeviceDTO result = deviceService.save(deviceDTO);

        return ResponseEntity
                .created(new URI("/api/devices/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
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
     */
    @Operation(summary = "Update a device", description = "Update a device",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device updated successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Device is not valid " +
                                    "/ Device with same mac address already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PutMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable(value = "id", required = false) final Long id,
                                                  @Valid @RequestBody DeviceDTO deviceDTO) {
        log.debug("REST request to update Device : {}, {}", id, deviceDTO);

        checkIdValidity(deviceDTO, id);
        if (deviceRepository.findByMacAddressIgnoreCase(deviceDTO.getMacAddress()).isPresent())
            throw new BadRequestAlertException("mac address already exists"
                    , ENTITY_NAME, "macaddressexists");

        DeviceDTO result = deviceService.save(deviceDTO);

        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        deviceDTO.getId().toString()))
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
     */
    @Operation(summary = "Patch a device", description = "Patch a device",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device patched successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (Device is not valid " +
                                    "/ Device with same mac address already exists)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Not found (to patch)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @PatchMapping(value = "/devices/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<DeviceDTO> partialUpdateDevice(@PathVariable(value = "id", required = false) final Long id,
                                                         @NotNull @RequestBody DeviceDTO deviceDTO) {
        log.debug("REST request to partial update Device partially : {}, {}", id, deviceDTO);

        checkIdValidity(deviceDTO, id);
        if (deviceRepository.findByMacAddressIgnoreCase(deviceDTO.getMacAddress()).isPresent())
            throw new BadRequestAlertException("mac address already exists"
                    , ENTITY_NAME, "macaddressexists");

        Optional<DeviceDTO> result = deviceService.partialUpdate(deviceDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        deviceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /devices} : get all the devices.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of devices in body.
     */

    @Operation(summary = "Get all devices paged", description = "Get all devices paged",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a page of devices",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceDTO>> getAllDevices(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Devices");

        Page<DeviceDTO> page = deviceService.findAll(pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /devices/:id} : get the "id" device.
     *
     * @param id the id of the deviceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deviceDTO, or with status {@code 404 (Not Found)}.
     */

    @Operation(summary = "Find a device by ID", description = "Find a device by ID",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a device",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeviceDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Device Not found",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        log.debug("REST request to get Device : {}", id);

        Optional<DeviceDTO> deviceDTO = deviceService.findOne(id);

        return ResponseUtil.wrapOrNotFound(deviceDTO);
    }

    /**
     * {@code DELETE  /devices/:id} : delete the "id" device.
     *
     * @param id the id of the deviceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */

    @Operation(summary = "Delete a device", description = "Delete a device",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Device successfully deleted",
                            content = {@Content(mediaType = "application/json")}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.debug("REST request to delete Device : {}", id);

        deviceService.delete(id);

        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                        ENTITY_NAME, id.toString()))
                .build();
    }

    private void checkIdValidity(DeviceDTO deviceDTO, Long id) {
        if (deviceDTO.getId() == null)
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        if (!Objects.equals(id, deviceDTO.getId()))
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");

        if (!deviceRepository.existsById(id))
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    }
}

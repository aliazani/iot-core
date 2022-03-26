package com.example.iotcorenew.repository;

import com.example.iotcorenew.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Device entity.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
}
package com.example.iotcore.security.repository;

import com.example.iotcore.security.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}

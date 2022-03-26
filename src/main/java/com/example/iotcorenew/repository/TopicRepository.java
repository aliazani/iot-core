package com.example.iotcorenew.repository;

import com.example.iotcorenew.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Topic entity.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
}


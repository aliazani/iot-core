package com.example.iotcore.repository;

import com.example.iotcore.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Topic entity.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
}


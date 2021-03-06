package com.example.iotcore.repository;

import com.example.iotcore.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Message entity.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}


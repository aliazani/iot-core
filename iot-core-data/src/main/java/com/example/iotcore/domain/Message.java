package com.example.iotcore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Message.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "message")
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "created_time_stamp")
    private Instant createdTimeStamp;

    @Column(name = "message_type")
    private Integer messageType;

    @OneToOne
    @JoinColumn(unique = true)
    private Device device;

    @OneToOne
    @JoinColumn(unique = true)
    private Topic topic;
}


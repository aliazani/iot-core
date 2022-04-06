package com.example.iotcore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
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

    @JoinColumn(name = "device")
    @ManyToOne(targetEntity = Device.class)
    private Device device;


    @JoinColumn(name = "topic")
    @ManyToOne(targetEntity = Topic.class)
    private Topic topic;
}


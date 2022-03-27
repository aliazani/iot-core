-- liquibase formatted sql

-- changeset ali: 1
CREATE TABLE IF NOT EXISTS device
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    mac_address VARCHAR(255)          NULL,
    CONSTRAINT PK_DEVICE PRIMARY KEY (id)
);

-- changeset ali: 2
CREATE TABLE IF NOT EXISTS topic
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NULL,
    CONSTRAINT PK_TOPIC PRIMARY KEY (id)
);

-- changeset ali: 3
CREATE TABLE IF NOT EXISTS message
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    content            VARCHAR(255)          NULL,
    created_time_stamp datetime              NULL,
    message_type       INT                   NULL,
    device_id          BIGINT                NULL,
    topic_id           BIGINT                NULL,
    CONSTRAINT PK_MESSAGE PRIMARY KEY (id),
    UNIQUE (device_id),
    UNIQUE (topic_id)
);
ALTER TABLE message
    ADD CONSTRAINT FKkjoquerqtrcvqgnxlkfn7iwv0 FOREIGN KEY (topic_id) REFERENCES topic (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE message
    ADD CONSTRAINT FKml7ww4agpbfyn5eiqq5o9vte6 FOREIGN KEY (device_id) REFERENCES device (id) ON UPDATE RESTRICT ON DELETE RESTRICT;


-- liquibase formatted sql

-- changeset mohammadali:1648417070412-1
CREATE TABLE device (id BIGINT AUTO_INCREMENT NOT NULL, mac_address VARCHAR(255) NULL, CONSTRAINT PK_DEVICE PRIMARY KEY (id));

-- changeset mohammadali:1648417070412-2
CREATE TABLE message (id BIGINT AUTO_INCREMENT NOT NULL, content VARCHAR(255) NULL, created_time_stamp datetime NULL, message_type INT NULL, device_id BIGINT NULL, topic_id BIGINT NULL, CONSTRAINT PK_MESSAGE PRIMARY KEY (id));

-- changeset mohammadali:1648417070412-3
CREATE TABLE topic (id BIGINT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NULL, CONSTRAINT PK_TOPIC PRIMARY KEY (id));

-- changeset mohammadali:1648417070412-4
CREATE INDEX FKkjoquerqtrcvqgnxlkfn7iwv0 ON message(topic_id);

-- changeset mohammadali:1648417070412-5
CREATE INDEX FKml7ww4agpbfyn5eiqq5o9vte6 ON message(device_id);

-- changeset mohammadali:1648417070412-6
ALTER TABLE message ADD CONSTRAINT FKkjoquerqtrcvqgnxlkfn7iwv0 FOREIGN KEY (topic_id) REFERENCES topic (id) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset mohammadali:1648417070412-7
ALTER TABLE message ADD CONSTRAINT FKml7ww4agpbfyn5eiqq5o9vte6 FOREIGN KEY (device_id) REFERENCES device (id) ON UPDATE RESTRICT ON DELETE RESTRICT;


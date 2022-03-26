--liquibase formatted sql

-- device table
CREATE TABLE IF NOT EXISTS `t_iot`.`device`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `mac_address` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- topic table
CREATE TABLE IF NOT EXISTS `t_iot`.`topic`
(
    `id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- message table
CREATE TABLE IF NOT EXISTS `t_iot`.`message`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `content`            VARCHAR(255) NULL DEFAULT NULL,
    `created_time_stamp` DATETIME(6)  NULL DEFAULT NULL,
    `message_type`       INT          NULL DEFAULT NULL,
    `device_id`          BIGINT       NULL DEFAULT NULL,
    `topic_id`           BIGINT       NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_jnti37mg6c7x5qtna97ed4d6k` (`device_id` ASC) VISIBLE,
    UNIQUE INDEX `UK_mo507ebgb8jigyefx1qfedibw` (`topic_id` ASC) VISIBLE,
    CONSTRAINT `FKkjoquerqtrcvqgnxlkfn7iwv0`
        FOREIGN KEY (`topic_id`)
            REFERENCES `t_iot`.`topic` (`id`),
    CONSTRAINT `FKml7ww4agpbfyn5eiqq5o9vte6`
        FOREIGN KEY (`device_id`)
            REFERENCES `t_iot`.`device` (`id`)
);
-- liquibase formatted sql

-- changeset mohammadali:1649271964885-1
CREATE TABLE authority
(
    name VARCHAR(50) NOT NULL,
    CONSTRAINT PK_AUTHORITY PRIMARY KEY (name)
);

-- changeset mohammadali:1649271964885-2
CREATE TABLE device
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    mac_address VARCHAR(255)          NOT NULL,
    CONSTRAINT PK_DEVICE PRIMARY KEY (id),
    UNIQUE (mac_address)
);

-- changeset mohammadali:1649271964885-3
CREATE TABLE message
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    content            VARCHAR(255)          NULL,
    created_time_stamp datetime              NULL,
    device             BIGINT                NULL,
    topic              BIGINT                NULL,
    CONSTRAINT PK_MESSAGE PRIMARY KEY (id)
);

-- changeset mohammadali:1649271964885-4
CREATE TABLE topic
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT PK_TOPIC PRIMARY KEY (id),
    UNIQUE (name)
);

-- changeset mohammadali:1649271964885-5
CREATE TABLE user
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_by         VARCHAR(50)           NOT NULL,
    created_date       datetime              NULL,
    last_modified_by   VARCHAR(50)           NULL,
    last_modified_date datetime              NULL,
    activated          BIT(1)                NOT NULL,
    activation_key     VARCHAR(20)           NULL,
    email              VARCHAR(254)          NULL,
    first_name         VARCHAR(50)           NULL,
    image_url          VARCHAR(256)          NULL,
    lang_key           VARCHAR(10)           NULL,
    last_name          VARCHAR(50)           NULL,
    login              VARCHAR(50)           NOT NULL,
    password_hash      VARCHAR(60)           NOT NULL,
    reset_date         datetime              NULL,
    reset_key          VARCHAR(20)           NULL,
    CONSTRAINT PK_USER PRIMARY KEY (id),
    UNIQUE (email),
    UNIQUE (login)
);

-- changeset mohammadali:1649271964885-6
CREATE TABLE user_authority
(
    user_id        BIGINT      NOT NULL,
    authority_name VARCHAR(50) NOT NULL,
    CONSTRAINT PK_USER_AUTHORITY PRIMARY KEY (user_id, authority_name)
);

-- changeset mohammadali:1649271964885-7
CREATE INDEX FK2uicl69t7fd5s804dh9atq1rr ON message (device);

-- changeset mohammadali:1649271964885-8
CREATE INDEX FK6ktglpl5mjosa283rvken2py5 ON user_authority (authority_name);

-- changeset mohammadali:1649271964885-9
CREATE INDEX FKidr9y3v3kvdc5ym5a0t5koabn ON message (topic);

-- changeset mohammadali:1649271964885-10
ALTER TABLE message
    ADD CONSTRAINT FK2uicl69t7fd5s804dh9atq1rr FOREIGN KEY (device) REFERENCES device (id) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset mohammadali:1649271964885-11
ALTER TABLE user_authority
    ADD CONSTRAINT FK6ktglpl5mjosa283rvken2py5 FOREIGN KEY (authority_name) REFERENCES authority (name) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset mohammadali:1649271964885-12
ALTER TABLE message
    ADD CONSTRAINT FKidr9y3v3kvdc5ym5a0t5koabn FOREIGN KEY (topic) REFERENCES topic (id) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset mohammadali:1649271964885-13
ALTER TABLE user_authority
    ADD CONSTRAINT FKpqlsjpkybgos9w2svcri7j8xy FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE RESTRICT ON DELETE RESTRICT;


-- changeset mohammadali:1649271964885-14
INSERT INTO authority (name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_USER');

INSERT INTO user (id, login, password_hash, first_name, last_name, email, activated,
                  lang_key, created_by, last_modified_by)
VALUES (1, 'admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',
        'Administrator', 'Administrator', 'admin@localhost', true, 'en', 'system', 'system');

INSERT INTO user_authority (user_id, authority_name)
VALUES (1, 'ROLE_ADMIN');

-- changeset mohammadali:1649271964885-15 context:test
INSERT INTO device(mac_address)
VALUES ('00:00:00:00:00:00'),
       ('11:11:11:11:11:11'),
       ('22:22:22:22:22:22');
INSERT INTO topic(name)
VALUES ('topic1'),
       ('topic2'),
       ('topic3');
INSERT INTO message(content, created_time_stamp, device, topic)
VALUES ('message1', '2015-04-13 11:43:47', 1, 1),
       ('message2', '2016-04-13 11:43:47', 2, 2);

INSERT INTO user (id, login, password_hash, first_name, last_name, email, activated,
                  lang_key, created_by, last_modified_by)
VALUES (2, 'user', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',
        'firstname-user', 'lastname-user', 'user@localhost.com', true, 'en', 'system', 'system'),

       (3, 'user-admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',
        'firstname-admin-user', 'lastname-admin-user', 'user_admin@localhost.com', true, 'en', 'system', 'system');


INSERT INTO user_authority (user_id, authority_name)
VALUES (2, 'ROLE_ADMIN'), (3, 'ROLE_USER'), (3, 'ROLE_ADMIN');


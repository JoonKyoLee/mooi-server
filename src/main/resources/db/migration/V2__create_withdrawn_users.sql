-- V2__create_withdrawn_users.sql

CREATE TABLE `withdrawn_users`
(
    `withdrawn_user_id` bigint      NOT NULL AUTO_INCREMENT,
    `user_id`           bigint      NOT NULL,
    `withdrawn_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `purge_after`       datetime    NOT NULL,
    `purge_status`      varchar(20) NOT NULL DEFAULT 'PENDING',
    `purged_at`         datetime             DEFAULT NULL,
    `fail_reason`       varchar(255)         DEFAULT NULL,

    PRIMARY KEY (`withdrawn_user_id`),

    UNIQUE KEY `uk_withdrawn_users_user_id` (`user_id`),
    KEY `idx_withdrawn_users_status_purge_after` (`purge_status`, `purge_after`),
    KEY `idx_withdrawn_users_purge_after` (`purge_after`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `evadecoration_user` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`uuid` VARCHAR(16) NOT NULL COLLATE 'utf8_general_ci',
	`clothes_data` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_bin',
	`updated_at` DATETIME NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
	`created_at` DATETIME NOT NULL DEFAULT current_timestamp(),
	PRIMARY KEY (`id`) USING BTREE
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

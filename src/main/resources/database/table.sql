-- 建表
create table tb_config (
	`id` varchar(36) NOT NULL,
    `createDate` TIMESTAMP NOT NULL,
    `modifyDate` TIMESTAMP NOT NULL,
    `name` VARCHAR(255) DEFAULT NULL,
    `value` VARCHAR(255) DEFAULT NULL,
	PRIMARY KEY (`id`)
);
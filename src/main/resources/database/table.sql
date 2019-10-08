-- 配置
create table tb_config (
	`id` VARCHAR(36) NOT NULL,
    `createDate` TIMESTAMP NOT NULL,
    `modifyDate` TIMESTAMP NOT NULL,
    `name` VARCHAR(256) DEFAULT NULL,
    `value` MEDIUMTEXT DEFAULT NULL,
	PRIMARY KEY (`id`)
);
-- 任务：BT下载文件过多时可以修改description类型
create table tb_task (
	`id` VARCHAR(36) NOT NULL,
    `createDate` TIMESTAMP NOT NULL,
    `modifyDate` TIMESTAMP NOT NULL,
    `name` VARCHAR(1024) DEFAULT NULL,
    `type` VARCHAR(32) DEFAULT NULL,
    `fileType` VARCHAR(32) DEFAULT NULL,
    `file` VARCHAR(2048) DEFAULT NULL,
    `url` VARCHAR(5120) DEFAULT NULL,
    `torrent` VARCHAR(2048) DEFAULT NULL,
    `status` VARCHAR(32) DEFAULT NULL,
    `size` BIGINT DEFAULT NULL,
    `endDate` TIMESTAMP DEFAULT NULL,
    `description` MEDIUMTEXT DEFAULT NULL,
	PRIMARY KEY (`id`)
);

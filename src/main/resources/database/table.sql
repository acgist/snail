/**
 * <p>建表SQL语句</p>
 * <p>如果项目启动数据库没有初始化，使用该SQL语句初始化数据库。</p>
 */
/**
 * <p>配置</p>
 */
create table tb_config (
	`id` CHAR(36) NOT NULL,
	`createDate` TIMESTAMP NOT NULL,
	`modifyDate` TIMESTAMP NOT NULL,
	`name` VARCHAR(256) DEFAULT NULL,
	`value` MEDIUMTEXT DEFAULT NULL,
	PRIMARY KEY (`id`)
);
/**
 * <p>任务</p>
 */
create table tb_task (
	`id` CHAR(36) NOT NULL,
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

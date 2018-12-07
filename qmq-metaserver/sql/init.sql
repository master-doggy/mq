CREATE TABLE `broker_group`
(
  `id`             INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_name`     VARCHAR(30)      NOT NULL DEFAULT '' COMMENT 'group名字',
  `kind`           INT              NOT NULL DEFAULT '-1' COMMENT '类型',
  `master_address` VARCHAR(25)      NOT NULL DEFAULT '' COMMENT 'master地址,ip:port',
  `broker_state`   TINYINT          NOT NULL DEFAULT '-1' COMMENT 'broker master 状态',
  `tag`            VARCHAR(30)      NOT NULL DEFAULT '' COMMENT '分组的标签',
  `create_time`    TIMESTAMP        NOT NULL DEFAULT '1970-01-01 08:00:01' COMMENT '创建时间',
  `update_time`    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_group_name` (`group_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'broker组信息';

CREATE TABLE `subject_info`
(
  `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '主题名',
  `tag`         VARCHAR(30)      NOT NULL DEFAULT '' COMMENT 'tag',
  `create_time` TIMESTAMP        NOT NULL DEFAULT '1970-01-01 08:00:01' COMMENT '创建时间',
  `update_time` TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '主题信息';

CREATE TABLE `subject_route`
(
  `id`                INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_info`      VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '主题',
  `broker_group_json` VARCHAR(300)     NOT NULL DEFAULT '' COMMENT 'broker group name信息，json存储',
  `version`           INT              NOT NULL DEFAULT 0 COMMENT '版本信息',
  `create_time`       TIMESTAMP        NOT NULL DEFAULT '1970-01-01 08:00:01'
    COMMENT '创建时间',
  `update_time`       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_subject` (`subject_info`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '主题路由信息';

CREATE TABLE client_meta_info
(
  `id`             INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_info`   VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '主题',
  `client_type`    TINYINT          NOT NULL DEFAULT 0 COMMENT 'client类型',
  `consumer_group` VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '消费组',
  `client_id`      VARCHAR(100)     NOT NULL DEFAULT '' COMMENT 'client id',
  `app_code`       VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '应用',
  `room`           VARCHAR(20)      NOT NULL DEFAULT '' COMMENT '机房',
  `create_time`    TIMESTAMP        NOT NULL DEFAULT '1970-01-01 08:00:01' COMMENT '创建时间',
  `update_time`    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_subject_client_type_consumer_group_client_id` (`subject_info`, `client_type`, `consumer_group`, `client_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '订阅关系表';

CREATE TABLE `client_offline_state`
(
  `id`             INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `client_id`      VARCHAR(100)     NOT NULL DEFAULT '' COMMENT 'client id',
  `subject_info`   VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '主题',
  `consumer_group` VARCHAR(100)     NOT NULL DEFAULT '' COMMENT '消费组',
  `state`          TINYINT          NOT NULL DEFAULT 0 COMMENT '上下线状态，0上线，1下线',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_client_id_subject_group` (`client_id`, `subject_info`, `consumer_group`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '下线的client';

CREATE TABLE `broker`
(
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `group_name`  VARCHAR(30)     NOT NULL DEFAULT '' COMMENT 'group名字',
  `hostname`    VARCHAR(50)     NOT NULL DEFAULT '' COMMENT '机器名',
  `ip`          INT UNSIGNED    NOT NULL DEFAULT '0' COMMENT '机器IP',
  `role`        INT             NOT NULL DEFAULT '-1' COMMENT '角色',
  `serve_port`  INT             NOT NULL DEFAULT '20881' COMMENT '客户端请求端口',
  `sync_port`   INT             NOT NULL DEFAULT '20882' COMMENT '从机同步端口',
  `create_time` TIMESTAMP       NOT NULL DEFAULT '2018-01-01 01:01:01' COMMENT '创建时间',
  `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY uniq_broker (`hostname`, `serve_port`),
  UNIQUE KEY uniq_group_role (`group_name`, `role`),
  KEY idx_broker_group (`group_name`)
) ENGINE InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT 'broker列表';

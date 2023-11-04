CREATE TABLE `department` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint NOT NULL DEFAULT '0' COMMENT '创建用户',
    `update_user` bigint NOT NULL DEFAULT '0' COMMENT '更新用户',
    `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级ID',
    `parent_path` varchar(50) NOT NULL DEFAULT '' COMMENT '祖级路径',
    `name` varchar(50) NOT NULL DEFAULT '' COMMENT '部门名称',
    `order_num` smallint NOT NULL DEFAULT '0' COMMENT '显示顺序',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '部门状态 0=正常,1=停用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门信息';
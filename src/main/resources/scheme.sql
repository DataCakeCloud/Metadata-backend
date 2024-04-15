CREATE TABLE  IF NOT EXISTS `header` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `type` int(2) DEFAULT NULL COMMENT '类型',
  `create_user` varchar(128) DEFAULT NULL COMMENT '创建人',
  `headers` text COMMENT '表头',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表头';

CREATE TABLE  IF NOT EXISTS `looker_job_base_daily` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(255) DEFAULT NULL,
  `owner` varchar(64) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `lowscore` tinyint(4) DEFAULT NULL,
  `lowcpu` tinyint(4) DEFAULT NULL,
  `lowmem` tinyint(4) DEFAULT NULL,
  `dataskew` tinyint(4) DEFAULT NULL,
  `highcost` tinyint(4) DEFAULT NULL,
  `dt` varchar(16) DEFAULT NULL,
  `last_7day_memory_min_used` decimal(4,2) DEFAULT NULL,
  `last_7day_memory_max_used` decimal(4,2) DEFAULT NULL,
  `last_7day_memory_avg_used` decimal(4,2) DEFAULT NULL,
  `last_7day_cpu_min_used` decimal(4,2) DEFAULT NULL,
  `last_7day_cpu_max_used` decimal(4,2) DEFAULT NULL,
  `last_7day_cpu_avg_used` decimal(4,2) DEFAULT NULL,
  `score_rank` double DEFAULT NULL,
  `job_name_total_price` decimal(18,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `job_name` (`job_name`) USING BTREE,
  KEY `dt` (`dt`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;


CREATE TABLE  IF NOT EXISTS `looker_table_base_daily` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建区域',
  `db_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '库',
  `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表名',
  `table_bucket_name` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `tag_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '标签',
  `owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '存储路径',
  `last_activity_count` int(11) DEFAULT NULL COMMENT '最近三十天访问次数',
  `transient_lastDdlTime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表变更时间',
  `total_storage` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `table_standard_size` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `table_intelligent_size` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `table_deep_size` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `table_archive_size` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `multi_count` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '重复映射表数量',
  `table_storage_dt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `dt` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'CURRENT_TIMESTAMP',
  `sd_file_format` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '文件类型',
  `partition_keys` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分区字段',
  `last_activity_time` datetime DEFAULT NULL COMMENT '最近访问时间',
  `last_activity_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '最近访问人',
  `table_small_object_num` int(11) DEFAULT NULL COMMENT '小文件个数',
  `table_small_object_total_size` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `table_info_id` bigint(20) DEFAULT NULL,
  `table_partition_num` mediumtext,
  PRIMARY KEY (`id`),
  KEY `dt` (`dt`) USING BTREE,
  KEY `table_info_id` (`table_info_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;


CREATE TABLE  IF NOT EXISTS `looker_metrics_daily` (
  `tagName` varchar(255) NOT NULL,
  `tagCode` varchar(16) NOT NULL,
  `value` int(11) DEFAULT NULL,
  `DoD` varchar(8) DEFAULT NULL,
  `DoDType` varchar(8) DEFAULT NULL,
  `WoW` varchar(8) DEFAULT NULL,
  `WoWType` varchar(8) DEFAULT NULL,
  `timeline` json DEFAULT NULL,
  `dt` varchar(255) DEFAULT NULL,
  KEY `dt` (`dt`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE  IF NOT EXISTS `zombie` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `zombie_data` bigint(20) DEFAULT NULL,
  `cloud_type` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `bucket_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `dt` varchar(25) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE  IF NOT EXISTS `checkup_detail` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `scan_id` varchar(32) DEFAULT NULL COMMENT '扫描id',
  `create_time` varchar(128) DEFAULT NULL COMMENT '扫描时间',
  `create_user` varchar(128) DEFAULT '0',
  `metrics_type` int(11) DEFAULT '1' COMMENT '扫描的类型,30天，无人',
  `looker_id` int(11) DEFAULT NULL COMMENT 'looker_*_base_daily表主键',
  `last_activity_time` datetime DEFAULT NULL COMMENT '表：最后访问时间',
  `last_activity_user` varchar(32) DEFAULT 'b''0''' COMMENT '最后访问人',
  `job_name` varchar(255) DEFAULT '0',
  `table_exits` bigint(20) DEFAULT NULL COMMENT 'true-存在。false-已经删除',
  `gov_record_info_after_task_id` bigint(20) DEFAULT NULL,
  `is_finished` bigint(20) DEFAULT NULL COMMENT '1-完成，0-执行中',
  `gov_record_info_before_task_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `job_name` (`job_name`) USING BTREE,
  KEY `looker_id` (`looker_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='体检明细表';

CREATE TABLE  IF NOT EXISTS `checkup` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `create_user` varchar(128) DEFAULT NULL COMMENT '扫描人',
  `create_time` varchar(128) DEFAULT NULL COMMENT '扫描时间',
  `level` int(11) DEFAULT '0' COMMENT '范围 数据表还是 数据任务',
  `scope` int(11) DEFAULT '0' COMMENT '父id',
  `group_ids` text COMMENT '扫描的组织树',
  `no_visit30_day_tables_num` int(4) DEFAULT '0' COMMENT '30天未认领',
  `handle_no_visit30_day_tables_num` int(4) DEFAULT '0' COMMENT '30天已处理',
  `small_file_too_many_tables_num` int(4) DEFAULT '0' COMMENT '小文件太多',
  `handle_small_file_too_many_tables_num` int(4) DEFAULT '0' COMMENT '小文件太多已处理',
  `not_claim_tables_num` int(4) DEFAULT '0' COMMENT '为认领',
  `handle_not_claim_tables_num` int(4) DEFAULT '0' COMMENT '未认领',
  `lower_score_tasks_num` int(4) DEFAULT '0' COMMENT '低评分',
  `handle_lower_score_tasks_num` int(4) DEFAULT '0' COMMENT '低评分',
  `data_skew_tasks_num` int(4) DEFAULT '0' COMMENT '数据倾斜总数',
  `handle_data_skew_tasks_num` int(4) DEFAULT '0' COMMENT '已处理的数据倾斜',
  `has_govern` bit(1) DEFAULT b'0' COMMENT '已处理',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='体检表';

-- Create syntax for TABLE 'access_group'
CREATE TABLE  IF NOT EXISTS `access_group` (
  `id` int(11) NOT NULL COMMENT '主键',
  `name` varchar(128) DEFAULT NULL COMMENT '名称',
  `e_name` varchar(128) DEFAULT NULL COMMENT '部门别名',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户id',
  `parent_id` int(11) DEFAULT NULL COMMENT '父id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `type` int(4) DEFAULT '0' COMMENT '0是组，1是人',
  `hierarchy` int(4) DEFAULT '1' COMMENT '层级',
  `is_leader` int(4) DEFAULT NULL COMMENT '0:是 ，1：不是',
  `delete_status` varchar(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0：未删除；1：已删除',
  `standard` int(1) unsigned zerofill DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='group表 ';

-- Create syntax for TABLE 'access_user'
CREATE TABLE  IF NOT EXISTS `access_user` (
  `id` int(11) NOT NULL COMMENT '主键',
  `name` varchar(128) DEFAULT NULL COMMENT '名称',
  `email` varchar(128) DEFAULT NULL COMMENT '用户邮箱',
  `password` varchar(1000) DEFAULT NULL COMMENT '用户密码',
  `description` text COMMENT '描述',
  `company_id` varchar(128) DEFAULT NULL COMMENT '企业id',
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `tenancy_code` varchar(128) DEFAULT NULL COMMENT '企业部门',
  `source` varchar(128) DEFAULT NULL COMMENT '来源',
  `latest_code` varchar(128) DEFAULT NULL COMMENT '最新验证码',
  `freeze_status` varchar(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0：启动；1：冻结',
  `delete_status` varchar(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0：未删除；1：已删除',
  `access_group_id` int(15) DEFAULT NULL COMMENT '组织架构id',
  `business_group` varchar(255) DEFAULT NULL COMMENT '用户名称',
  `standard` int(10) unsigned zerofill DEFAULT NULL,
  `ratio` decimal(10,2) NOT NULL DEFAULT '1.00',
  PRIMARY KEY (`id`),
  KEY `owner` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表 ';

-- Create syntax for TABLE 'authority_gov_info'
CREATE TABLE  IF NOT EXISTS `authority_gov_info` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '表名',
  `operator` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '操作发起用户',
  `operate` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限操作',
  `permission` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限',
  `operated_user` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '被操作对象',
  `user_name` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色中的用户',
  `reason` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '原因',
  `execute_status` int(20) DEFAULT NULL COMMENT '执行状态',
  `cycle` int(20) DEFAULT '2' COMMENT '1:1天，2:1周，3:1月，4:3月，5:永久',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE  IF NOT EXISTS `table_info_subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `subject` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'billing_de_cost_daily'
CREATE TABLE  IF NOT EXISTS `billing_de_cost_daily` (
  `id` int(11) DEFAULT NULL,
  `tenant_id` varchar(50) DEFAULT NULL,
  `tenant_name` varchar(50) DEFAULT NULL,
  `owner` varchar(70) DEFAULT NULL,
  `group` varchar(70) DEFAULT NULL,
  `access_group_id` int(11) DEFAULT NULL COMMENT '任务的业务组id',
  `access_user_group` varchar(255) DEFAULT NULL COMMENT '用户的组织名称',
  `access_user_group_id` int(11) DEFAULT NULL COMMENT '用户的组织id',
  `access_business_group` varchar(255) DEFAULT NULL COMMENT '任务的业务组名称',
  `job_name` varchar(255) DEFAULT NULL,
  `job_id` varchar(50) DEFAULT NULL,
  `org_id` varchar(50) DEFAULT NULL,
  `provider` varchar(50) DEFAULT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `life_cycle` varchar(30) DEFAULT NULL,
  `instance_name` varchar(255) DEFAULT NULL,
  `original_price` double DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `hrs` double DEFAULT NULL,
  `sub_group` varchar(50) DEFAULT NULL,
  `assign_cluster_cost` double DEFAULT NULL,
  `remain_cluster_cost` double DEFAULT NULL,
  `inter_az_cost` double DEFAULT NULL,
  `ebs_gp2_cost` double DEFAULT NULL,
  `ebs_gp3_cost` double DEFAULT NULL,
  `evs_cost` double DEFAULT NULL,
  `bare_metal_discount` double DEFAULT NULL,
  `idle_resource_cost` double DEFAULT NULL,
  `cloud_watch_cost` double DEFAULT NULL,
  `ext1_cost` double DEFAULT NULL,
  `ext2_cost` double DEFAULT NULL,
  `dt` varchar(20) DEFAULT NULL,
  `product` varchar(20) DEFAULT NULL,
  `business_sepate` bit(1) DEFAULT b'0' COMMENT '是否业务分摊比例拆分了，默认没有',
  `engine` varchar(20) DEFAULT NULL,
  KEY `deindex` (`dt`,`tenant_name`,`job_name`,`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_de_cost_daily_business'
CREATE TABLE  IF NOT EXISTS `billing_de_cost_daily_business` (
  `id` int(11) DEFAULT NULL,
  `tenant_id` varchar(50) DEFAULT NULL,
  `tenant_name` varchar(50) DEFAULT NULL,
  `owner` varchar(70) DEFAULT NULL,
  `group` varchar(70) DEFAULT NULL,
  `access_group_id` int(11) DEFAULT NULL COMMENT '任务的业务组id',
  `access_user_group` varchar(255) DEFAULT NULL COMMENT '用户的组织名称',
  `access_user_group_id` int(11) DEFAULT NULL COMMENT '用户的组织id',
  `access_business_group` varchar(255) DEFAULT NULL COMMENT '任务的业务组名称',
  `job_name` varchar(255) DEFAULT NULL,
  `job_id` varchar(50) DEFAULT NULL,
  `org_id` varchar(50) DEFAULT NULL,
  `provider` varchar(50) DEFAULT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `life_cycle` varchar(30) DEFAULT NULL,
  `instance_name` varchar(255) DEFAULT NULL,
  `original_price` double DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `hrs` double DEFAULT NULL,
  `sub_group` varchar(50) DEFAULT NULL,
  `assign_cluster_cost` double DEFAULT NULL,
  `remain_cluster_cost` double DEFAULT NULL,
  `inter_az_cost` double DEFAULT NULL,
  `ebs_gp2_cost` double DEFAULT NULL,
  `ebs_gp3_cost` double DEFAULT NULL,
  `evs_cost` double DEFAULT NULL,
  `bare_metal_discount` double DEFAULT NULL,
  `idle_resource_cost` double DEFAULT NULL,
  `cloud_watch_cost` double DEFAULT NULL,
  `ext1_cost` double DEFAULT NULL,
  `ext2_cost` double DEFAULT NULL,
  `dt` varchar(20) DEFAULT NULL,
  `product` varchar(20) DEFAULT NULL,
  `ratio` decimal(10,5) DEFAULT '1.00000',
  KEY `keys` (`access_group_id`,`job_name`,`dt`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_diff_rate_daily'
CREATE TABLE  IF NOT EXISTS `billing_diff_rate_daily` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `tenant_id` varchar(200) DEFAULT NULL COMMENT '租户ID',
  `tenant_name` varchar(200) DEFAULT NULL COMMENT '租户名称',
  `diff_cost` double DEFAULT NULL COMMENT '成本差额',
  `diff_rate` double DEFAULT NULL COMMENT '成本差额率',
  `dt` varchar(200) DEFAULT NULL COMMENT '日期标识:yyyy-MM-dd',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控差额表';

-- Create syntax for TABLE 'billing_edp_discount_daily'
CREATE TABLE  IF NOT EXISTS `billing_edp_discount_daily` (
  `id` int(11) DEFAULT NULL,
  `provider` varchar(200) DEFAULT NULL,
  `discount_rate` double DEFAULT NULL,
  `dt` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_qe_cost_daily'
CREATE TABLE  IF NOT EXISTS `billing_qe_cost_daily` (
  `id` int(10) NOT NULL,
  `tenant_id` varchar(255) NOT NULL,
  `tenant_name` varchar(255) NOT NULL,
  `owner` varchar(100) DEFAULT NULL,
  `provider` varchar(100) NOT NULL,
  `region` varchar(100) NOT NULL,
  `instance_name` varchar(255) NOT NULL,
  `life_cycle` varchar(50) NOT NULL,
  `query_id` varchar(255) DEFAULT NULL,
  `query_used` bigint(19) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `dt` varchar(20) NOT NULL,
  `query_cost` decimal(25,20) DEFAULT NULL,
  `access_user_group_id` int(10) DEFAULT NULL,
  `access_user_group` varchar(255) DEFAULT NULL,
  KEY `indexs` (`dt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_qe_cost_dc_daily'
CREATE TABLE  IF NOT EXISTS `billing_qe_cost_dc_daily` (
  `dt` text,
  `engine_name` text,
  `node_instance` text,
  `cost` double NOT NULL,
  `used` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_storage_cost_daily'
CREATE TABLE  IF NOT EXISTS `billing_storage_cost_daily` (
  `bucket_name` text,
  `storage_type_size` double DEFAULT NULL,
  `dt` text NOT NULL,
  `group` text NOT NULL,
  `provider` text NOT NULL,
  `region` text,
  `tenant_id` text NOT NULL,
  `tenant_name` text NOT NULL,
  `storage_type_cost` double DEFAULT NULL,
  `storage_type` text NOT NULL,
  `tag_group` text,
  `create_time` text,
  `object_num` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'billing_tenant_edp_discount_daily'
CREATE TABLE  IF NOT EXISTS `billing_tenant_edp_discount_daily` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户ID',
  `tenant_name` varchar(255) DEFAULT NULL COMMENT '租户名称',
  `product_type` varchar(255) DEFAULT NULL COMMENT '产品类型 DE|QE',
  `discount` double DEFAULT NULL COMMENT '折扣率',
  `dt` varchar(200) DEFAULT NULL COMMENT '日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='EDP租户折扣表';

-- Create syntax for TABLE 'billing_tenant_instance_pricing_daily'
CREATE TABLE  IF NOT EXISTS `billing_tenant_instance_pricing_daily` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) DEFAULT NULL,
  `tenant_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `life_cycle` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `instance_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `instance_price` decimal(10,0) DEFAULT NULL,
  `dt` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'collect_info'
CREATE TABLE  IF NOT EXISTS `collect_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sole` varchar(1000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'catalog.db.name',
  `table_id` int(20) DEFAULT NULL COMMENT '表id',
  `user_id` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-取消1-收藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'data_grade'
CREATE TABLE  IF NOT EXISTS `data_grade` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL COMMENT '关联表table_info id',
  `grade_type` int(8) DEFAULT NULL COMMENT '数据等级类型，0：database， 1： table, 2: field',
  `grade` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '数据等级： 1级、2级、3级、4级',
  `name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '字段名称',
  `maintainer` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '维护人',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-有效1-无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`table_id`,`name`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `table_id` (`table_id`)
) ENGINE=InnoDB AUTO_INCREMENT=218 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'department_region_route_info'
CREATE TABLE  IF NOT EXISTS `department_region_route_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `department` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门',
  `alias_name` varchar(225) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '英文名称',
  `region` varchar(255) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '区域',
  `bucket_name` varchar(225) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '桶',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-有效1-无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'dict_info'
CREATE TABLE  IF NOT EXISTS `dict_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '数据类型',
  `key` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT 'key',
  `value` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT 'value',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-有效1-无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'gov_config'
CREATE TABLE  IF NOT EXISTS `gov_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(256) DEFAULT NULL,
  `bucket_name` varchar(256) NOT NULL,
  `path` varchar(256) NOT NULL,
  `smart_layer` int(11) DEFAULT NULL,
  `freez` int(11) DEFAULT NULL,
  `deep_freez` int(11) DEFAULT NULL,
  `delete` int(11) DEFAULT NULL,
  `bucket_types` int(11) DEFAULT NULL,
  `owner` varchar(100) DEFAULT NULL,
  `replace_shareid` varchar(100) DEFAULT NULL,
  `gov_type` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_active` int(11) DEFAULT NULL,
  `reason` varchar(256) DEFAULT NULL,
  `business_owner` varchar(256) DEFAULT NULL,
  `approval_status` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_id_mapping'
CREATE TABLE  IF NOT EXISTS `gov_job_id_mapping` (
  `dt` varchar(20) DEFAULT NULL,
  `job_id` varchar(50) DEFAULT NULL,
  `spark_app_id` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_info'
CREATE TABLE  IF NOT EXISTS `gov_job_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(512) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '任务名称',
  `department` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门名称',
  `score` bigint(20) DEFAULT NULL COMMENT '评分',
  `cpu_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT 'cpu利用率',
  `mem_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT 'mem利用率',
  `job_name_quantity` double DEFAULT '0' COMMENT '日均用量',
  `stranded_days` int(11) DEFAULT '0' COMMENT '滞留天数',
  `remind_num` int(11) DEFAULT '0' COMMENT '提醒次数',
  `govern_grade` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '治理等级 P0 P1 P2 P3 P4',
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '负责人',
  `status` tinyint(1) DEFAULT '1' COMMENT '0-非治理中 1-治理中',
  `shuffle` int(1) NOT NULL DEFAULT '0' COMMENT '是否倾斜',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `description` text COLLATE utf8mb4_bin COMMENT '备注',
  PRIMARY KEY (`job_name`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `job_name_UNIQUE` (`job_name`),
  FULLTEXT KEY `name` (`job_name`,`owner`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB AUTO_INCREMENT=20822 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'gov_job_metrics'
CREATE TABLE  IF NOT EXISTS `gov_job_metrics` (
  `dt` varchar(20) DEFAULT NULL COMMENT '执行日期',
  `property_type` varchar(100) DEFAULT NULL,
  `property_key` varchar(100) DEFAULT NULL COMMENT 'metrics_key',
  `property_value` varchar(100) DEFAULT NULL COMMENT 'metrics_value',
  `job_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `url` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_metrics_flag'
CREATE TABLE  IF NOT EXISTS `gov_job_metrics_flag` (
  `dt` varchar(20) DEFAULT NULL,
  `property_type` varchar(20) DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_score'
CREATE TABLE  IF NOT EXISTS `gov_job_score` (
  `job_name` text,
  `proba` float DEFAULT NULL,
  `score` float DEFAULT NULL,
  `score_rank` double DEFAULT NULL,
  `create_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_score_record'
CREATE TABLE  IF NOT EXISTS `gov_job_score_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dt` varchar(20) NOT NULL,
  `job_name` varchar(255) NOT NULL,
  `score` double DEFAULT NULL COMMENT '任务评分',
  `proba` double DEFAULT NULL COMMENT '评估概率值',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61745 DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_job_score_v2'
CREATE TABLE  IF NOT EXISTS `gov_job_score_v2` (
  `job_name` text,
  `proba` float DEFAULT NULL,
  `score` float DEFAULT NULL,
  `score_rank` double DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_record_info'
CREATE TABLE  IF NOT EXISTS `gov_record_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '实例id',
  `ds_id` int(10) DEFAULT NULL COMMENT 'ds平台id',
  `job_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务名',
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '策略名称',
  `score` bigint(20) DEFAULT NULL COMMENT '评分',
  `cpu_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT 'cpu利用率',
  `mem_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT 'mem利用率',
  `job_name_quantity` double DEFAULT NULL COMMENT '日均用量',
  `cpr_cpu_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '对比cpu利用率',
  `cpr_mem_use_ratio` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '对比mem利用率',
  `cpr_avg_daily_dosage` bigint(20) DEFAULT NULL COMMENT '对比日均用量',
  `last_avg_memory_used` double DEFAULT NULL COMMENT '最后job_id的memory平均利用率',
  `last_avg_cpu_used` double DEFAULT NULL COMMENT '最后job_id的cpu平均利用率',
  `before_conf` text COLLATE utf8mb4_bin COMMENT '任务治理前配置',
  `after_conf` text COLLATE utf8mb4_bin COMMENT '任务治理后配置',
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '负责人',
  `flag` tinyint(1) DEFAULT '1' COMMENT '配置修改方式:0-DS 1-GOV',
  `status` tinyint(1) DEFAULT '1' COMMENT '流程闭环状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `description` text COLLATE utf8mb4_bin COMMENT '备注',
  `dt` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '时间分区字段',
  `job_id_quantity` double DEFAULT NULL COMMENT '日均用量实例',
  `job_name_total_price` double DEFAULT NULL COMMENT '日均用量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'gov_shuffle_read_write_info'
CREATE TABLE  IF NOT EXISTS `gov_shuffle_read_write_info` (
  `dt` varchar(200) DEFAULT NULL,
  `job_name` varchar(200) DEFAULT NULL,
  `sum_input_bytes` mediumtext,
  `sum_shuffle_read_bytes` mediumtext,
  `sum_shuffle_write_bytes` mediumtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE  IF NOT EXISTS `gov_spark_cost` (
  `dt` text NOT NULL,
  `job_name` varchar(255) DEFAULT NULL,
  `owner` text,
  `cluster` text,
  `region` text,
  `tenant_id` text,
  `tenant_name` text,
  `department` mediumtext,
  `access_user_group_id` int(11) DEFAULT NULL,
  `groups` text NOT NULL,
  `executorNums` int(11) NOT NULL,
  `requestMemory` decimal(1,1) NOT NULL,
  `avg_memory_used` decimal(18,10) DEFAULT NULL,
  `min_memory_used` decimal(18,10) DEFAULT NULL,
  `max_memory_used` decimal(18,10) DEFAULT NULL,
  `request_cpu` decimal(1,1) NOT NULL,
  `avg_cpu_used` decimal(18,10) DEFAULT NULL,
  `min_cpu_used` decimal(18,10) DEFAULT NULL,
  `max_cpu_used` decimal(18,10) DEFAULT NULL,
  `entry` text,
  `run_count` int(11) DEFAULT NULL,
  `max_cpu_gap` decimal(18,10) DEFAULT NULL,
  `max_memory_gap` decimal(18,10) DEFAULT NULL,
  `last_job_id` text,
  `last_avg_memory_used` decimal(18,10) DEFAULT NULL,
  `last_min_memory_used` decimal(18,10) DEFAULT NULL,
  `last_max_memory_used` decimal(18,10) DEFAULT NULL,
  `last_avg_cpu_used` decimal(18,10) DEFAULT NULL,
  `last_min_cpu_used` decimal(18,10) DEFAULT NULL,
  `last_max_cpu_used` decimal(18,10) DEFAULT NULL,
  `job_name_quantity` decimal(18,2) DEFAULT NULL,
  `job_id_quantity` decimal(18,2) DEFAULT NULL,
  `job_name_total_price` decimal(18,2) DEFAULT NULL,
  `job_id_total_price` decimal(18,2) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `engine` text,
  `avg_instances_num` double DEFAULT NULL,
  `min_instances_num` int(11) DEFAULT NULL,
  `max_instances_num` int(11) DEFAULT NULL,
  `score_rank` double DEFAULT NULL,
  `last_7day_cpu_avg_used` decimal(18,10) DEFAULT NULL,
  `last_7day_cpu_max_used` decimal(18,10) DEFAULT NULL,
  `last_7day_cpu_min_used` decimal(18,10) DEFAULT NULL,
  `last_7day_memory_avg_used` decimal(18,10) DEFAULT NULL,
  `last_7day_memory_max_used` decimal(18,10) DEFAULT NULL,
  `last_7day_memory_min_used` decimal(18,10) DEFAULT NULL,
  `last_7day_avg_cost` decimal(18,2) DEFAULT NULL,
  KEY `job_name` (`job_name`) USING BTREE,
  KEY `gov_spark_cost_dt_index` (`dt`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_spark_cost_flag'
CREATE TABLE  IF NOT EXISTS `gov_spark_cost_flag` (
  `dt` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'gov_spark_recommend_v2'
CREATE TABLE  IF NOT EXISTS `gov_spark_recommend_v2` (
  `dt` varchar(200) NOT NULL COMMENT '数据同步日期；格式yyyy-MM-dd',
  `job_name` varchar(200) DEFAULT NULL COMMENT '任务名称',
  `job_id` varchar(200) DEFAULT NULL,
  `recommend_cpu` varchar(20) DEFAULT NULL COMMENT '推荐cpu大小',
  `recommend_instances` int(10) DEFAULT NULL COMMENT '推荐instance大小 ',
  `healthy_ratio` float(12,0) DEFAULT NULL COMMENT '任务健康度 ',
  `complex_ratio` float(12,0) DEFAULT NULL COMMENT '任务复杂度 ',
  `stability_ratio` float(12,0) DEFAULT NULL COMMENT '任务复杂度 ',
  `is_recommend` int(10) DEFAULT NULL COMMENT '0 是, 1:否',
  `create_time` timestamp(6) NULL DEFAULT NULL,
  `recommend_memory` varchar(20) DEFAULT NULL COMMENT '推荐内存大小',
  `recommend_type` varchar(20) DEFAULT NULL,
  `owner` varchar(20) DEFAULT NULL COMMENT '用户'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'governance_tags'
CREATE TABLE  IF NOT EXISTS `governance_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录自增ID',
  `tag_name` varchar(60) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '标签名称',
  `tag_value` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '标签值',
  `tag_type` tinyint(1) NOT NULL COMMENT '标签类型',
  `object_id` varchar(60) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '标签所属对象ID',
  `object_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '标签所属对象名称',
  `status` tinyint(1) DEFAULT '1' COMMENT '标签状态：0启用， 1废弃',
  `tag_comment` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '标签备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`object_name`,`tag_name`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `object_name` (`object_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1608327809916103050 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'instance'
CREATE TABLE  IF NOT EXISTS `instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `provider` varchar(25) COLLATE utf8mb4_bin DEFAULT NULL,
  `region` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `group` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `cpu` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `memory` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `description` text COLLATE utf8mb4_bin,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'job_shuffle'
CREATE TABLE  IF NOT EXISTS `job_shuffle` (
  `dt` varchar(20) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `shuffle` varchar(20) DEFAULT NULL,
  `url` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'last_activity_info'
CREATE TABLE  IF NOT EXISTS `last_activity_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(120) COLLATE utf8mb4_bin NOT NULL COMMENT '表名',
  `user_id` varchar(250) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `sum_count` int(20) DEFAULT NULL COMMENT '该用户访问该表的总次数',
  `avg_count` int(20) DEFAULT NULL COMMENT '该用户访问该表的平均次数',
  `recently_visited_timestamp` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '最近一次访问时间戳',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `db_name` varchar(120) COLLATE utf8mb4_bin NOT NULL COMMENT '库名',
  `region` varchar(120) COLLATE utf8mb4_bin NOT NULL COMMENT '区域',
  `table_id` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  `table_bucket_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `provider` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`table_name`,`db_name`,`region`,`user_id`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `rdt` (`region`,`db_name`,`table_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5774689 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'lock_info'
CREATE TABLE  IF NOT EXISTS `lock_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag` varchar(256) NOT NULL COMMENT '锁唯一标签',
  `hostname` varchar(255) DEFAULT '' COMMENT '持有锁的主机名',
  `expirationTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '锁状态',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tag_status` (`tag`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'owner_not_default_bucket_record'
CREATE TABLE  IF NOT EXISTS `owner_not_default_bucket_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `department` varchar(20) COLLATE utf8mb4_bin DEFAULT '' COMMENT '部门',
  `owner` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户',
  `region` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '区域',
  `bucket_name` varchar(20) COLLATE utf8mb4_bin DEFAULT '' COMMENT '桶',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-有效1-无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;





-- Create syntax for TABLE 'permission_record_info'
CREATE TABLE  IF NOT EXISTS `permission_record_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '工单id',
  `table_list` text COLLATE utf8mb4_bin COMMENT '申请列表',
  `permission` text COLLATE utf8mb4_bin COMMENT '权限类型',
  `apply_user` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '授予用户',
  `flag` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '是否钉钉通知',
  `grant_type` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '授予类型 个人或者角色',
  `status` tinyint(1) DEFAULT '0' COMMENT '0-有效1-无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `grant_user` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '授予用户或角色',
  `type` varchar(512) COLLATE utf8mb4_bin DEFAULT '' COMMENT '工单类型',
  PRIMARY KEY (`id`),
  KEY `permission_record_info_apply_user_index` (`apply_user`),
  KEY `permission_record_info_grant_user_index` (`grant_user`)
) ENGINE=InnoDB AUTO_INCREMENT=1350 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'report'
CREATE TABLE  IF NOT EXISTS `report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `top_params` text COLLATE utf8mb4_bin,
  `query_group` int(5) DEFAULT NULL,
  `side_filter` text COLLATE utf8mb4_bin,
  `filter_str` text COLLATE utf8mb4_bin,
  `query_group_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `dt_type` int(2) DEFAULT NULL,
  `time_range` int(2) DEFAULT NULL,
  `create_time` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_user` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `update_time` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `update_user` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'report_history'
CREATE TABLE  IF NOT EXISTS `report_history` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT,
  `report_id` bigint(20) DEFAULT NULL,
  `report_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_user` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `visit_time` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'role_owner_relevance'
CREATE TABLE  IF NOT EXISTS `role_owner_relevance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` varchar(220) COLLATE utf8mb4_bin NOT NULL COMMENT 'shareit',
  `role_name` varchar(520) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色名',
  `role_id` varchar(520) COLLATE utf8mb4_bin NOT NULL COMMENT '库名',
  `comment` varchar(520) COLLATE utf8mb4_bin DEFAULT '' COMMENT '描述',
  `created_time` varchar(520) COLLATE utf8mb4_bin DEFAULT NULL,
  `user_name` varchar(220) COLLATE utf8mb4_bin NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`,`user_name`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `rid` (`role_id`,`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=386820 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'role_table_relevance'
CREATE TABLE  IF NOT EXISTS `role_table_relevance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` varchar(220) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色id',
  `role_name` varchar(520) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色名',
  `privilege` varchar(220) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限',
  `granted_on` varchar(520) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'table or db',
  `name` varchar(220) COLLATE utf8mb4_bin NOT NULL DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`,`name`,`privilege`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `rid` (`role_id`,`name`,`privilege`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=185157752 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'shedlock'
CREATE TABLE  IF NOT EXISTS `shedlock` (
  `name` varchar(64) NOT NULL,
  `lock_until` timestamp(3) NULL DEFAULT NULL,
  `locked_at` timestamp(3) NULL DEFAULT NULL,
  `locked_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='shedlock分布式定时任务表';

-- Create syntax for TABLE 'small_file_job'
CREATE TABLE  IF NOT EXISTS `small_file_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region` varchar(50) DEFAULT NULL,
  `db_name` varchar(200) DEFAULT NULL,
  `bucket_name` varchar(200) DEFAULT NULL,
  `table_id` varchar(50) DEFAULT NULL,
  `table_name` varchar(200) DEFAULT NULL,
  `location` text,
  `task_name` varchar(255) DEFAULT NULL,
  `task_id` bigint(20) DEFAULT NULL,
  `create_time` varchar(20) DEFAULT NULL,
  `start_job_time` varchar(20) DEFAULT NULL,
  `stop_job_time` varchar(20) DEFAULT NULL,
  `cloud_type` varchar(20) DEFAULT NULL,
  `shareit_id` varchar(50) DEFAULT NULL,
  `status` int(2) unsigned DEFAULT '0',
  `has_task` bit(1) DEFAULT NULL,
  `owner` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='小文件任务表';

-- Create syntax for TABLE 'small_file_job_history'
CREATE TABLE  IF NOT EXISTS `small_file_job_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region` varchar(50) DEFAULT NULL,
  `db_name` varchar(200) DEFAULT NULL,
  `bucket_name` varchar(200) DEFAULT NULL,
  `table_id` varchar(50) DEFAULT NULL,
  `table_name` varchar(200) DEFAULT NULL,
  `location` text,
  `task_name` varchar(255) DEFAULT NULL,
  `task_id` bigint(20) DEFAULT NULL,
  `create_time` varchar(20) DEFAULT NULL,
  `start_job_time` varchar(20) DEFAULT NULL,
  `stop_job_time` varchar(20) DEFAULT NULL,
  `cloud_type` varchar(20) DEFAULT NULL,
  `shareit_id` varchar(50) DEFAULT NULL,
  `before_obj_num` bigint(20) DEFAULT NULL,
  `after_obj_num` bigint(20) DEFAULT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `owner` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1582693542611931138 DEFAULT CHARSET=utf8 COMMENT='小文件任务表历史表';

-- Create syntax for TABLE 'spark_job_metric'
CREATE TABLE  IF NOT EXISTS `spark_job_metric` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `started` varchar(255) DEFAULT NULL,
  `finished` varchar(255) DEFAULT NULL,
  `task_total_time` double DEFAULT '0',
  `history_url` varchar(255) DEFAULT NULL,
  `spark_job_id` varchar(255) DEFAULT NULL,
  `cluster_name` varchar(100) DEFAULT NULL,
  `command_name` varchar(100) DEFAULT NULL,
  `task_total_input_bytes` double DEFAULT NULL,
  `task_total_shuffle_read` double DEFAULT NULL,
  `task_total_shuffle_write` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'spark_metric'
CREATE TABLE  IF NOT EXISTS `spark_metric` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `started` varchar(255) DEFAULT NULL,
  `finished` varchar(255) DEFAULT NULL,
  `taskTotalTime` double DEFAULT '0',
  `historyUrl` varchar(255) DEFAULT NULL,
  `sparkJobId` varchar(255) DEFAULT NULL,
  `cluster_name` varchar(100) DEFAULT NULL,
  `command_name` varchar(100) DEFAULT NULL,
  `taskTotalInputBytes` double DEFAULT NULL COMMENT '单位G',
  `taskTotalShuffleRead` double DEFAULT NULL COMMENT '单位G',
  `taskTotalShuffleWrite` double DEFAULT NULL COMMENT '单位G',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'spark_metric_all_command'
CREATE TABLE  IF NOT EXISTS `spark_metric_all_command` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `started` varchar(255) DEFAULT NULL,
  `finished` varchar(255) DEFAULT NULL,
  `taskTotalTime` double DEFAULT '0',
  `historyUrl` varchar(255) DEFAULT NULL,
  `sparkJobId` varchar(255) DEFAULT NULL,
  `cluster_name` varchar(100) DEFAULT NULL,
  `command_name` varchar(100) DEFAULT NULL,
  `taskTotalInputBytes` double DEFAULT NULL COMMENT '单位G',
  `taskTotalShuffleRead` double DEFAULT NULL COMMENT '单位G',
  `taskTotalShuffleWrite` double DEFAULT NULL COMMENT '单位G',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create syntax for TABLE 'table_blood_info'
CREATE TABLE  IF NOT EXISTS `table_blood_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建区域',
  `db_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '库',
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表名',
  `blood_str` text COLLATE utf8mb4_bin COMMENT '血缘json',
  `owners` text COLLATE utf8mb4_bin COMMENT 'owner json',
  `task_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务id',
  `task_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务名称',
  `exception` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=91077 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_change_record'
CREATE TABLE  IF NOT EXISTS `table_change_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(220) COLLATE utf8mb4_bin DEFAULT '' COMMENT '表名',
  `db_name` varchar(520) COLLATE utf8mb4_bin DEFAULT '' COMMENT '库名',
  `region` varchar(520) COLLATE utf8mb4_bin DEFAULT '' COMMENT '区域',
  `operation` varchar(520) COLLATE utf8mb4_bin DEFAULT '' COMMENT '操作',
  `created_time` varchar(520) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '操作时间',
  `operation_user` varchar(220) COLLATE utf8mb4_bin DEFAULT '' COMMENT '操作人',
  `inform_list` varchar(220) COLLATE utf8mb4_bin DEFAULT '' COMMENT '通知上下游用户集',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `message` text COLLATE utf8mb4_bin COMMENT '提醒信息',
  `args` text COLLATE utf8mb4_bin COMMENT '回调参数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11471 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_data_info'
CREATE TABLE  IF NOT EXISTS `table_data_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '执行表名',
  `region` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '执行区域',
  `sql_text` varchar(209) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '执行sql',
  `data` longtext COLLATE utf8mb4_bin,
  `size` int(20) DEFAULT NULL COMMENT '查询条数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sql_text`,`region`),
  UNIQUE KEY `id_unique` (`id`),
  KEY `rdt` (`sql_text`,`region`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1999 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_info'
CREATE TABLE  IF NOT EXISTS `table_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_type` tinyint(1) DEFAULT NULL COMMENT '0 模版模式  1 sql模式',
  `region` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建区域',
  `type` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '数据源类型',
  `db_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '库',
  `subject` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '主题',
  `update_type` varchar(8) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表类型',
  `interval` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新频次',
  `cn_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '中文名',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `partition_type` tinyint(1) DEFAULT NULL COMMENT '1 分区表 0 非分区表',
  `lifecycle` int(1) DEFAULT NULL COMMENT '生命周期',
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '表名',
  `columns` longtext COLLATE utf8mb4_bin,
  `partition_keys` text COLLATE utf8mb4_bin,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(1) DEFAULT NULL COMMENT '0-有效1-无效',
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `hierarchical` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分层',
  `location` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '存储路径',
  `application` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '应用',
  `num_rows` int(20) DEFAULT NULL COMMENT '行数',
  `byte_size` int(20) DEFAULT NULL COMMENT '存储',
  `sd_file_format` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `last_activity_count` int(11) DEFAULT NULL COMMENT '最近三十天访问次数',
  `transient_lastDdlTime` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  FULLTEXT KEY `owner` (`owner`,`name`,`cn_name`,`description`)
) ENGINE=InnoDB AUTO_INCREMENT=89156 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_info_search_history'
CREATE TABLE  IF NOT EXISTS `table_info_search_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户Id',
  `table_info_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `table_info_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13614 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_info_user_input'
CREATE TABLE  IF NOT EXISTS `table_info_user_input` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户Id',
  `input` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(1) DEFAULT '0' COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33940 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_storage_info'
CREATE TABLE  IF NOT EXISTS `table_storage_info` (
  `dt` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `id` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  `table_name` varchar(250) COLLATE utf8mb4_bin DEFAULT NULL,
  `db_name` varchar(120) COLLATE utf8mb4_bin DEFAULT NULL,
  `table_object_num` mediumtext COLLATE utf8mb4_bin,
  `table_small_object_num` mediumtext COLLATE utf8mb4_bin,
  `table_partition_num` mediumtext COLLATE utf8mb4_bin,
  `table_bucket_name` longtext COLLATE utf8mb4_bin,
  `total_storage` mediumtext COLLATE utf8mb4_bin,
  `storage_type` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  `storage_file_format` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL,
  `location` longtext COLLATE utf8mb4_bin,
  `small_file_advice` longtext COLLATE utf8mb4_bin,
  `small_file_workbench_url` longtext COLLATE utf8mb4_bin,
  `region` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `provider` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `owner` longtext COLLATE utf8mb4_bin,
  `table_standard_size` mediumtext COLLATE utf8mb4_bin,
  `table_intelligent_size` mediumtext COLLATE utf8mb4_bin,
  `table_deep_size` mediumtext COLLATE utf8mb4_bin,
  `table_archive_size` mediumtext COLLATE utf8mb4_bin,
  `tenant_id` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `tenant_name` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  KEY `dtid` (`dt`,`id`),
  KEY `rdt` (`region`,`db_name`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'table_visit'
CREATE TABLE  IF NOT EXISTS `table_visit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region` varchar(255) DEFAULT NULL,
  `db_name` varchar(200) DEFAULT NULL,
  `bucket_name` varchar(200) DEFAULT NULL,
  `table_id` varchar(50) DEFAULT NULL,
  `table_name` varchar(200) DEFAULT NULL,
  `location` text,
  `dt` varchar(20) DEFAULT NULL,
  `visit` varchar(255) DEFAULT NULL,
  `cloud_type` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表访问';

-- Create syntax for TABLE 'task'
CREATE TABLE  IF NOT EXISTS `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) DEFAULT NULL,
  `task_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `group` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_by` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `update_time` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81603 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'task_business'
CREATE TABLE  IF NOT EXISTS `task_business` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `group` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `business_group` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `access_group_id` bigint(20) DEFAULT NULL,
  `ratio` decimal(10,5) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=398384 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Create syntax for TABLE 'task_tag'
CREATE TABLE  IF NOT EXISTS `task_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `task_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `tag` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_user` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=267434 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE  IF NOT EXISTS `billing_owner_pu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `department` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `pu` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `pu_id` bigint(20),
  `ratio` decimal(10,3) NOT NULL DEFAULT '1.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

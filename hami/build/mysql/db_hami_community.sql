/*
 Navicat Premium Data Transfer

 Source Server         : hami-mysql-local
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 172.21.0.1:8300
 Source Schema         : db_hami_community

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 15/01/2024 22:41:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS db_hami_community;
CREATE DATABASE db_hami_community;
use db_hami_community;
/*
 Navicat Premium Data Transfer

 Source Server         : hami-mysql-local
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 172.21.0.1:8300
 Source Schema         : db_hami_community

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 18/03/2024 20:02:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`
(
    `id`       int                                                           NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '用户名',
    `email`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '邮箱',
    `role`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT 'user' COMMENT '角色',
    `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '密码',
    `state`    tinyint                                                       NOT NULL DEFAULT 0 COMMENT ' 状态 0-未激活 1-激活',
    `deleted`  tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    `ctime`    timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `mtime`    timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_email` (`email` ASC) USING BTREE,
    UNIQUE INDEX `uk_username` (`username` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账号表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`
(
    `id`          int                                                           NOT NULL AUTO_INCREMENT COMMENT '文章id',
    `user_id`     int                                                           NOT NULL COMMENT '作者id',
    `category_id` int                                                           NOT NULL COMMENT '分类id',
    `tag_ids`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '文章标签列表',
    `title`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
    `summary`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章简介',
    `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '文章内容',
    `picture`     varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文章封面',
    `deleted`     tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    `ctime`       timestamp(3)                                                  NOT NULL DEFAULT (now(3)) COMMENT '创建时间',
    `mtime`       timestamp(3)                                                  NOT NULL DEFAULT (now(3)) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_category_ctime` (`category_id` ASC, `deleted` ASC, `ctime` ASC) USING BTREE,
    INDEX `idx_user_id_ctime` (`user_id` ASC, `deleted` ASC, `ctime` ASC) USING BTREE,
    INDEX `idx_article_time` (`deleted` ASC, `ctime` ASC, `id` ASC) USING BTREE,
    FULLTEXT INDEX `idx_search_title` (`title`) WITH PARSER `ngram`,
    FULLTEXT INDEX `idx_search_summary` (`summary`) WITH PARSER `ngram`
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_collect
-- ----------------------------
DROP TABLE IF EXISTS `article_collect`;
CREATE TABLE `article_collect`
(
    `id`         int          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fid`        int          NULL     DEFAULT NULL COMMENT '收藏夹ID (备用/先不搞收藏夹)',
    `user_id`    int          NOT NULL COMMENT '用户ID',
    `article_id` int          NOT NULL COMMENT '文章ID',
    `state`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态',
    `ctime`      timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`      timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_id_article_id` (`user_id` ASC, `article_id` ASC) USING BTREE,
    INDEX `idx_user_id_mtime` (`user_id` ASC, `state` ASC, `mtime` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章收藏表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_draft
-- ----------------------------
DROP TABLE IF EXISTS `article_draft`;
CREATE TABLE `article_draft`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID,草稿ID',
    `user_id`     int                                                           NOT NULL COMMENT '用户ID',
    `article_id`  int                                                           NULL     DEFAULT NULL COMMENT '文章ID',
    `category_id` int                                                           NULL     DEFAULT NULL COMMENT '分类ID',
    `tag_ids`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '文章标签',
    `title`       varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '标题',
    `summary`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '文章简介',
    `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '文章内容',
    `picture`     varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '文章图片地址',
    `state`       tinyint                                                       NOT NULL DEFAULT 0 COMMENT '草稿状态 0-未发表 1-已发表',
    `version`     bigint                                                        NOT NULL DEFAULT 0 COMMENT '版本号',
    `deleted`     tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
    `ctime`       timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`       timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_article_draft` (`article_id` ASC) USING BTREE,
    INDEX `idx_user_id_mtime` (`user_id` ASC, `state` ASC, `mtime` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章草稿表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_stat
-- ----------------------------
DROP TABLE IF EXISTS `article_stat`;
CREATE TABLE `article_stat`
(
    `article_id` int                                                                                    NOT NULL COMMENT '文章ID',
    `user_id`    int                                                                                    NOT NULL COMMENT '用户ID',
    `views`      int                                                                                    NOT NULL DEFAULT 0 COMMENT '阅读量',
    `likes`      int                                                                                    NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comments`   int                                                                                    NOT NULL DEFAULT 0 COMMENT '评论数',
    `collects`   int                                                                                    NOT NULL DEFAULT 0 COMMENT '收藏数',
    `deleted`    tinyint                                                                                NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
    `ctime`      timestamp(3)                                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`      timestamp(3)                                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `hot_rank`   decimal(10, 3) GENERATED ALWAYS AS ((
        (((`likes` * 2.326) + (`comments` * 1.162)) + (`collects` * 6.673)) + (`views` * 0.33))) STORED NOT NULL,
    PRIMARY KEY (`article_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id` ASC, `deleted` ASC) USING BTREE,
    INDEX `idx_hot_index` (`deleted` ASC, `ctime` ASC, `hot_rank` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章数据记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bulletin
-- ----------------------------
DROP TABLE IF EXISTS `bulletin`;
CREATE TABLE `bulletin`
(
    `id`      int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '内容',
    `ctime`   datetime(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`   datetime(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted` tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_ctime` (`deleted` ASC, `ctime` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`      int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '类目名称',
    `path`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '路径',
    `deleted` int                                                           NOT NULL DEFAULT 0 COMMENT '是否删除',
    `ctime`   timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`   timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 10008
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`         int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` int                                                           NOT NULL DEFAULT 0 COMMENT '文章ID',
    `user_id`    int                                                           NOT NULL DEFAULT 0 COMMENT '用户ID',
    `root_id`    int                                                           NOT NULL DEFAULT 0 COMMENT '顶级评论ID 0-表示是根评论',
    `parent_id`  int                                                           NOT NULL DEFAULT 0 COMMENT '父评论ID',
    `reply_to`   int                                                           NOT NULL DEFAULT 0 COMMENT '回复的用户ID',
    `ip_info`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '评论时的IP信息',
    `content`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '评论内容',
    `pictures`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '评论图片',
    `likes`      int                                                           NOT NULL DEFAULT 0 COMMENT '点赞数',
    `deleted`    tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除',
    `ctime`      timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`      timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_root_id` (`root_id` ASC) USING BTREE,
    INDEX `idx_ctime` (`article_id` ASC, `root_id` ASC, `deleted` ASC, `ctime` ASC) USING BTREE,
    INDEX `idx_likes` (`article_id` ASC, `root_id` ASC, `deleted` ASC, `likes` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file_detail
-- ----------------------------
DROP TABLE IF EXISTS `file_detail`;
CREATE TABLE `file_detail`
(
    `id`                varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '文件id',
    `url`               varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件访问地址',
    `size`              bigint                                                        NULL DEFAULT NULL COMMENT '文件大小，单位字节',
    `filename`          varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件名称',
    `original_filename` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '原始文件名',
    `base_path`         varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '基础存储路径',
    `path`              varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储路径',
    `ext`               varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '文件扩展名',
    `content_type`      varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'MIME类型',
    `platform`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '存储平台',
    `object_id`         varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '文件所属对象id',
    `object_type`       varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
    `create_time`       datetime(3)                                                   NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件上传记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for login_record
-- ----------------------------
DROP TABLE IF EXISTS `login_record`;
CREATE TABLE `login_record`
(
    `id`         bigint                                                NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    int                                                   NOT NULL COMMENT '用户ID',
    `deleted`    tinyint                                               NOT NULL DEFAULT 0 COMMENT '是否删除',
    `login_time` bigint                                                NOT NULL COMMENT '登录时间',
    `ip_info`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '登录的IP地址信息',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id` ASC, `deleted` ASC, `login_time` ASC) USING BTREE COMMENT '用户ID索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notify_msg
-- ----------------------------
DROP TABLE IF EXISTS `notify_msg`;
CREATE TABLE `notify_msg`
(
    `id`         int                                                            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `related_id` int                                                            NOT NULL DEFAULT 0 COMMENT '关联的主键',
    `item_id`    int                                                            NOT NULL DEFAULT -1 COMMENT '备用 ',
    `sender`     int                                                            NOT NULL DEFAULT 0 COMMENT '源用户ID(发送通知)',
    `receiver`   int                                                            NOT NULL DEFAULT 0 COMMENT '目标用户ID(接收通知)',
    `type`       int                                                            NOT NULL DEFAULT 0 COMMENT '类型: 0-系统，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-关注的用户发布新文章',
    `detail`     varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '消息内容',
    `state`      tinyint                                                        NOT NULL DEFAULT 0 COMMENT '阅读状态: 0-未读，1-已读',
    `ctime`      timestamp(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`      timestamp(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_notify_msg` (`item_id` ASC, `sender` ASC, `receiver` ASC, `type` ASC) USING BTREE,
    INDEX `idx_receiver_type` (`receiver` ASC, `state` ASC, `type` ASC, `ctime` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知列表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for reading_record
-- ----------------------------
DROP TABLE IF EXISTS `reading_record`;
CREATE TABLE `reading_record`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      int          NOT NULL COMMENT '作者ID',
    `article_id`   int          NOT NULL COMMENT '文章ID',
    `reading_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '阅读时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_article_id` (`user_id` ASC, `article_id` ASC) USING BTREE,
    INDEX `idx_reading_time` (`user_id` ASC, `reading_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '阅读记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`
(
    `id`          int                                                          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
    `type`        int                                                          NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-自定义标签',
    `category_id` int                                                          NOT NULL DEFAULT 0 COMMENT '类目ID',
    `deleted`     tinyint                                                      NOT NULL DEFAULT 0 COMMENT '是否删除',
    `ctime`       timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`       timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_tag_category` (`name` ASC, `category_id` ASC) USING BTREE,
    INDEX `idx_category_id` (`category_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签管理表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_like
-- ----------------------------
DROP TABLE IF EXISTS `tb_like`;
CREATE TABLE `tb_like`
(
    `id`        int          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `liker_id`  int          NOT NULL COMMENT '点赞人ID',
    `item_id`   int          NOT NULL COMMENT '实体ID -文章/评论',
    `item_type` tinyint      NOT NULL DEFAULT 1 COMMENT '实体类型 1-文章 2-评论',
    `state`     tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0-未点赞 1-点赞',
    `ctime`     timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`     timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_like_item` (`liker_id` ASC, `item_id` ASC, `item_type` ASC) USING BTREE,
    INDEX `idx_liker_id_type` (`liker_id` ASC, `item_type` ASC, `state` ASC) USING BTREE,
    INDEX `idx_mtime` (`liker_id` ASC, `item_type` ASC, `state` ASC, `mtime` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞通用表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `user_id`  int                                                           NOT NULL COMMENT '用户账号ID',
    `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '用户名',
    `avatar`   varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '头像',
    `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '职位',
    `company`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '公司',
    `profile`  varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '' COMMENT '个人简介',
    `blog`     varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '个人主页',
    `tag`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '标签',
    `deleted`  tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
    `ctime`    timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`    timestamp(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`
(
    `id`        int          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`   int          NOT NULL COMMENT '用户ID',
    `following` int          NOT NULL COMMENT '关注的用户ID',
    `state`     tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0-未关注 1关注',
    `ctime`     timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `mtime`     timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_follow` (`user_id` ASC, `following` ASC) USING BTREE,
    INDEX `idx_following_item` (`user_id` ASC, `state` ASC, `mtime` ASC, `following` ASC) USING BTREE,
    INDEX `idx_user_state` (`user_id` ASC, `state` ASC) USING BTREE,
    INDEX `idx_follower_item` (`following` ASC, `state` ASC, `mtime` ASC, `user_id` ASC) USING BTREE,
    INDEX `idx_follow_state` (`following` ASC, `state` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_stat
-- ----------------------------
DROP TABLE IF EXISTS `user_stat`;
CREATE TABLE `user_stat`
(
    `user_id`          int                                       NOT NULL COMMENT '用户ID',
    `total_followings` int                                       NOT NULL DEFAULT 0 COMMENT '总关注数',
    `total_articles`   int                                       NOT NULL DEFAULT 0 COMMENT '总文章数',
    `total_views`      int                                       NOT NULL DEFAULT 0 COMMENT '总阅读量',
    `total_likes`      int                                       NOT NULL DEFAULT 0 COMMENT '总获赞数',
    `total_comments`   int                                       NOT NULL DEFAULT 0 COMMENT '收到的总评论数',
    `total_collects`   int                                       NOT NULL DEFAULT 0 COMMENT '文章被总收藏数',
    `total_followers`  int                                       NOT NULL DEFAULT 0 COMMENT '总粉丝数',
    `deleted`          tinyint                                   NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
    `hot_index`        decimal(10, 3) GENERATED ALWAYS AS ((
        (((((`total_articles` * 0.416) + (`total_views` * 0.112)) + (`total_likes` * 1.823)) +
          (`total_collects` * 3.643)) + (`total_comments` * 0.336)) +
        (`total_followers` * 1.322))) STORED COMMENT 'hot_index' NOT NULL,
    PRIMARY KEY (`user_id`) USING BTREE,
    INDEX `idx_hot_index` (`deleted` ASC, `hot_index` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

INSERT INTO `bulletin` VALUES (1, '更新日志-0.0.1', '- 完成大部分功能, 内测中\r\n- 增加公告功能\r\n- 增加夜间星空特效\r\n- 优化UI', '2024-02-24 16:25:25.718', '2024-03-16 18:33:33.306', 0);


INSERT INTO `category` VALUES (10000, '后端', 'backend', 0, '2023-03-10 12:04:11.000', '2023-03-21 20:12:19.000');
INSERT INTO `category` VALUES (10001, '前端', 'frontend', 0, '2023-03-10 12:04:31.000', '2023-03-21 20:12:21.000');
INSERT INTO `category` VALUES (10002, 'Android', 'android', 0, '2023-03-10 12:04:49.000', '2023-04-26 00:22:05.000');
INSERT INTO `category` VALUES (10003, 'IOS', 'ios', 0, '2023-03-10 12:05:22.000', '2023-03-21 20:12:26.000');
INSERT INTO `category` VALUES (10004, '人工智能', 'ai', 0, '2023-03-10 12:05:40.000', '2023-03-21 20:12:28.000');
INSERT INTO `category` VALUES (10005, '开发工具', 'tool', 0, '2023-03-10 12:06:01.000', '2023-03-21 20:13:20.000');
INSERT INTO `category` VALUES (10006, '代码人生', 'coding', 0, '2023-03-10 12:06:49.000', '2023-03-21 20:12:36.000');
INSERT INTO `category` VALUES (10007, '阅读', 'reading', 0, '2023-03-10 12:07:11.000', '2023-03-21 20:12:39.000');


INSERT INTO `tag` VALUES (1000, '后端', 1, 10000, 0, '2023-03-21 19:41:18.000', '2023-03-21 19:41:18.000');
INSERT INTO `tag` VALUES (1001, 'Java', 1, 10000, 0, '2023-03-21 19:41:29.000', '2023-03-21 19:45:15.000');
INSERT INTO `tag` VALUES (1002, 'Go', 1, 10000, 0, '2023-03-21 19:41:37.000', '2023-03-21 19:45:18.000');
INSERT INTO `tag` VALUES (1003, '大数据', 1, 10000, 0, '2023-03-21 19:42:01.000', '2023-03-21 19:45:20.000');
INSERT INTO `tag` VALUES (1004, 'Spring', 1, 10000, 0, '2023-03-21 19:42:09.000', '2023-03-21 19:45:22.000');
INSERT INTO `tag` VALUES (1005, 'SpringBoot', 1, 10000, 0, '2023-03-21 19:42:16.000', '2023-03-21 19:45:23.000');
INSERT INTO `tag` VALUES (1006, 'Mybatis', 1, 10000, 0, '2023-03-21 19:42:21.000', '2023-03-21 19:45:24.000');
INSERT INTO `tag` VALUES (1007, 'SpringMVC', 1, 10000, 0, '2023-03-21 19:42:29.000', '2023-03-21 19:45:27.000');
INSERT INTO `tag` VALUES (1008, 'Kubernetes', 1, 10000, 0, '2023-03-21 19:43:25.000', '2023-03-21 19:45:28.000');
INSERT INTO `tag` VALUES (1009, 'Python', 1, 10000, 0, '2023-03-21 19:43:38.000', '2023-03-21 19:45:29.000');
INSERT INTO `tag` VALUES (1010, 'ElasticSearch', 1, 10000, 0, '2023-03-21 19:44:59.000', '2023-03-21 19:45:31.000');
INSERT INTO `tag` VALUES (1011, '前端', 1, 10001, 0, '2023-03-21 19:45:55.000', '2023-03-21 19:47:12.000');
INSERT INTO `tag` VALUES (1012, 'JavaScript', 1, 10001, 0, '2023-03-21 19:45:59.000', '2023-03-21 19:47:13.000');
INSERT INTO `tag` VALUES (1013, 'Vue.js', 1, 10001, 0, '2023-03-21 19:46:06.000', '2023-03-21 19:47:13.000');
INSERT INTO `tag` VALUES (1014, 'React.js', 1, 10001, 0, '2023-03-21 19:46:22.000', '2023-03-21 19:47:14.000');
INSERT INTO `tag` VALUES (1015, 'CSS', 1, 10001, 0, '2023-03-21 19:46:25.000', '2023-03-21 19:47:15.000');
INSERT INTO `tag` VALUES (1016, 'TypeScript', 1, 10001, 0, '2023-03-21 19:46:29.000', '2023-03-21 19:47:16.000');
INSERT INTO `tag` VALUES (1017, 'Node.js', 1, 10001, 0, '2023-03-21 19:46:38.000', '2023-03-21 19:47:16.000');
INSERT INTO `tag` VALUES (1018, 'Webpack', 1, 10001, 0, '2023-03-21 19:47:03.000', '2023-03-21 19:47:18.000');
INSERT INTO `tag` VALUES (1019, 'Android', 1, 10002, 0, '2023-03-21 19:47:37.000', '2023-03-21 19:48:37.000');
INSERT INTO `tag` VALUES (1020, 'Kotlin', 1, 10002, 0, '2023-03-21 19:48:10.000', '2023-03-21 19:48:43.000');
INSERT INTO `tag` VALUES (1021, 'Android Jetpack', 1, 10002, 0, '2023-03-21 19:48:21.000', '2023-03-21 19:48:55.000');
INSERT INTO `tag` VALUES (1022, 'IOS', 1, 10003, 0, '2023-03-21 19:49:00.000', '2023-03-21 19:50:00.000');
INSERT INTO `tag` VALUES (1023, 'Swift', 1, 10003, 0, '2023-03-21 19:49:14.000', '2023-03-21 19:50:02.000');
INSERT INTO `tag` VALUES (1024, 'Object-C', 1, 10003, 0, '2023-03-21 19:49:18.000', '2023-03-21 19:50:03.000');
INSERT INTO `tag` VALUES (1025, 'SwiftUI', 1, 10003, 0, '2023-03-21 19:49:38.000', '2023-03-21 19:50:05.000');
INSERT INTO `tag` VALUES (1026, 'Xcode', 1, 10003, 0, '2023-03-21 19:49:42.000', '2023-03-21 19:50:06.000');
INSERT INTO `tag` VALUES (1027, 'MacOs', 1, 10003, 0, '2023-03-21 19:49:48.000', '2023-03-21 19:50:18.000');
INSERT INTO `tag` VALUES (1028, 'ChatGPT', 1, 10004, 0, '2023-03-21 19:50:25.000', '2023-03-21 19:51:16.000');
INSERT INTO `tag` VALUES (1029, 'GPT', 1, 10004, 0, '2023-03-21 19:50:28.000', '2023-03-21 19:51:18.000');
INSERT INTO `tag` VALUES (1030, '深度学习', 1, 10004, 0, '2023-03-21 19:50:41.000', '2023-03-21 19:51:19.000');
INSERT INTO `tag` VALUES (1031, '机器学习', 1, 10004, 0, '2023-03-21 19:50:50.000', '2023-03-21 19:51:20.000');
INSERT INTO `tag` VALUES (1032, '计算机视觉', 1, 10004, 0, '2023-03-21 19:51:00.000', '2023-03-21 19:51:21.000');
INSERT INTO `tag` VALUES (1033, 'OpenAI', 1, 10004, 0, '2023-03-21 19:51:11.000', '2023-03-21 19:51:48.000');
INSERT INTO `tag` VALUES (1034, 'Git', 1, 10005, 0, '2023-03-21 19:51:57.000', '2023-03-21 19:54:14.000');
INSERT INTO `tag` VALUES (1035, 'GitHub', 1, 10005, 0, '2023-03-21 19:52:20.000', '2023-03-21 19:54:17.000');
INSERT INTO `tag` VALUES (1036, 'IDEA', 1, 10005, 0, '2023-03-21 19:52:30.000', '2023-03-21 19:54:20.000');
INSERT INTO `tag` VALUES (1037, 'VSCode', 1, 10005, 0, '2023-03-21 19:52:58.000', '2023-03-21 19:54:23.000');
INSERT INTO `tag` VALUES (1038, 'DevC++', 1, 10005, 0, '2023-03-21 19:53:30.000', '2023-03-21 19:54:26.000');
INSERT INTO `tag` VALUES (1039, '代码规范', 1, 10006, 0, '2023-03-21 19:53:53.000', '2023-03-21 19:54:31.000');
INSERT INTO `tag` VALUES (1040, '年终总结', 1, 10006, 0, '2023-03-21 19:54:04.000', '2023-03-21 19:54:34.000');
INSERT INTO `tag` VALUES (1041, '测试', 1, 10006, 0, '2023-03-21 19:54:08.000', '2023-03-21 19:54:37.000');
INSERT INTO `tag` VALUES (1042, '架构', 1, 10006, 0, '2023-03-21 19:54:11.000', '2023-03-21 19:54:41.000');
INSERT INTO `tag` VALUES (1043, '程序员', 1, 0, 0, '2023-03-21 19:55:00.000', '2023-03-21 19:55:00.000');
INSERT INTO `tag` VALUES (1044, '面试', 1, 0, 0, '2023-03-21 19:55:05.000', '2023-03-21 19:55:05.000');
INSERT INTO `tag` VALUES (1045, '产品', 1, 0, 0, '2023-03-21 19:55:21.000', '2023-03-21 19:55:21.000');
INSERT INTO `tag` VALUES (1046, '数据库', 1, 10000, 0, '2023-03-21 19:55:31.000', '2023-03-21 19:55:31.000');
INSERT INTO `tag` VALUES (1047, 'Linux', 1, 0, 0, '2023-03-21 19:57:34.000', '2023-03-21 19:57:34.000');
INSERT INTO `tag` VALUES (1048, 'Windows', 1, 0, 0, '2023-03-21 19:57:34.000', '2023-04-25 20:15:37.000');
INSERT INTO `tag` VALUES (1049, 'Oracle', 1, 10000, 0, '2023-03-21 19:59:09.000', '2023-04-25 20:15:31.000');
INSERT INTO `tag` VALUES (1050, 'MySQL', 1, 10000, 0, '2023-03-21 19:59:09.000', '2023-03-21 20:01:28.000');
INSERT INTO `tag` VALUES (1051, '算法', 1, 0, 0, '2023-03-21 20:01:46.000', '2023-03-21 20:01:46.000');
INSERT INTO `tag` VALUES (1052, 'HTML', 1, 10001, 0, '2023-03-21 20:01:50.000', '2023-03-21 20:02:15.000');
INSERT INTO `tag` VALUES (1053, '微信小程序', 1, 0, 0, '2023-03-21 20:02:20.000', '2023-03-21 20:02:20.000');
INSERT INTO `tag` VALUES (1054, 'Nginx', 1, 0, 0, '2023-03-21 20:02:39.000', '2023-03-21 20:02:39.000');
INSERT INTO `tag` VALUES (1055, 'JQuery', 1, 10001, 0, '2023-03-21 20:02:55.000', '2023-03-21 20:02:55.000');
INSERT INTO `tag` VALUES (1056, 'Canal', 1, 10005, 0, '2023-09-14 21:25:42.000', '2024-01-25 00:05:48.999');
INSERT INTO `tag` VALUES (1057, 'Kotlin', 1, 10003, 0, '2023-09-05 13:49:26.000', '2024-01-25 00:06:33.026');
INSERT INTO `tag` VALUES (1058, 'Flink', 1, 10005, 0, '2023-09-05 14:26:08.000', '2024-01-25 00:06:24.657');
INSERT INTO `tag` VALUES (1059, '通义灵码', 1, 0, 0, '2024-01-25 00:08:04.336', '2024-01-25 00:08:13.963');
INSERT INTO `tag` VALUES (1060, '微服务', 1, 0, 0, '2024-01-25 00:08:23.269', '2024-01-25 00:08:26.123');

SET FOREIGN_KEY_CHECKS = 1;

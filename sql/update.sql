ALTER TABLE `question` ADD COLUMN `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '题目图片URL' AFTER `content`;

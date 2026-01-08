-- Add file content fields to user_operation_log table
-- 用于存储文件转换操作的原始文件和结果文件

ALTER TABLE `user_operation_log` 
ADD COLUMN `source_file_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '原始文件内容(Base64编码)',
ADD COLUMN `source_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件名',
ADD COLUMN `result_file_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '结果文件内容(Base64编码)',
ADD COLUMN `result_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果文件名';

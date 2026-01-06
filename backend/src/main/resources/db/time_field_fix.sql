-- =========================================
-- 时间字段自动填充修复 SQL 脚本
-- 执行时间: 2026-01-06
-- =========================================

-- 1. practice_record 表：删除 cost_time 字段
ALTER TABLE `practice_record` DROP COLUMN `cost_time`;

-- 2. 验证各表时间字段默认值设置（DDL层面已正确配置）
-- 所有表的时间字段均已设置 DEFAULT CURRENT_TIMESTAMP，无需修改DDL
-- 但需要MyBatis-Plus的MetaObjectHandler来处理Java层面的自动填充
-- 已创建 MyMetaObjectHandler.java 处理以下字段的自动填充：
--   - createTime: INSERT时填充
--   - updateTime: INSERT和UPDATE时填充
--   - practiceTime: INSERT时填充

-- 3. 可选：更新现有NULL值记录（如有需要）
-- UPDATE `practice_round` SET create_time = NOW() WHERE create_time IS NULL;
-- UPDATE `practice_round` SET update_time = NOW() WHERE update_time IS NULL;
-- UPDATE `subject` SET create_time = NOW() WHERE create_time IS NULL;
-- UPDATE `wrong_book` SET create_time = NOW() WHERE create_time IS NULL;
-- UPDATE `wrong_book` SET update_time = NOW() WHERE update_time IS NULL;

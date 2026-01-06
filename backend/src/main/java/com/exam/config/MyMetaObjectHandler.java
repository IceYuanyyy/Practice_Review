package com.exam.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 用于自动填充实体类中标注了 @TableField(fill = FieldFill.INSERT) 或 FieldFill.INSERT_UPDATE 的字段
 * 
 * @author Exam System
 * @since 2026-01-06
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 填充 createTime, practiceTime 等INSERT类型字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充 createTime 字段 (用于 Subject, WrongBook, PracticeRound 等)
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 填充 updateTime 字段 (用于 WrongBook, PracticeRound 等，INSERT时也需要填充)
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 填充 practiceTime 字段 (用于 PracticeRecord)
        this.strictInsertFill(metaObject, "practiceTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * 填充 updateTime 等UPDATE类型字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充 updateTime 字段 (用于 WrongBook, PracticeRound 等)
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

# 多用户题库练习系统扩展开发计划

> **项目**: Exam Practice System (期末复习在线题库系统)  
> **目标**: 将单用户共享数据模式升级为多用户独立数据模式  
> **版本**: v2.0.0  
> **创建日期**: 2025-12-27

---

## 📋 项目现状分析

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 2.7.18 |
| **ORM框架** | MyBatis-Plus | 3.5.5 |
| **数据库** | MySQL | 8.0 |
| **前端框架** | Vue 3 | 3.4.0 |
| **UI组件库** | Naive UI | 2.38 |
| **构建工具** | Vite | 5.0 |

### 现有数据库结构

```
┌─────────────────────────────────────────────────────────────┐
│                         question                            │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ type             │ 题型                                     │
│ display_order    │ 显示顺序                                 │
│ subject          │ 科目                                     │
│ content          │ 题目内容                                 │
│ options          │ 选项(JSON)                               │
│ answer           │ 答案                                     │
│ analysis         │ 解析                                     │
│ difficulty       │ 难度                                     │
│ is_marked        │ 是否收藏(全局) ⚠️ 问题                   │
│ wrong_count      │ 错题次数(全局) ⚠️ 问题                   │
│ practice_count   │ 练习次数(全局) ⚠️ 问题                   │
│ create_time      │ 创建时间                                 │
│ update_time      │ 更新时间                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     practice_record                         │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ question_id (FK) │ 题目ID                                   │
│ user_answer      │ 用户答案                                 │
│ is_correct       │ 是否正确                                 │
│ cost_time        │ 答题耗时                                 │
│ practice_time    │ 练习时间                                 │
│ ⚠️ 缺少 user_id  │ 无法区分用户                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         subject                             │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ name (UK)        │ 科目名称                                 │
│ question_count   │ 题目数量                                 │
│ create_time      │ 创建时间                                 │
└─────────────────────────────────────────────────────────────┘
```

### 核心问题识别

| 问题 | 影响 | 优先级 |
|------|------|--------|
| `practice_record` 表缺少 `user_id` 字段 | 无法区分不同用户的练习记录 | **P0** |
| `question` 表的 `wrong_count`/`practice_count`/`is_marked` 是全局的 | 所有用户共享统计数据，无法个性化 | **P0** |
| 前端无登录/注册页面 | 用户无法独立使用系统 | **P0** |
| 前端无路由守卫 | 未登录用户可访问所有页面 | **P1** |
| API 层无认证机制 | 接口不安全 | **P0** |
| 导出功能无用户过滤 | 导出时会导出所有用户题目 | **P1** |
| 前端 Layout 用户名硬编码 | 无法显示真实用户信息 | **P1** |
| 无全局异常处理器 | Sa-Token 异常无法友好返回 | **P1** |
| 密码字段会返回前端 | 安全风险 | **P0** |

---

## 🎯 目标数据库结构

```
┌─────────────────────────────────────────────────────────────┐
│                           user                              │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ username (UK)    │ 用户名                                   │
│ password         │ 密码(BCrypt)                             │
│ nickname         │ 昵称                                     │
│ email            │ 邮箱                                     │
│ avatar_url       │ 头像URL                                  │
│ role             │ 角色(user/admin)                         │
│ status           │ 状态(0禁用/1启用)                        │
│ create_time      │ 创建时间                                 │
│ update_time      │ 更新时间                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   user_question_stats                       │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ user_id (FK)     │ 用户ID                                   │
│ question_id (FK) │ 题目ID                                   │
│ practice_count   │ 练习次数                                 │
│ wrong_count      │ 错题次数                                 │
│ is_marked        │ 是否收藏                                 │
│ last_practice_time│ 最后练习时间                            │
│ create_time      │ 创建时间                                 │
│ UK(user_id, question_id) │ 唯一约束                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     practice_record                         │
├─────────────────────────────────────────────────────────────┤
│ id (PK)          │ 主键ID                                   │
│ user_id (FK)     │ 用户ID ✅ 新增                           │
│ question_id (FK) │ 题目ID                                   │
│ user_answer      │ 用户答案                                 │
│ is_correct       │ 是否正确                                 │
│ cost_time        │ 答题耗时                                 │
│ practice_time    │ 练习时间                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 技术选型

### 认证框架：Sa-Token (推荐)

| 对比项 | Sa-Token | Spring Security |
|--------|----------|-----------------|
| **学习曲线** | 低（中文文档完善） | 高（配置复杂） |
| **集成难度** | 简单（开箱即用） | 中等 |
| **功能覆盖** | 登录/权限/JWT/SSO | 更全面但过度设计 |
| **适用场景** | 中小型项目 | 大型企业级项目 |
| **代码侵入性** | 低 | 中 |

**依赖添加**:
```xml
<!-- Sa-Token 权限认证 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.44.0</version>
</dependency>

<!-- Sa-Token 整合 JWT -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>1.44.0</version>
</dependency>
```

### 密码加密：BCrypt

使用 Hutool 的 BCrypt 工具类（项目已依赖 Hutool）。

---

## 🚀 分阶段实施计划

---

## Phase 1: 用户认证模块 (P0 - 预计5天)

### 1.1 数据库变更

#### [NEW] 创建 `user` 表

```sql
-- 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
    `avatar_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色:user/admin',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态:0禁用/1启用',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_username` (`username`) USING BTREE,
    INDEX `idx_role` (`role`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin');
```

---

### 1.2 后端实现

#### [MODIFY] pom.xml - 添加依赖

```xml
<!-- Sa-Token 权限认证 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.44.0</version>
</dependency>

<!-- Sa-Token 整合 JWT -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>1.44.0</version>
</dependency>
```

#### [MODIFY] application.yml - 添加 Sa-Token 配置

```yaml
# Sa-Token 配置
sa-token:
  # Token 名称
  token-name: Authorization
  # Token 有效期（7天，单位秒）
  timeout: 604800
  # Token 风格
  token-style: uuid
  # 是否允许同一账号多地同时登录
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个 Token
  is-share: false
  # 是否输出操作日志
  is-log: false
```

#### [NEW] entity/User.java

```java
package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码(BCrypt加密) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatarUrl;

    /** 角色: user/admin */
    private String role;

    /** 状态: 0禁用/1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

#### [NEW] mapper/UserMapper.java

```java
package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

#### [NEW] service/UserService.java

```java
package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.User;

public interface UserService extends IService<User> {
    
    /** 根据用户名查找用户 */
    User findByUsername(String username);
    
    /** 用户注册 */
    boolean register(String username, String password, String nickname);
    
    /** 用户登录，返回Token */
    String login(String username, String password);
    
    /** 获取当前登录用户 */
    User getCurrentUser();
}
```

#### [NEW] service/impl/UserServiceImpl.java

```java
package com.exam.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public boolean register(String username, String password, String nickname) {
        // 检查用户名是否存在
        if (findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        // 使用 Hutool BCrypt 加密密码
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setNickname(nickname != null ? nickname : username);
        user.setRole("user");
        user.setStatus(1);
        return save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // Sa-Token 登录
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getById(userId);
    }
}
```

#### [NEW] controller/AuthController.java

```java
package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.Result;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        try {
            userService.register(dto.getUsername(), dto.getPassword(), dto.getNickname());
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        try {
            String token = userService.login(dto.getUsername(), dto.getPassword());
            User user = userService.getCurrentUser();
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("已登出");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }
}
```

#### [NEW] dto/LoginDTO.java

```java
package com.exam.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
}
```

#### [NEW] dto/RegisterDTO.java

```java
package com.exam.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String nickname;
}
```

#### [NEW] config/SaTokenConfig.java

```java
package com.exam.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 认证配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 校验登录
            StpUtil.checkLogin();
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/auth/login",      // 登录接口放行
            "/api/auth/register",   // 注册接口放行
            "/api/test/**"          // 测试接口放行
        );
    }
}
```

---

### 1.3 前端实现

#### [NEW] views/Login.vue

登录页面，包含：
- 用户名输入框
- 密码输入框
- 登录按钮
- 跳转注册链接
- 表单验证

#### [NEW] views/Register.vue

注册页面，包含：
- 用户名输入框
- 密码输入框
- 确认密码输入框
- 昵称输入框（可选）
- 注册按钮
- 跳转登录链接

#### [MODIFY] api/request.js

```javascript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      console.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    // 处理 401 未授权
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    console.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

#### [NEW] api/auth.js

```javascript
import request from './request'

// 用户登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 用户登出
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/auth/me')
}
```

#### [NEW] stores/user.js

```javascript
import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    isLoggedIn: !!localStorage.getItem('token')
  }),

  actions: {
    async login(username, password) {
      const res = await login({ username, password })
      this.token = res.data.token
      this.user = res.data.user
      this.isLoggedIn = true
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    async logout() {
      try {
        await logout()
      } catch (e) {
        // 即使登出失败也清除本地状态
      }
      this.token = ''
      this.user = null
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    async fetchCurrentUser() {
      const res = await getCurrentUser()
      this.user = res.data
      localStorage.setItem('user', JSON.stringify(this.user))
    }
  }
})
```

#### [MODIFY] router/index.js

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      // ...existing routes...
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth !== false && !token) {
    // 需要登录但未登录，跳转登录页
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录访问登录/注册页，跳转首页
    next('/')
  } else {
    next()
  }
})

export default router
```

---

## Phase 2: 数据隔离重构 (P0 - 预计4天)

### 2.1 数据库变更

#### [MODIFY] practice_record 表

```sql
-- 添加 user_id 字段
ALTER TABLE `practice_record` 
ADD COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID' AFTER `id`;

-- 添加索引
ALTER TABLE `practice_record` 
ADD INDEX `idx_user_id` (`user_id` ASC);

-- 添加外键约束（可选）
ALTER TABLE `practice_record` 
ADD CONSTRAINT `fk_practice_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
```

#### [NEW] user_question_stats 表

```sql
-- 用户题目统计表
CREATE TABLE `user_question_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `practice_count` INT NOT NULL DEFAULT 0 COMMENT '练习次数',
    `wrong_count` INT NOT NULL DEFAULT 0 COMMENT '错误次数',
    `is_marked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `last_practice_time` DATETIME NULL DEFAULT NULL COMMENT '最后练习时间',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_question` (`user_id`, `question_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_question_id` (`question_id`) USING BTREE,
    INDEX `idx_wrong_count` (`wrong_count`) USING BTREE,
    CONSTRAINT `fk_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_stats_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户题目统计表';
```

---

### 2.2 后端实现

#### [MODIFY] entity/PracticeRecord.java

添加 `userId` 字段：

```java
/** 用户ID */
private Long userId;
```

#### [NEW] entity/UserQuestionStats.java

```java
package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_question_stats")
public class UserQuestionStats {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long questionId;
    private Integer practiceCount;
    private Integer wrongCount;
    private Boolean isMarked;
    private LocalDateTime lastPracticeTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

#### [NEW] mapper/UserQuestionStatsMapper.java

```java
@Mapper
public interface UserQuestionStatsMapper extends BaseMapper<UserQuestionStats> {
}
```

#### [NEW] service/UserQuestionStatsService.java

```java
public interface UserQuestionStatsService extends IService<UserQuestionStats> {
    
    /** 更新用户题目统计 */
    void updateStats(Long userId, Long questionId, boolean isCorrect);
    
    /** 获取用户错题ID列表 */
    List<Long> getWrongQuestionIds(Long userId);
    
    /** 获取用户收藏题目ID列表 */
    List<Long> getMarkedQuestionIds(Long userId);
}
```

#### [MODIFY] controller/PracticeController.java

核心修改：所有操作关联当前用户

```java
@PostMapping("/submit")
public Result<Map<String, Object>> submitAnswer(@RequestBody PracticeRecord record) {
    // 获取当前登录用户ID
    Long userId = StpUtil.getLoginIdAsLong();
    record.setUserId(userId);
    
    // ...existing answer checking logic...
    
    // 保存练习记录
    practiceRecordService.save(record);
    
    // 更新用户题目统计（而非全局统计）
    userQuestionStatsService.updateStats(userId, record.getQuestionId(), isCorrect);
    
    // ...return result...
}

@GetMapping("/wrong")
public Result<PageResult<Question>> getWrongQuestions(...) {
    Long userId = StpUtil.getLoginIdAsLong();
    
    // 从 user_question_stats 查询当前用户的错题
    List<Long> wrongIds = userQuestionStatsService.getWrongQuestionIds(userId);
    // ...filter by wrongIds...
}

@GetMapping("/statistics")
public Result<DashboardDTO> getStatistics() {
    Long userId = StpUtil.getLoginIdAsLong();
    
    // 只统计当前用户的数据
    // ...
}
```

---

## Phase 3: 用户中心 (P1 - 预计2天)

### 3.1 后端实现

- 创建 UserController
- 实现个人资料获取/更新接口
- 实现密码修改接口

### 3.2 前端实现

- 创建 Profile.vue 个人中心页面
- 修改 Layout.vue 添加用户信息展示
- 添加用户下拉菜单

---

## Phase 4: 权限管理与管理员功能 (P2 - 预计4天)

### 4.1 角色权限设计（已更新）

> **设计理念**: 这是一个**开放式自主练习网站**，用户可以完全自主管理自己的题库和练习数据。

| 功能 | user | admin |
|------|------|-------|
| **练习相关** | | |
| 练习题目 | ✅ (自己的题库) | ✅ (所有) |
| 查看错题本 | ✅ (仅自己) | ✅ (可查看任意用户) |
| 查看统计 | ✅ (仅自己) | ✅ (全局+用户维度) |
| **题目管理** | | |
| 题目增删改查 | ✅ (自己的题目) | ✅ (所有题目) |
| 导入题目 | ✅ (导入到自己的题库) | ✅ (可指定用户) |
| 导出题目 | ✅ (导出自己的题目) | ✅ (导出任意用户) |
| **管理员专属** | | |
| 用户管理 | ❌ | ✅ |
| 查看用户活动日志 | ❌ | ✅ |
| 查看用户登录记录 | ❌ | ✅ |
| 系统数据统计 | ❌ | ✅ |
| 公共题库管理 | ❌ | ✅ (可选) |

---

### 4.2 数据库变更 - 用户活动日志

#### [NEW] 创建 `user_login_log` 用户登录日志表

```sql
-- 用户登录日志表
CREATE TABLE `user_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `login_ip` VARCHAR(50) NULL COMMENT '登录IP',
    `login_location` VARCHAR(255) NULL COMMENT '登录地点(通过IP解析)',
    `browser` VARCHAR(100) NULL COMMENT '浏览器类型',
    `os` VARCHAR(100) NULL COMMENT '操作系统',
    `user_agent` VARCHAR(500) NULL COMMENT 'User-Agent',
    `login_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '登录状态:0失败/1成功',
    `fail_reason` VARCHAR(255) NULL COMMENT '失败原因',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_login_time` (`login_time`) USING BTREE,
    INDEX `idx_login_ip` (`login_ip`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志表';
```

#### [NEW] 创建 `user_operation_log` 用户操作日志表

```sql
-- 用户操作日志表（记录导入导出等关键操作）
CREATE TABLE `user_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型:IMPORT/EXPORT/DELETE_BATCH/CLEAR等',
    `operation_desc` VARCHAR(500) NULL COMMENT '操作描述',
    `operation_data` JSON NULL COMMENT '操作数据(如导入的题目数量、科目等)',
    `request_ip` VARCHAR(50) NULL COMMENT '请求IP',
    `request_url` VARCHAR(255) NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) NULL COMMENT '请求方法',
    `operation_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作状态:0失败/1成功',
    `error_msg` TEXT NULL COMMENT '错误信息',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_operation_type` (`operation_type`) USING BTREE,
    INDEX `idx_operation_time` (`operation_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户操作日志表';
```

---

### 4.3 题目归属设计

由于用户可以自主管理题目，需要修改 `question` 表：

#### [MODIFY] question 表 - 添加用户归属

```sql
-- 添加 owner_id 字段（题目所属用户）
ALTER TABLE `question` 
ADD COLUMN `owner_id` BIGINT NULL COMMENT '题目所属用户ID(NULL表示公共题库)' AFTER `id`;

-- 添加索引
ALTER TABLE `question` 
ADD INDEX `idx_owner_id` (`owner_id` ASC);

-- 添加外键约束（可选）
ALTER TABLE `question` 
ADD CONSTRAINT `fk_question_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;
```

**设计说明**:
- `owner_id = NULL`: 公共题库（管理员创建，所有用户可见可练习）
- `owner_id = 用户ID`: 用户私有题库（仅该用户可见）

---

### 4.4 后端实现

#### [NEW] entity/UserLoginLog.java

```java
@Data
@TableName("user_login_log")
public class UserLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String browser;
    private String os;
    private String userAgent;
    private Integer loginStatus;  // 0失败/1成功
    private String failReason;
    private LocalDateTime loginTime;
}
```

#### [NEW] entity/UserOperationLog.java

```java
@Data
@TableName("user_operation_log")
public class UserOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String operationType;  // IMPORT/EXPORT/DELETE_BATCH/CLEAR
    private String operationDesc;
    private String operationData;  // JSON格式
    private String requestIp;
    private String requestUrl;
    private String requestMethod;
    private Integer operationStatus;  // 0失败/1成功
    private String errorMsg;
    private LocalDateTime operationTime;
}
```

#### [NEW] service/UserLoginLogService.java

```java
public interface UserLoginLogService extends IService<UserLoginLog> {
    
    /** 记录登录日志 */
    void recordLogin(Long userId, String username, HttpServletRequest request, 
                     boolean success, String failReason);
    
    /** 分页查询用户登录日志（管理员用） */
    Page<UserLoginLog> getLoginLogs(Long userId, LocalDateTime startTime, 
                                     LocalDateTime endTime, int page, int size);
    
    /** 获取今日登录次数 */
    long getTodayLoginCount();
    
    /** 获取今日活跃用户数 */
    long getTodayActiveUserCount();
}
```

#### [NEW] service/UserOperationLogService.java

```java
public interface UserOperationLogService extends IService<UserOperationLog> {
    
    /** 记录操作日志 */
    void recordOperation(Long userId, String username, String operationType, 
                         String operationDesc, Object operationData, 
                         HttpServletRequest request, boolean success, String errorMsg);
    
    /** 分页查询用户操作日志（管理员用） */
    Page<UserOperationLog> getOperationLogs(Long userId, String operationType, 
                                            LocalDateTime startTime, LocalDateTime endTime, 
                                            int page, int size);
}
```

#### [NEW] util/IpUtil.java - IP地址解析工具

```java
public class IpUtil {
    
    /** 获取客户端真实IP */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /** 根据IP获取地理位置（可使用第三方API或本地IP库） */
    public static String getLocationByIp(String ip) {
        // 可选方案：
        // 1. 使用 ip2region 离线库（推荐，无网络依赖）
        // 2. 使用在线API（如太平洋网络IP查询、百度地图等）
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "本地";
        }
        return "未知"; // 实际项目可集成ip2region
    }
    
    /** 解析User-Agent获取浏览器和操作系统信息 */
    public static Map<String, String> parseUserAgent(String userAgent) {
        // 使用 Hutool 的 UserAgentUtil
        cn.hutool.http.useragent.UserAgent ua = 
            cn.hutool.http.useragent.UserAgentUtil.parse(userAgent);
        Map<String, String> result = new HashMap<>();
        result.put("browser", ua.getBrowser().getName() + " " + ua.getVersion());
        result.put("os", ua.getOs().getName());
        return result;
    }
}
```

#### [MODIFY] AuthController.java - 登录时记录日志

```java
@Autowired
private UserLoginLogService loginLogService;

@PostMapping("/login")
public Result<Map<String, Object>> login(@RequestBody LoginDTO dto, 
                                          HttpServletRequest request) {
    try {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        User user = userService.getCurrentUser();
        
        // 记录登录成功日志
        loginLogService.recordLogin(user.getId(), user.getUsername(), request, true, null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    } catch (RuntimeException e) {
        // 记录登录失败日志
        loginLogService.recordLogin(null, dto.getUsername(), request, false, e.getMessage());
        return Result.error(e.getMessage());
    }
}
```

#### [MODIFY] ImportController.java - 导入时记录日志

```java
@Autowired
private UserOperationLogService operationLogService;

@PostMapping("/excel")
public Result<String> importExcel(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) String subject,
                                  HttpServletRequest request) {
    Long userId = StpUtil.getLoginIdAsLong();
    User user = userService.getById(userId);
    
    try {
        // 导入逻辑...
        int importCount = doImport(file, subject, userId);
        
        // 记录操作日志
        Map<String, Object> data = new HashMap<>();
        data.put("fileName", file.getOriginalFilename());
        data.put("subject", subject);
        data.put("importCount", importCount);
        operationLogService.recordOperation(userId, user.getUsername(), 
            "IMPORT", "导入题目", data, request, true, null);
        
        return Result.success("成功导入 " + importCount + " 道题目");
    } catch (Exception e) {
        // 记录失败日志
        operationLogService.recordOperation(userId, user.getUsername(), 
            "IMPORT", "导入题目失败", null, request, false, e.getMessage());
        return Result.error("导入失败: " + e.getMessage());
    }
}
```

#### [NEW] controller/AdminController.java - 管理员功能

```java
package com.exam.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
// ... other imports

/**
 * 管理员 Controller
 */
@RestController
@RequestMapping("/api/admin")
@SaCheckRole("admin")  // 整个Controller仅管理员可访问
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserLoginLogService loginLogService;
    @Autowired
    private UserOperationLogService operationLogService;
    @Autowired
    private QuestionService questionService;

    // ==================== 用户管理 ====================
    
    /** 获取用户列表（分页） */
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        // 实现分页查询...
    }

    /** 获取用户详情 */
    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /** 修改用户状态（启用/禁用） */
    @PutMapping("/users/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, 
                                           @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    /** 重置用户密码 */
    @PutMapping("/users/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id, "123456");
        return Result.success("密码已重置为: 123456");
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("用户已删除");
    }

    // ==================== 用户登录日志 ====================
    
    /** 获取用户登录日志 */
    @GetMapping("/login-logs")
    public Result<PageResult<UserLoginLog>> getLoginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer loginStatus) {
        // 实现分页查询...
    }

    // ==================== 用户操作日志 ====================
    
    /** 获取用户操作日志 */
    @GetMapping("/operation-logs")
    public Result<PageResult<UserOperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        // 实现分页查询...
    }

    // ==================== 系统统计 ====================
    
    /** 获取系统概览统计 */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("totalQuestions", questionService.count());
        stats.put("todayLoginCount", loginLogService.getTodayLoginCount());
        stats.put("todayActiveUsers", loginLogService.getTodayActiveUserCount());
        return Result.success(stats);
    }

    /** 获取用户的题库统计 */
    @GetMapping("/users/{id}/question-stats")
    public Result<Map<String, Object>> getUserQuestionStats(@PathVariable Long id) {
        return Result.success(questionService.getUserQuestionStats(id));
    }
}
```

#### [MODIFY] QuestionController.java - 题目归属用户

```java
/** 获取题目列表（只返回当前用户的题目+公共题库） */
@GetMapping
public Result<PageResult<Question>> getQuestions(...) {
    Long userId = StpUtil.getLoginIdAsLong();
    
    // 查询条件：owner_id = 当前用户 OR owner_id IS NULL（公共题库）
    QueryWrapper<Question> wrapper = new QueryWrapper<>();
    wrapper.and(w -> w.eq("owner_id", userId).or().isNull("owner_id"));
    // ... 其他筛选条件
}

/** 新增题目（自动设置owner_id为当前用户） */
@PostMapping
public Result<Long> addQuestion(@RequestBody Question question) {
    Long userId = StpUtil.getLoginIdAsLong();
    question.setOwnerId(userId);  // 设置题目归属
    questionService.save(question);
    return Result.success(question.getId());
}

/** 删除题目（只能删除自己的题目） */
@DeleteMapping("/{id}")
public Result<String> deleteQuestion(@PathVariable Long id) {
    Long userId = StpUtil.getLoginIdAsLong();
    Question question = questionService.getById(id);
    
    // 权限校验：只能删除自己的题目（管理员除外）
    User currentUser = userService.getById(userId);
    if (!"admin".equals(currentUser.getRole()) 
        && !userId.equals(question.getOwnerId())) {
        return Result.error("无权操作此题目");
    }
    
    questionService.removeById(id);
    return Result.success("删除成功");
}
```

---

### 4.5 前端实现

#### [NEW] views/admin/UserManage.vue - 用户管理页面

- 用户列表（支持搜索、筛选）
- 用户状态切换（启用/禁用）
- 重置密码
- 查看用户详情
- 查看用户题库统计

#### [NEW] views/admin/LoginLogs.vue - 登录日志页面

- 登录日志列表
- 按用户/时间/IP筛选
- 显示登录IP、地点、浏览器、状态
- 导出日志（可选）

#### [NEW] views/admin/OperationLogs.vue - 操作日志页面

- 操作日志列表
- 按用户/操作类型/时间筛选
- 显示导入导出详情
- 导出日志（可选）

#### [NEW] views/admin/Dashboard.vue - 管理员仪表盘

- 系统概览（用户总数、题目总数、今日活跃等）
- 最近登录用户
- 最近操作记录
- 数据趋势图表（可选）

#### [NEW] views/admin/AdminLayout.vue - 管理后台布局

- 管理后台专用侧边栏
- 管理员菜单导航

#### [MODIFY] router/index.js - 添加管理员路由

```javascript
// 管理员路由
{
  path: '/admin',
  name: 'AdminLayout',
  component: () => import('@/views/admin/AdminLayout.vue'),
  meta: { requiresAdmin: true },
  children: [
    {
      path: 'dashboard',
      name: 'AdminDashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { title: '管理后台' }
    },
    {
      path: 'users',
      name: 'UserManage',
      component: () => import('@/views/admin/UserManage.vue'),
      meta: { title: '用户管理' }
    },
    {
      path: 'login-logs',
      name: 'LoginLogs',
      component: () => import('@/views/admin/LoginLogs.vue'),
      meta: { title: '登录日志' }
    },
    {
      path: 'operation-logs',
      name: 'OperationLogs',
      component: () => import('@/views/admin/OperationLogs.vue'),
      meta: { title: '操作日志' }
    }
  ]
}
```

#### [MODIFY] views/Layout.vue

- 管理员显示「管理后台」入口
- 普通用户不显示管理菜单

---

## 📁 文件变更清单

### 后端 (backend)

| 操作 | 文件路径 |
|------|----------|
| MODIFY | pom.xml |
| MODIFY | application.yml |
| NEW | entity/User.java |
| NEW | entity/UserQuestionStats.java |
| NEW | entity/UserLoginLog.java |
| NEW | entity/UserOperationLog.java |
| MODIFY | entity/PracticeRecord.java |
| MODIFY | entity/Question.java (添加 ownerId) |
| NEW | mapper/UserMapper.java |
| NEW | mapper/UserQuestionStatsMapper.java |
| NEW | mapper/UserLoginLogMapper.java |
| NEW | mapper/UserOperationLogMapper.java |
| NEW | service/UserService.java |
| NEW | service/impl/UserServiceImpl.java |
| NEW | service/UserQuestionStatsService.java |
| NEW | service/UserLoginLogService.java |
| NEW | service/UserOperationLogService.java |
| NEW | controller/AuthController.java |
| NEW | controller/AdminController.java |
| MODIFY | controller/PracticeController.java |
| MODIFY | controller/QuestionController.java |
| MODIFY | controller/ImportController.java |
| NEW | config/SaTokenConfig.java |
| NEW | config/GlobalExceptionHandler.java |
| NEW | config/StpInterfaceImpl.java |
| NEW | dto/LoginDTO.java |
| NEW | dto/RegisterDTO.java |
| NEW | util/IpUtil.java |

### 前端 (frontend)

| 操作 | 文件路径 |
|------|----------|
| NEW | views/Login.vue |
| NEW | views/Register.vue |
| NEW | views/Profile.vue |
| MODIFY | views/Layout.vue |
| NEW | views/admin/AdminLayout.vue |
| NEW | views/admin/Dashboard.vue |
| NEW | views/admin/UserManage.vue |
| NEW | views/admin/LoginLogs.vue |
| NEW | views/admin/OperationLogs.vue |
| NEW | api/auth.js |
| NEW | api/admin.js |
| MODIFY | api/request.js |
| NEW | stores/user.js |
| MODIFY | router/index.js |

---

## ⚠️ 风险与注意事项

### 数据迁移风险

现有 `practice_record` 表没有 `user_id` 字段，添加字段后需要处理历史数据：
- **方案1**: 将历史数据关联到默认管理员账号
- **方案2**: 清空历史练习记录（仅保留题库）
- **方案3**: 新增字段设为可空，历史数据保持 NULL

现有 `question` 表没有 `owner_id` 字段：
- 历史题目可设为公共题库（owner_id = NULL）
- 或关联到管理员账号

### 向后兼容性

- 所有新增 API 使用独立路径 `/api/auth/*`, `/api/admin/*`
- 现有 API 保持路径不变，仅增加用户校验
- 前端需要同时更新，否则会出现 401 错误

### 密码安全

- 必须使用 BCrypt 加密存储密码
- 禁止在日志中输出密码
- Token 不要存储敏感信息
- **User 实体的 password 字段需添加 @JsonIgnore 注解**

### IP 地址解析

- 可选集成 ip2region 离线库（推荐）
- 或使用在线 API（需处理网络异常）

### Token 安全增强

- 建议开启 Token 自动续期（activity-timeout）
- 可选：绑定 IP 或设备指纹

---

## 📅 开发日程表（按天细分）

> **总工期**: 17 天  
> **开始日期**: 待定  
> **每日工作时长**: 约 6-8 小时

---

### 第一周：用户认证模块 (Day 1-5)

#### Day 1 - 数据库设计与后端基础

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 执行数据迁移脚本 A.1-A.3 | `user` 表、`practice_record` 修改、`question` 修改 |
| 上午 | 执行数据迁移脚本 A.4-A.5 | `user_question_stats` 表、日志表 |
| 下午 | 添加 Sa-Token 依赖到 pom.xml | 依赖配置完成 |
| 下午 | 配置 application.yml (Sa-Token) | 配置文件更新 |
| 下午 | 创建 User 实体类 (含 @JsonIgnore) | `entity/User.java` |

**验收标准**: 
- [ ] 数据库表创建成功，可通过 Navicat 查看
- [ ] Spring Boot 项目能正常启动

---

#### Day 2 - 用户服务层实现

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 UserMapper | `mapper/UserMapper.java` |
| 上午 | 创建 UserService 接口 | `service/UserService.java` |
| 上午 | 创建 UserServiceImpl 实现 | `service/impl/UserServiceImpl.java` |
| 下午 | 创建 LoginDTO、RegisterDTO | `dto/LoginDTO.java`, `dto/RegisterDTO.java` |
| 下午 | 实现注册逻辑 (BCrypt 加密) | 注册功能可用 |
| 下午 | 实现登录逻辑 (Sa-Token) | 登录功能可用 |

**验收标准**: 
- [ ] 可通过 Postman 测试注册接口
- [ ] 可通过 Postman 测试登录接口，返回 Token

---

#### Day 3 - 认证配置与异常处理

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 SaTokenConfig (拦截器) | `config/SaTokenConfig.java` |
| 上午 | 创建 StpInterfaceImpl | `config/StpInterfaceImpl.java` |
| 上午 | 创建 GlobalExceptionHandler | `config/GlobalExceptionHandler.java` |
| 下午 | 创建 AuthController | `controller/AuthController.java` |
| 下午 | 测试 /api/auth/* 接口 | 登录/注册/登出/获取用户信息 |
| 下午 | 测试未登录访问受保护接口 | 返回 401 错误 |

**验收标准**: 
- [ ] 未登录访问 /api/questions 返回 401
- [ ] 登录后携带 Token 可正常访问
- [ ] 权限不足返回 403

---

#### Day 4 - 前端登录页面

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 stores/user.js (Pinia) | 用户状态管理 |
| 上午 | 创建 api/auth.js | 认证 API 封装 |
| 上午 | 修改 api/request.js (Token 拦截器) | 请求拦截器配置 |
| 下午 | 创建 Login.vue 页面 | 登录页面 UI |
| 下午 | 实现登录表单验证 | 用户名/密码验证 |
| 下午 | 实现登录逻辑 | 登录成功跳转首页 |

**验收标准**: 
- [ ] 登录页面 UI 美观
- [ ] 登录成功后跳转首页
- [ ] Token 存储到 localStorage

---

#### Day 5 - 前端注册页面与路由守卫

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 Register.vue 页面 | 注册页面 UI |
| 上午 | 实现注册逻辑 | 注册成功跳转登录页 |
| 下午 | 修改 router/index.js | 添加登录/注册路由 |
| 下午 | 实现路由守卫 | 未登录重定向到登录页 |
| 下午 | 修改 Layout.vue | 显示当前用户信息 |
| 下午 | 实现退出登录 | 清除 Token 并跳转 |

**验收标准**: 
- [ ] 未登录访问任何页面自动跳转登录页
- [ ] 已登录访问登录页自动跳转首页
- [ ] 顶部显示当前用户昵称
- [ ] 退出登录功能正常

---

### 第二周：数据隔离重构 (Day 6-9)

#### Day 6 - 用户题目统计服务

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 UserQuestionStats 实体 | `entity/UserQuestionStats.java` |
| 上午 | 创建 UserQuestionStatsMapper | `mapper/UserQuestionStatsMapper.java` |
| 上午 | 创建 UserQuestionStatsService 接口 | `service/UserQuestionStatsService.java` |
| 下午 | 实现 updateStats() 方法 | 更新用户答题统计 |
| 下午 | 实现 getWrongQuestionIds() | 获取用户错题ID |
| 下午 | 实现 getMarkedQuestionIds() | 获取用户收藏ID |

**验收标准**: 
- [ ] 单元测试通过
- [ ] 可正确插入/更新用户题目统计

---

#### Day 7 - PracticeController 重构

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 修改 PracticeRecord 实体 | 添加 userId 字段 |
| 上午 | 修改 submitAnswer() | 关联当前用户 |
| 上午 | 修改 submitAnswer() | 更新 user_question_stats |
| 下午 | 修改 getWrongQuestions() | 按用户过滤 |
| 下午 | 修改 getStatistics() | 按用户统计 |
| 下午 | 修改 clearWrongQuestions() | 按用户清除 |

**验收标准**: 
- [ ] 提交答题正确记录到 practice_record (含 user_id)
- [ ] 错题本只显示当前用户的错题
- [ ] 统计数据只显示当前用户的

---

#### Day 8 - QuestionController 重构

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 修改 Question 实体 | 添加 ownerId 字段 |
| 上午 | 修改 getQuestionList() | 只返回当前用户题目+公共题库 |
| 上午 | 修改 addQuestion() | 自动设置 ownerId |
| 下午 | 修改 deleteQuestion() | 权限校验 |
| 下午 | 修改 updateQuestion() | 权限校验 |
| 下午 | 修改 getRandomQuestion() | 按用户过滤 |

**验收标准**: 
- [ ] 新增题目自动归属当前用户
- [ ] 只能看到自己的题目和公共题库
- [ ] 不能删除/修改他人题目

---

#### Day 9 - ImportController 重构与前端验证

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 修改 importExcel() | 导入题目关联用户 |
| 上午 | 修改 exportExcel() | 按用户过滤导出 |
| 上午 | 创建 IpUtil 工具类 | IP 解析工具 |
| 下午 | 前端测试：多用户场景 | 用户数据隔离验证 |
| 下午 | 修复发现的 Bug | Bug 修复 |
| 下午 | 代码审查与优化 | 代码质量提升 |

**验收标准**: 
- [ ] 用户 A 的练习记录用户 B 看不到
- [ ] 用户 A 的错题用户 B 看不到
- [ ] 用户 A 导入的题目用户 B 看不到
- [ ] 导出只导出自己的题目

---

### 第二周：用户中心 (Day 10-11)

#### Day 10 - 用户中心后端

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 UserController | `controller/UserController.java` |
| 上午 | 实现获取个人资料接口 | GET /api/user/profile |
| 上午 | 实现更新个人资料接口 | PUT /api/user/profile |
| 下午 | 实现修改密码接口 | PUT /api/user/password |
| 下午 | UserService 添加相关方法 | resetPassword() 等 |
| 下午 | 测试用户中心接口 | Postman 测试 |

**验收标准**: 
- [ ] 可获取当前用户资料
- [ ] 可修改昵称/邮箱
- [ ] 可修改密码

---

#### Day 11 - 用户中心前端

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 Profile.vue 页面 | 个人中心 UI |
| 上午 | 实现个人信息展示 | 用户信息卡片 |
| 上午 | 实现修改昵称功能 | 表单提交 |
| 下午 | 实现修改密码功能 | 密码修改弹窗 |
| 下午 | 添加个人中心路由 | 路由配置 |
| 下午 | Layout 添加用户下拉菜单 | 个人中心/退出登录 |

**验收标准**: 
- [ ] 个人中心页面 UI 美观
- [ ] 可修改个人信息
- [ ] 顶部用户头像可点击展开菜单

---

### 第三周：权限管理与管理员功能 (Day 12-15)

#### Day 12 - 管理员后端 (用户管理)

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 AdminController | `controller/AdminController.java` |
| 上午 | 实现用户列表接口 | GET /api/admin/users |
| 上午 | 实现用户详情接口 | GET /api/admin/users/{id} |
| 下午 | 实现修改用户状态接口 | PUT /api/admin/users/{id}/status |
| 下午 | 实现重置密码接口 | PUT /api/admin/users/{id}/reset-password |
| 下午 | 实现删除用户接口 | DELETE /api/admin/users/{id} |

**验收标准**: 
- [ ] 普通用户访问 /api/admin/* 返回 403
- [ ] 管理员可获取用户列表
- [ ] 管理员可禁用/启用用户

---

#### Day 13 - 管理员后端 (日志功能)

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 UserLoginLog 实体 | `entity/UserLoginLog.java` |
| 上午 | 创建 UserLoginLogService | 登录日志服务 |
| 上午 | 修改 AuthController | 登录时记录日志 |
| 下午 | 创建 UserOperationLog 实体 | `entity/UserOperationLog.java` |
| 下午 | 创建 UserOperationLogService | 操作日志服务 |
| 下午 | 修改 ImportController | 导入/导出时记录日志 |

**验收标准**: 
- [ ] 用户登录后日志表有记录
- [ ] 导入/导出后操作日志表有记录
- [ ] 日志包含 IP、浏览器信息

---

#### Day 14 - 管理员后端 (日志查询与统计)

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 实现登录日志查询接口 | GET /api/admin/login-logs |
| 上午 | 实现操作日志查询接口 | GET /api/admin/operation-logs |
| 上午 | 实现系统统计接口 | GET /api/admin/statistics |
| 下午 | 实现用户题库统计接口 | GET /api/admin/users/{id}/question-stats |
| 下午 | 实现今日登录/活跃用户统计 | getTodayLoginCount() 等 |
| 下午 | 测试所有管理员接口 | Postman 测试 |

**验收标准**: 
- [ ] 可分页查询登录日志
- [ ] 可按条件筛选日志
- [ ] 系统统计数据正确

---

#### Day 15 - 管理员前端

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 创建 AdminLayout.vue | 管理后台布局 |
| 上午 | 创建 Dashboard.vue | 管理仪表盘 |
| 上午 | 创建 UserManage.vue | 用户管理页面 |
| 下午 | 创建 LoginLogs.vue | 登录日志页面 |
| 下午 | 创建 OperationLogs.vue | 操作日志页面 |
| 下午 | 添加管理员路由 | 路由配置 |
| 下午 | Layout 添加管理后台入口 | 管理员可见 |

**验收标准**: 
- [ ] 管理员可访问管理后台
- [ ] 普通用户看不到管理后台入口
- [ ] 仪表盘显示系统概览数据
- [ ] 可管理用户、查看日志

---

### 第三周：测试与修复 (Day 16-17)

#### Day 16 - 集成测试

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 用户认证流程测试 | 注册→登录→使用→退出 |
| 上午 | 数据隔离测试 | 多用户场景验证 |
| 上午 | 权限控制测试 | 普通用户/管理员权限 |
| 下午 | 导入导出测试 | Excel 功能验证 |
| 下午 | 错题本/统计测试 | 功能完整性 |
| 下午 | 边界条件测试 | 异常输入处理 |

**验收标准**: 
- [ ] 所有核心功能正常
- [ ] 无严重 Bug
- [ ] 记录发现的问题

---

#### Day 17 - Bug 修复与优化

| 时段 | 任务 | 交付物 |
|------|------|--------|
| 上午 | 修复测试发现的 Bug | Bug 修复 |
| 上午 | 代码审查与优化 | 代码质量提升 |
| 下午 | UI/UX 优化 | 界面细节调整 |
| 下午 | 性能检查 | 慢查询优化 |
| 下午 | 文档更新 | README/API文档 |
| 下午 | 部署准备 | 打包测试 |

**验收标准**: 
- [ ] 所有已知 Bug 修复
- [ ] 代码符合规范
- [ ] 可成功打包部署

---

## 📊 里程碑总览

```
Week 1 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Day 1   Day 2   Day 3   Day 4   Day 5
  [数据库] [服务层] [配置]  [前端登录] [前端注册]
    ↓       ↓       ↓        ↓         ↓
  ══════════════════════════════════════════════════
          ★ 里程碑1: 用户认证完成 ★

Week 2 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Day 6   Day 7   Day 8   Day 9   Day 10  Day 11
  [统计]  [练习]  [题目]  [导入]  [用户中心后端] [前端]
    ↓       ↓       ↓       ↓        ↓         ↓
  ══════════════════════════════════════════════════
      ★ 里程碑2: 数据隔离完成 ★   ★ 里程碑3: 用户中心完成 ★

Week 3 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Day 12  Day 13  Day 14  Day 15  Day 16  Day 17
  [用户管理] [日志] [统计] [前端] [测试]  [修复]
     ↓        ↓      ↓      ↓      ↓       ↓
  ══════════════════════════════════════════════════
     ★ 里程碑4: 管理员功能完成 ★   ★ 里程碑5: 项目交付 ★
```

---

## ⏰ 每日检查点

每天结束前，请确认:

- [ ] 当日任务全部完成
- [ ] 代码已提交到 Git
- [ ] 无阻塞性问题
- [ ] 明日任务清晰

如遇阻塞问题，及时调整后续计划。



## 📚 参考资料

- [Sa-Token 官方文档](https://sa-token.cc/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Naive UI 组件库](https://www.naiveui.com/)
- [ip2region IP地址离线库](https://github.com/lionsoul2014/ip2region)

---

## 📝 审查补充（2025-12-27）

> 以下内容为代码审查后发现的遗漏项，已整合到上述各阶段中。

### 补充1: 全局异常处理器

**需添加文件**: `config/GlobalExceptionHandler.java`

```java
package com.exam.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.exam.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Sa-Token 未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        return Result.error(401, "未登录或登录已过期");
    }

    // Sa-Token 角色权限不足
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRole(NotRoleException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    // Sa-Token 权限不足
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermission(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    // 其他运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error(500, e.getMessage());
    }
}
```

---

### 补充2: StpInterfaceImpl 权限接口实现

**需添加文件**: `config/StpInterfaceImpl.java`

```java
package com.exam.config;

import cn.dev33.satoken.stp.StpInterface;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限接口实现
 * 用于 @SaCheckRole 等注解的权限校验
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    /**
     * 获取用户角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.parseLong(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(user.getRole());
    }

    /**
     * 获取用户权限列表（暂不使用细粒度权限）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
```

---

### 补充3: User 实体密码安全处理

**修改文件**: `entity/User.java`

```java
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@TableName("user")
public class User {
    // ...其他字段...
    
    /** 密码(BCrypt加密) - 返回JSON时忽略此字段 */
    @JsonIgnore
    private String password;
    
    // ...其他字段...
}
```

---

### 补充4: 导出功能按用户过滤

**修改文件**: `controller/ImportController.java`

```java
@GetMapping("/export")
public void exportExcel(HttpServletResponse response) {
    try {
        Long userId = StpUtil.getLoginIdAsLong();
        User currentUser = userService.getById(userId);
        
        // 构建查询条件：只导出当前用户的题目 + 公共题库
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        if (!"admin".equals(currentUser.getRole())) {
            // 普通用户：只能导出自己的题目和公共题库
            wrapper.and(w -> w.eq("owner_id", userId).or().isNull("owner_id"));
        }
        // 管理员：可以导出所有题目（不加过滤条件）
        
        List<Question> questions = questionService.list(wrapper);
        
        // ...后续导出逻辑保持不变...
    } catch (IOException e) {
        log.error("导出失败", e);
    }
}
```

---

### 补充5: 前端 Layout 动态显示用户信息

**修改文件**: `views/Layout.vue`

```vue
<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<template>
  <!-- 替换原来的硬编码用户信息 -->
  <div class="user-profile" @click="showUserMenu = true">
    <n-avatar round size="small" :style="{ backgroundColor: '#10b981', color: 'white' }">
      {{ userStore.user?.nickname?.charAt(0) || '用' }}
    </n-avatar>
    <span class="username">{{ userStore.user?.nickname || '用户' }}</span>
  </div>
</template>
```

---

### 补充6: 前端错误处理增强

**修改文件**: `api/request.js`

```javascript
// 响应拦截器 - 增强错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      // 显示错误提示
      window.$message?.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未登录或Token过期
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.$message?.warning('登录已过期，请重新登录')
          window.location.href = '/login'
          break
        case 403:
          // 权限不足
          window.$message?.error('权限不足')
          break
        case 500:
          // 服务器错误
          window.$message?.error('服务器错误，请稍后重试')
          break
        default:
          window.$message?.error(error.message || '网络错误')
      }
    } else {
      window.$message?.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)
```

---

### 补充7: Sa-Token 配置优化

**修改文件**: `application.yml`

```yaml
# Sa-Token 配置（增强版）
sa-token:
  # Token 名称
  token-name: Authorization
  # Token 有效期（7天，单位秒）
  timeout: 604800
  # Token 风格
  token-style: uuid
  # 是否允许同一账号多地同时登录
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个 Token
  is-share: false
  # 是否输出操作日志
  is-log: false
  # Token 临时有效期（30分钟无操作则过期，-1表示不限制）
  activity-timeout: 1800
  # 是否允许同一账号多次登录，为true时每次登录会生成新Token
  is-concurrent: true
```

---

## 📦 附录A: 数据迁移脚本

> **重要**: 以下脚本需要在部署新版本前执行，请按顺序依次运行。

### A.1 创建用户表

```sql
-- Step 1: 创建用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
    `avatar_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色:user/admin',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态:0禁用/1启用',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_username` (`username`) USING BTREE,
    INDEX `idx_role` (`role`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- Step 2: 插入默认管理员账号 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin');
```

### A.2 修改 practice_record 表

```sql
-- Step 3: 添加 user_id 字段（先设为可空）
ALTER TABLE `practice_record` 
ADD COLUMN `user_id` BIGINT NULL COMMENT '用户ID' AFTER `id`;

-- Step 4: 将历史记录关联到管理员账号（假设管理员 id=1）
UPDATE `practice_record` SET `user_id` = 1 WHERE `user_id` IS NULL;

-- Step 5: 修改为非空
ALTER TABLE `practice_record` 
MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID';

-- Step 6: 添加索引和外键
ALTER TABLE `practice_record` 
ADD INDEX `idx_user_id` (`user_id` ASC);

ALTER TABLE `practice_record` 
ADD CONSTRAINT `fk_practice_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
```

### A.3 修改 question 表

```sql
-- Step 7: 添加 owner_id 字段
ALTER TABLE `question` 
ADD COLUMN `owner_id` BIGINT NULL COMMENT '题目所属用户ID(NULL表示公共题库)' AFTER `id`;

-- Step 8: 添加索引
ALTER TABLE `question` 
ADD INDEX `idx_owner_id` (`owner_id` ASC);

-- Step 9: 历史题目设为公共题库（保持 NULL）或关联管理员
-- 如果需要关联管理员：
-- UPDATE `question` SET `owner_id` = 1 WHERE `owner_id` IS NULL;
```

### A.4 创建用户题目统计表

```sql
-- Step 10: 创建 user_question_stats 表
CREATE TABLE `user_question_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `practice_count` INT NOT NULL DEFAULT 0 COMMENT '练习次数',
    `wrong_count` INT NOT NULL DEFAULT 0 COMMENT '错误次数',
    `is_marked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `last_practice_time` DATETIME NULL DEFAULT NULL COMMENT '最后练习时间',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_question` (`user_id`, `question_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_question_id` (`question_id`) USING BTREE,
    INDEX `idx_wrong_count` (`wrong_count`) USING BTREE,
    CONSTRAINT `fk_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_stats_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户题目统计表';
```

### A.5 创建日志表

```sql
-- Step 11: 创建用户登录日志表
CREATE TABLE `user_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `login_ip` VARCHAR(50) NULL COMMENT '登录IP',
    `login_location` VARCHAR(255) NULL COMMENT '登录地点',
    `browser` VARCHAR(100) NULL COMMENT '浏览器类型',
    `os` VARCHAR(100) NULL COMMENT '操作系统',
    `user_agent` VARCHAR(500) NULL COMMENT 'User-Agent',
    `login_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '登录状态:0失败/1成功',
    `fail_reason` VARCHAR(255) NULL COMMENT '失败原因',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_login_time` (`login_time`) USING BTREE,
    INDEX `idx_login_ip` (`login_ip`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志表';

-- Step 12: 创建用户操作日志表
CREATE TABLE `user_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_desc` VARCHAR(500) NULL COMMENT '操作描述',
    `operation_data` JSON NULL COMMENT '操作数据',
    `request_ip` VARCHAR(50) NULL COMMENT '请求IP',
    `request_url` VARCHAR(255) NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) NULL COMMENT '请求方法',
    `operation_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作状态',
    `error_msg` TEXT NULL COMMENT '错误信息',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_operation_type` (`operation_type`) USING BTREE,
    INDEX `idx_operation_time` (`operation_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户操作日志表';
```

---

## 📦 附录B: Subject 表设计决策

### 决策说明

**问题**: `subject` 表目前是全局共享的，是否需要按用户隔离？

**决策**: 保持 Subject 全局共享（推荐）

**原因**:
1. 科目本质上是分类标签，不涉及用户隐私
2. 避免不同用户创建同名科目导致混乱
3. 简化实现，减少代码改动量
4. 通过 `question.owner_id` 已实现题目隔离，科目作为标签共享是合理的

**影响**:
- 用户创建的新科目其他用户也可见
- 科目的 `question_count` 统计的是所有用户的题目数

如果未来需要按用户隔离科目，可以再添加 `owner_id` 字段。

---

## ✅ 审查完成确认

| 问题编号 | 问题描述 | 解决方案 | 状态 |
|----------|----------|----------|------|
| 1 | 导出功能缺少用户过滤 | 补充4 | ✅ |
| 2 | Subject 表设计 | 附录B（保持现状） | ✅ |
| 3 | StpInterfaceImpl 缺失 | 补充2 | ✅ |
| 4 | Layout 用户信息硬编码 | 补充5 | ✅ |
| 5 | 全局异常处理器缺失 | 补充1 | ✅ |
| 6 | 密码返回前端 | 补充3 | ✅ |
| 7 | Token 刷新机制 | 补充7 | ✅ |
| 8 | 前端错误处理 | 补充6 | ✅ |
| 9 | 数据迁移脚本 | 附录A | ✅ |

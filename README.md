<div align="center">

# 📚 Exam Practice System

### ✨ 智能期末复习题库系统
**[ Vue 3 | Spring Boot | MyBatis-Plus | Naive UI ]**

<p>
  <img src="https://img.shields.io/badge/Vue-3.4.0-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.5-red?style=for-the-badge" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Naive%20UI-2.38-18a058?style=for-the-badge" />
</p>

<p>
  <img src="https://img.shields.io/badge/Maintained%3F-Yes-green.svg?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" />
  <img src="https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=for-the-badge" />
</p>

一个现代化的**期末复习在线题库系统**，支持题目管理、智能练习、错题本、统计分析等功能。<br>
采用前后端分离架构，提供流畅的学习体验和强大的题库管理能力。

[核心功能](#-核心功能) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [项目结构](#-项目结构)

---
</div>

## 🌟 核心功能

<table>
  <tr>
    <td width="50%">

### 📝 题目管理
- ✅ 题目增删改查（CRUD）
- 📊 分页查询与多条件筛选
- 🏷️ 支持单选、多选、判断题
- 📁 科目分类管理
- ⭐ 题目收藏标记
- 📈 练习次数与错题统计

</td>
    <td width="50%">

### 📥 Excel 导入导出
- 📤 批量导入题目
- 📊 Excel模板下载
- 🔄 选择题与判断题分别导入
- ✅ 数据校验与错误提示
- 📑 题目导出功能
- 🎯 支持自定义字段映射

</td>
  </tr>
  <tr>
    <td width="50%">

### 🎯 智能练习
- 🎲 随机抽题练习
- 🔍 按科目/题型筛选
- ⚡ 实时答题反馈
- 💡 答案解析展示
- 📊 答题进度追踪
- 🎨 精美的答题界面

</td>
    <td width="50%">

### 📖 错题本与统计
- ❌ 错题自动收录
- 📋 错题列表分页展示
- 🔄 错题重新练习
- 📊 练习统计分析
- 📈 正确率趋势图
- 🎯 薄弱知识点分析

</td>
  </tr>
</table>

## 🏗️ 技术栈

### 后端技术

```text
Spring Boot 2.7.18 │ Java应用框架
MyBatis-Plus 3.5.5 │ 增强型ORM框架
MySQL 8.0          │ 关系型数据库
EasyExcel 3.3.2    │ Excel处理工具
Lombok             │ Java代码简化工具
Hutool 5.8.23      │ Java工具类库
Maven              │ 项目构建工具
```

**核心特性：**
- 🚀 RESTful API 设计
- 📦 统一结果封装
- 📄 分页查询支持
- 🔄 跨域配置
- 📝 自动代码生成

### 前端技术

```text
Vue 3.4            │ 渐进式JavaScript框架
Vite 5.0           │ 下一代前端构建工具
Naive UI 2.38      │ Vue 3 UI组件库
Vue Router 4.x     │ 官方路由管理器
Pinia 2.x          │ 新一代状态管理
Axios              │ HTTP客户端
XLSX               │ Excel处理库
```

**核心特性：**
- ⚡ Vite 极速开发体验
- 🎨 Naive UI 精美组件
- 📱 响应式设计
- 🔄 组件化开发
- 💾 持久化状态管理

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Node.js**: 16+
- **npm**: 8+

### 1️⃣ 数据库配置

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS exam_practice 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

执行SQL建表脚本（位于 `backend/src/main/resources/sql/` 目录）：

```sql
-- question表
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(50) NOT NULL COMMENT '题型',
    subject VARCHAR(100) NOT NULL COMMENT '科目',
    content TEXT NOT NULL COMMENT '题目内容',
    options JSON COMMENT '选项（JSON数组）',
    answer VARCHAR(200) NOT NULL COMMENT '答案',
    analysis TEXT COMMENT '解析',
    difficulty VARCHAR(20) DEFAULT 'medium' COMMENT '难度',
    is_marked TINYINT(1) DEFAULT 0 COMMENT '是否收藏',
    practice_count INT DEFAULT 0 COMMENT '练习次数',
    wrong_count INT DEFAULT 0 COMMENT '错误次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- practice_record表
CREATE TABLE practice_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    user_answer VARCHAR(200) COMMENT '用户答案',
    is_correct TINYINT(1) COMMENT '是否正确',
    practice_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
    FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习记录表';

-- subject表（可选）
CREATE TABLE subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '科目名称',
    description VARCHAR(500) COMMENT '科目描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';
```

### 2️⃣ 后端启动

#### 方式一：使用 IDE（推荐）

1. 用 IntelliJ IDEA 打开 `backend` 目录
2. 修改 `src/main/resources/application-dev.yml` 数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_practice?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的数据库用户名
    password: your_password # 修改为你的数据库密码
```

3. 运行 `ExamApplication.java` 的 main 方法
4. 看到以下提示表示启动成功：

```
=================================
期末复习题库系统启动成功！
访问地址: http://localhost:8081
=================================
```

#### 方式二：使用命令行

```bash
cd backend
mvn clean package
java -jar target/exam-practice-0.0.1.jar
```

#### 方式三：使用启动脚本（Windows）

```bash
# 双击运行根目录下的 启动后端.bat
启动后端.bat
```

### 3️⃣ 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:5173

### 4️⃣ 访问系统

- **前端地址**: http://localhost:5173
- **后端接口**: http://localhost:8081/api
- **题目管理**: http://localhost:5173/questions
- **开始练习**: http://localhost:5173/practice

## 📁 项目结构

```
Final_Practice/
├── README.md                           # 项目说明文档
├── 启动后端.bat                        # Windows后端启动脚本
├── 习思想题库_20251219_134241.xlsx    # 示例题库数据
├── .gitignore                          # Git忽略配置
│
├── backend/                            # 后端项目 ✅
│   ├── pom.xml                         # Maven配置文件
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/exam/
│   │   │   │   ├── ExamApplication.java          # 启动类
│   │   │   │   ├── config/                       # 配置类
│   │   │   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   │   │   └── MybatisPlusConfig.java   # MyBatis-Plus配置
│   │   │   │   ├── controller/                   # 控制器层
│   │   │   │   │   ├── QuestionController.java  # 题目管理API
│   │   │   │   │   ├── PracticeController.java  # 练习功能API
│   │   │   │   │   ├── ImportController.java    # 导入导出API
│   │   │   │   │   └── TestController.java      # 测试API
│   │   │   │   ├── entity/                       # 实体类
│   │   │   │   │   ├── Question.java            # 题目实体
│   │   │   │   │   ├── PracticeRecord.java      # 练习记录实体
│   │   │   │   │   └── Subject.java             # 科目实体
│   │   │   │   ├── mapper/                       # MyBatis Mapper
│   │   │   │   │   ├── QuestionMapper.java
│   │   │   │   │   ├── PracticeRecordMapper.java
│   │   │   │   │   └── SubjectMapper.java
│   │   │   │   ├── service/                      # 业务层
│   │   │   │   │   ├── QuestionService.java
│   │   │   │   │   ├── PracticeRecordService.java
│   │   │   │   │   └── impl/                     # 业务实现
│   │   │   │   ├── dto/                          # 数据传输对象
│   │   │   │   │   ├── QuestionImportDTO.java
│   │   │   │   │   ├── JudgeQuestionImportDTO.java
│   │   │   │   │   └── QuestionExportDTO.java
│   │   │   │   ├── listener/                     # Excel监听器
│   │   │   │   │   ├── ChoiceQuestionImportListener.java
│   │   │   │   │   └── JudgeQuestionImportListener.java
│   │   │   │   └── common/                       # 通用类
│   │   │   │       ├── Result.java              # 统一返回结果
│   │   │   │       └── PageResult.java          # 分页结果
│   │   │   └── resources/
│   │   │       ├── application.yml               # 主配置文件
│   │   │       ├── application-dev.yml           # 开发环境配置
│   │   │       ├── logback-spring.xml            # 日志配置
│   │   │       └── mapper/                       # MyBatis XML映射
│   │   └── test/                                 # 测试目录
│   └── target/                                   # 编译输出目录
│
└── frontend/                           # 前端项目 ✅
    ├── package.json                    # 项目依赖配置
    ├── vite.config.js                  # Vite构建配置
    ├── index.html                      # HTML入口文件
    └── src/
        ├── main.js                     # 应用入口
        ├── App.vue                     # 根组件
        ├── api/                        # API接口
        │   ├── request.js              # Axios封装
        │   ├── question.js             # 题目相关接口
        │   └── practice.js             # 练习相关接口
        ├── router/                     # 路由配置
        │   └── index.js                # 路由定义
        ├── stores/                     # Pinia状态管理
        │   └── practice.js             # 练习状态
        ├── views/                      # 页面组件
        │   ├── Layout.vue              # 布局组件
        │   ├── Home.vue                # 首页
        │   ├── QuestionManage.vue      # 题目管理页
        │   ├── QuestionConverter.vue   # 题目转换工具
        │   ├── Practice.vue            # 练习页面
        │   ├── WrongBook.vue           # 错题本
        │   └── Statistics.vue          # 统计分析
        ├── components/                 # 公共组件
        └── assets/                     # 静态资源
```

## 📡 API 接口

### 题目管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/questions` | GET | 分页查询题目列表 |
| `/api/questions/{id}` | GET | 获取题目详情 |
| `/api/questions` | POST | 新增题目 |
| `/api/questions/{id}` | PUT | 更新题目 |
| `/api/questions/{id}` | DELETE | 删除题目 |
| `/api/questions/batch` | DELETE | 批量删除题目 |
| `/api/questions/random` | GET | 随机获取题目 |

### 练习功能接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/practice/submit` | POST | 提交答题记录 |
| `/api/practice/wrong` | GET | 获取错题列表 |
| `/api/practice/statistics` | GET | 获取练习统计 |
| `/api/practice/records` | GET | 获取练习记录 |

### 导入导出接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/import/choice` | POST | 导入选择题 |
| `/api/import/judge` | POST | 导入判断题 |
| `/api/import/template/choice` | GET | 下载选择题模板 |
| `/api/import/template/judge` | GET | 下载判断题模板 |
| `/api/export` | POST | 导出题目 |

## 🎨 功能截图

### 题目管理
- 支持题目的增删改查
- 多条件筛选（科目、题型、难度）
- 分页展示与批量操作

### 智能练习
- 优雅的答题界面
- 实时反馈与解析
- 进度追踪

### 错题本
- 自动收录错题
- 支持错题重练
- 按科目分类展示

### 统计分析
- 练习次数统计
- 正确率分析
- 知识点掌握情况

## 🔧 开发指南

### 后端开发

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 前端开发

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器（热重载）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### 代码规范

- **后端**: 遵循阿里巴巴Java开发手册
- **前端**: 使用Vue 3 Composition API
- **命名**: 使用有意义的变量和方法名
- **注释**: 关键逻辑必须添加注释

## 📝 使用说明

### 1. 导入题目

1. 访问「题目管理」页面
2. 点击「导入题目」按钮
3. 选择题目类型（选择题/判断题）
4. 下载对应的Excel模板
5. 按照模板格式填写题目
6. 上传Excel文件完成导入

### 2. 开始练习

1. 访问「开始练习」页面
2. 选择科目和题型
3. 点击「开始专注练习」
4. 答题并查看解析
5. 错题自动收录到错题本

### 3. 查看错题

1. 访问「错题本」页面
2. 查看所有答错的题目
3. 支持按科目和题型筛选
4. 可以重新练习错题

### 4. 统计分析

1. 访问「统计分析」页面
2. 查看练习次数和正确率
3. 分析薄弱知识点
4. 制定复习计划

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建新分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👨‍💻 作者

- **开发者**: IceYuanyyy
- **邮箱**: 2478686497@qq.com / ercurym86@gmail.com
- **GitHub**: [@IceYuanyyy](https://github.com/IceYuanyyy)

## ⭐ Star History

如果这个项目对你有帮助，请给一个 Star ⭐️

## 📮 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 📧 Email: 2478686497@qq.com / ercurym86@gmail.com
- 💬 GitHub Issues: [提交Issue](https://github.com/IceYuanyyy/Practice_Review/issues)

---

<div align="center">

**[⬆ 返回顶部](#-exam-practice-system)**

Made with ❤️ by IceYuanyyy

</div>

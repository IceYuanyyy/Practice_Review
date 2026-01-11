<div align="center">

# ⚡ 期末复习系统
 
 ### ✨ 智能在线刷题平台
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

[核心功能](#-核心功能) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [更新日志](#-更新日志) • [项目结构](#-项目结构)

---
</div>

## 📅 更新日志

<details open>
<summary>v1.1.4 (2026-01-11) · 🎨 视觉革命 & 🌓 昼夜交替 & 🔐 导入防护 & 📊 档案追踪</summary>

- **🎨 后台与视觉进化 (UI/UX)**:
  - **Rogue's Gallery**: 用户管理页全面升级为 "恶人榜" (Admin Comic) 风格，采用 Bangers 字体与复古漫画网格布局
  - **汽化动画**: 实现用户删除时的 "Vaporize" 汽化动效，配合位移、旋转与色彩偏移，提升后台操作爽感
  - **🌓 昼夜模式**: 基于 `Pinia` + `Naive UI` 实现全站深色模式切换，适配 20+ 组件配色并支持持久化
  - **视觉优化**: 针对手绘风 (Comic Style) 进行专项适配，优化深色模式下的阴影、边框与对比度
- **📁 转换全链路追踪 (Backend/Frontend)**:
  - **转换日志存储**: `ImportController` 新增 `convert-log` 接口，支持 Base64 格式的原始 TXT 与转换结果 Excel 入库追踪
  - **双向追溯**: 支持管理员从操作日志直接下载题目转换的原始文本与生成的 Excel 文件
  - **多端持久化**: 题库转换逻辑集成日志保存机制，确保每一批次导入都有迹可循
- **📊 个人练习与管理员侦查 (Feature)**:
  - **用户档案库 (Dossier)**: 管理员可一键调阅任意用户的练习记录流水，包含答题时间、科目、题型及判定结果
  - **精细化回顾**: "统计分析"新增 "最近练习记录" 表格，支持查看历史每一题的用时、答案与结果
- **🔐 导入防护与权限优化 (Security/Core)**:
  - **重名防护**: 导入题目时新增 SQL 级别校验，禁止在同一用户的同一题库下导入同名科目（基于 `owner_id` 隔离）
  - **命名冲突保护**: 导入题目时自动校验 "题库名称" 唯一性，防止误操作导致的题库混淆
  - **权限修正**: 修复管理员无法在该批次导出记录页面下载普通用户导入文件的权限逻辑问题
- **📢 强制公告阅读 (Feature)**:
  - **仪式感确认**: 用户登录后自动弹窗展示未读公告，强制标记已读确保重要通知 100% 触达
  - **智能筛选**: `QuestionManage` 实现 "导入用户 -> 归属科目" 级联过滤，管理员操作更精准
- **🐛 稳定性与体验修复**:
  - **白屏风险修复**: 解决 `UserManage.vue` 因动态导入失败导致的系统风险，优化 Vite 构建缓存
  - **错题本上下文**: 修复错题练习模式下 "重置/退出" 导致的模式丢失及答题卡实时渲染 bug
  - **🖼️ 题目管理**: `POST /api/questions` 正式支持 `imageUrl` 字段，全自动图片展示流程

</details>

<details>
<summary>v1.1.3 (2026-01-09) · 🔐 权限修复 &题库加载修复 & 🎯 UI 精简</summary>

- **🐛 核心问题修复 (Backend)**:
  - **公共题库加载优化**: 修复管理员点击"公共题库"后科目列表为空的问题
  - **题库切换修复**: 解决"公共题库"与"我的题库"反复切换导致数据重置的bug
  - **权限逻辑调整**: 优化 `SubjectController` 中 `ownerId=-1` 的处理逻辑，管理员选择公共题库时查看所有题目
- **🖼️ 题目图片支持 (Feature)**:
  - **全链路打通**: 实现题目图片 URL 从数据库存储到前端展示的完整流程
  - **TXT 智能转换**: `QuestionConverter` 增加 `图片：[URL]` 语法支持，自动解析并转换为 Excel 列
  - **Excel 进阶导出**: 题库导出及操作日志导出新增 "图片URL" 字段，支持批量管理
  - **沉浸式展示**: 在练习模式 (`Practice`) 与错题本 (`WrongBook`) 中集成 `n-image` 组件，支持图片预览与自适应显示
  - **管理增强**: `QuestionManage` 表格新增图片预览列，编辑弹窗支持手动维护图片 URL
- **🧹 代码清理 (Frontend)**:
  - **前端报错修复**: 修复 `Practice.vue` 中 `NImage` 未导入导致的图片显示异常背景，并删除对不存在函数 `loadLastFilter()` 的调用
  - **UI 简化**: 移除题库来源处冗余的"或选择其他来源"下拉框，仅保留核心的"公共题库"和"我的题库"按钮
- **📦 系统稳定性**:
  - 提高题库加载稳定性，消除前端控制台错误
  - 优化管理员题库管理体验
  - 完善数据库初始化脚本 `exam_practice.sql` 及更新脚本 `update.sql`

</details>

<details>
<summary>v1.1.2 (2026-01-08) · 🎨 视觉进化 & ⚡ 系统增强</summary>

- **🎨 UI 风格焕新 (Frontend)**:
  - **手绘风公告栏**: 重构通知面板为 `Bulletin Board` 风格，采用格纹软木板背景 + `Patrick Hand` 手写字体
  - **便利贴交互**: 消息卡片升级为随机色系 (黄/蓝/粉) 的便利贴 (Sticky Notes)，支持 "透明胶带" 粘贴效果
  - **视觉细节**: 新增 "📌 置顶" 红色印章、"NEW!" 动态角标及手绘风 Tab 切换页签
- **📧 邮箱验证升级 (Auth)**:
  - **模板重制**: 邮件模板升级为 Comic/Industrial 漫画工业风，集成 CSS 胶带效果与 bouncing 动画
  - **体验优化**: 验证码改为大号等宽字体 + 虚线框设计，支持双击复制；修复 HTML 模板中 CSS 百分号转义问题
  - **强制流程**: 完善 `is_email_verified` 状态检查，未验证用户强制引导绑定
- **🌍 核心业务增强 (System)**:
  - **IP 归属地追踪**: 新增 `IpUtil` 多源 API，登录/操作日志自动记录省市级归属地，支持缓存与容错
  - **题库转换存档**: TXT→Excel 转换全量入库，支持后台下载原始/生成文件 (`sql/add_convert_file_fields.sql`)
- **🛠️ 稳定性与架构**:
  - **SQL 架构合并**: 合并 `announcement` 表结构至主 SQL 文件，精简维护成本
  - **数据一致性**: 题目编辑支持 6 选项 (E/F) 持久化；错题清空同步刷新 `wrong_book`
  - **问题修复**: 修正练习重置逻辑与答题卡样式，优化置顶公告排序算法
  - **样式修复**: 公告详情弹窗升级为 Admin Comic 风格（Bangers 字体 + 纯白背景），修复内容截断与 CSS 编译错误

</details>

<details>
<summary>v1.1.1 (2026-01-07) · 🌟 沉浸式体验升级</summary>

- **🚀 启动特效 & 沉浸体验**:
  - 新增全屏"火箭发射"启动动画，点击 "INJECT ENERGY" 为学习充能
  - **沉浸式练习**: 全新"手绘笔记本"风格 UI，还原真实书写质感
  - **动态侧边栏**: 整合答题卡、状态栏与专注控制，支持响应式布局
  - **手绘弹窗**: 自定义 Doodle 风格的确认/提示交互
- **🧘 禅模式 2.0**:
  - 支持浏览器全屏沉浸 (Fullscreen API)
  - 新增"防走神"监测（鼠标移出/页面失焦震动提醒）与视觉反馈
- **💄 界面与样式优化**:
  - 优化错题本 (WrongBook) 卡片样式，增加科目标签显示
  - 重构练习页 "当前状态"面板，新增笔记本风格主题
  - 修复错题练习模式下科目名称显示不正确的问题
- **🐛 核心问题修复**:
  - 重置轮次时同步清理当前轮次的练习记录，确保答题卡状态与历史彻底归零
  - 修复练习页答题卡跳转导致已做数量异常累加的问题

</details>

<details>
<summary>v1.1.0 (2026-01-06) · 展开查看</summary>

- **✨ 新增特性**:
  - 全新"知识粒子"鼠标特效 (📚, ✏️, ∑)，营造沉浸式学习氛围
  - 恢复经典鼠标点击特效 (❤️) 和背景樱花飘落效果 (🌸)
  - 优化全局交互体验
- **🐛 问题修复**:
  - 修复统计看板 (Statistics.vue) 中 ECharts 图表溢出容器的布局问题
  - 修复全局 Canvas 样式冲突导致图表显示异常的问题

</details>


## 🌟 核心功能

<details>
<summary>点击展开核心功能</summary>

<table>
  <tr>
    <td width="50%">

### 📝 题目管理
- ✅ 题目增删改查（CRUD）
- 📊 分页查询与多条件筛选
- 🏷️ 支持单选题、多选题、判断题
- 📁 科目分类自动管理
- 💾 科目题目数量自动统计
- ⭐ 题目收藏标记
- 📈 练习次数与错题统计
- 🎯 导入时可自定义科目名称
- 🖼️ 支持题目图片显示与管理

</td>
    <td width="50%">

### 📥 Excel 导入导出
- 📤 批量导入题目（支持自定义科目）
- 📊 Excel模板下载
- 🔄 选择题与判断题分别导入
- ✅ 数据校验与错误提示
- 📑 题目导出功能
- 🎯 支持自定义字段映射
- 🏷️ 导入时动态创建科目
- 📈 自动同步科目题目数量

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
- 🔄 轮次刷题模式
- 🎨 精美的答题界面
- 📝 错题本专项练习
- 🌓 昼夜模式切换与随心切换持久化

</td>
    <td width="50%">

### 📖 错题本与统计
- ❌ 错题自动收录到错题本
- 📋 错题列表分页展示
- 🔄 错题重新练习
- 📚 按科目分类复习错题
- 🎯 错题专项练习模式
- ✅ 标记已掌握移除错题
- 📊 练习统计分析
- 📈 正确率趋势图
- 🎯 薄弱知识点分析

</td>
  </tr>
  <tr>
    <td width="50%">

### 🔐 用户认证
- 📝 用户注册与登录
- 🔒 Sa-Token 权限框架
- 🎫 Token 身份认证
- 👤 个人中心管理
- 🔑 密码 BCrypt 加密
- 🛡️ 用户数据隔离
- 📧 邮箱验证与绑定流程

</td>
    <td width="50%">

### 🛡️ 管理员后台
- 👥 用户管理（CRUD）
- 📊 系统统计仪表盘
- 📋 登录日志查询
- 📝 操作日志查询（含导入/转换文件下载）
- 🔍 用户练习记录全量穿透查看
- 🔒 基于角色的访问控制
- ⚙️ 系统健康监控

</td>
  </tr>
</table>

</details>

## 🏗️ 技术栈

<details>
<summary>点击展开技术栈</summary>

### 后端技术

```text
Spring Boot 2.7.18 │ Java应用框架
MyBatis-Plus 3.5.5 │ 增强型ORM框架
MySQL 8.0          │ 关系型数据库
Sa-Token 1.37.0    │ 权限认证框架
EasyExcel 3.3.2    │ Excel处理工具
Lombok             │ Java代码简化工具
Hutool 5.8.23      │ Java工具类库
Maven              │ 项目构建工具
```

**核心特性：**
- 🚀 RESTful API 设计
- 📦 统一结果封装
- 📄 分页查询支持（最大1000条）
- 🔄 跨域配置
- 📝 自动代码生成
- 🏷️ 科目表自动管理
- 📊 题目统计自动更新
- ⚡ 数据库索引优化
- 🛡️ 文件上传安全校验
- 🔐 Token 身份认证
- 👥 用户数据隔离

### 前端技术

```text
Vue 3.4            │ 渐进式JavaScript框架
Vite 5.0           │ 下一代前端构建工具
Naive UI 2.38      │ Vue 3 UI组件库
Vue Router 4.x     │ 官方路由管理器
Pinia 2.x          │ 新一代状态管理
Axios              │ HTTP客户端
XLSX               │ Excel处理库
pinia-plugin-persistedstate │ 状态持久化
```

**核心特性：**
- ⚡ Vite 极速开发体验
- 🎨 Naive UI 精美组件
- 📱 响应式设计
- 🔄 组件化开发
- 💾 持久化状态管理

</details>

## 🚀 快速开始

<details>
<summary>点击展开快速开始</summary>

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

执行项目根目录下的 SQL 初始化脚本：`sql/exam_practice.sql`

> 📌 如果从旧版本升级，请额外执行迁移脚本：`sql/add_convert_file_fields.sql`（为操作日志增加转换文件字段）

**该脚本将自动创建以下数据表：**
- `user`: 用户表（包含管理员和普通用户）
- `question`: 题目表
- `subject`: 科目表
- `practice_record`: 练习记录表
- `practice_round`: 练习轮次表
- `user_question_stats`: 用户刷题统计表
- `wrong_book`: 错题本表
- `user_login_log`: 登录日志表
- `user_operation_log`: 操作日志表

> ⚠️ **注意**: 脚本中包含初始管理员账号 `admin` / `admin123` 和部分测试数据。

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
- **登录页面**: http://localhost:5173/login
- **个人中心**: http://localhost:5173/profile
- **题目管理**: http://localhost:5173/questions
- **开始练习**: http://localhost:5173/practice
- **管理后台**: http://localhost:5173/admin（需管理员权限）

### 5️⃣ 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 拥有所有权限，可访问管理后台 |

> 💡 **提示**: 首次使用请通过注册页面创建普通用户账号

</details>

## 📁 项目结构

```
Final_Practice/
├── README.md                           # 项目说明文档
├── API.md                              # API接口文档
├── User_Guide.md                       # 用户使用手册
├── NormalConverter.md                  # 题目转换说明
├── 启动系统.bat                        # Windows系统启动脚本
├── .gitignore                          # Git忽略配置
│
├── backend/                            # 后端项目 ✅
│   ├── pom.xml                         # Maven配置文件
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/exam/
│   │   │   │   ├── ExamApplication.java          # 启动类
│   │   │   │   ├── annotation/                   # 自定义注解
│   │   │   │   ├── aspect/                       # 切面配置
│   │   │   │   ├── config/                       # 配置类
│   │   │   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   │   │   ├── MybatisPlusConfig.java    # MyBatis-Plus配置
│   │   │   │   │   ├── SaTokenConfig.java        # Sa-Token配置
│   │   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │   ├── controller/                   # 控制器层
│   │   │   │   │   ├── AuthController.java       # 认证API
│   │   │   │   │   ├── AnnouncementController.java # 公告API
│   │   │   │   │   ├── AdminController.java      # 管理员API
│   │   │   │   │   ├── QuestionController.java   # 题目管理API
│   │   │   │   │   ├── PracticeController.java   # 练习功能API
│   │   │   │   │   ├── ImportController.java     # 导入导出API
│   │   │   │   │   └── SubjectController.java    # 科目管理API
│   │   │   │   ├── entity/                       # 实体类
│   │   │   │   │   ├── User.java                 # 用户实体
│   │   │   │   │   ├── Question.java             # 题目实体
│   │   │   │   │   ├── Announcement.java         # 公告实体
│   │   │   │   │   ├── PracticeRecord.java       # 练习记录实体
│   │   │   │   │   └── ...                       # 其他实体
│   │   │   │   ├── mapper/                       # MyBatis Mapper
│   │   │   │   ├── service/                      # 业务层
│   │   │   │   ├── dto/                          # 数据传输对象
│   │   │   │   ├── listener/                     # Excel监听器
│   │   │   │   ├── util/                         # 工具类
│   │   │   │   └── common/                       # 通用类
│   │   │   └── resources/
│   │   │       ├── application.yml               # 主配置文件
│   │   │       └── mapper/                       # MyBatis XML映射
│   │   └── test/                                 # 测试目录
│   └── target/                                   # 编译输出目录
│
├── frontend/                           # 前端项目 ✅
│   ├── package.json                    # 项目依赖配置
│   ├── vite.config.js                  # Vite构建配置
│   ├── index.html                      # HTML入口文件
│   └── src/
│       ├── main.js                     # 应用入口
│       ├── App.vue                     # 根组件
│       ├── api/                        # API接口
│       │   ├── auth.js                 # 认证相关接口
│       │   ├── announcement.js         # 公告相关接口
│       │   ├── admin.js                # 管理员相关接口
│       │   ├── question.js             # 题目相关接口
│       │   └── practice.js             # 练习相关接口
│       ├── router/                     # 路由配置
│       ├── stores/                     # Pinia状态管理
│       │   ├── user.js                 # 用户状态
│       │   ├── theme.js                # 主题/昼夜状态
│       │   └── practice.js             # 练习状态
│       ├── views/                      # 页面组件
│       │   ├── Home.vue                # 首页
│       │   ├── Login.vue               # 登录页
│       │   ├── Register.vue            # 注册页
│       │   ├── Profile.vue             # 个人中心
│       │   ├── QuestionManage.vue      # 题目管理页
│       │   ├── QuestionConverter.vue   # 题目转换工具
│       │   ├── Practice.vue            # 练习页面
│       │   ├── WrongBook.vue           # 错题本
│       │   ├── Statistics.vue          # 统计分析
│       │   ├── VerifyEmail.vue         # 邮箱验证页
│       │   └── admin/                  # 管理后台
│       │       ├── Dashboard.vue       # 仪表盘
│       │       ├── UserManage.vue      # 用户管理
│       │       ├── AnnouncementManage.vue # 公告管理
│       │       └── OperationLogs.vue   # 操作日志
│       ├── components/                 # 公共组件
│       ├── utils/                      # 工具函数
│       └── assets/                     # 静态资源
│
├── data/                               # 题库数据与标准 ✅
├── scripts/                            # 自动化脚本 ✅
└── sql/                                # 数据库脚本 ✅
```

## 📡 API 接口

> 完整的 API 文档请参阅 [API.md](API.md)

### 🔐 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/user` | GET | 获取当前用户信息 |
| `/api/auth/check` | GET | 检查登录状态 |
| `/api/auth/send-code` | POST | 发送注册验证码 |
| `/api/auth/bind-email` | POST | 绑定/验证邮箱 |

### 👤 用户中心接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/profile` | GET | 获取个人资料 |
| `/api/user/profile` | PUT | 更新个人资料 |
| `/api/user/password` | PUT | 修改密码 |

### 🛡️ 管理员接口（需 admin 角色）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/statistics` | GET | 系统统计数据 |
| `/api/admin/users` | GET | 用户列表（分页） |
| `/api/admin/users/{id}` | GET | 用户详情 |
| `/api/admin/users/{id}/status` | PUT | 修改用户状态 |
| `/api/admin/users/{id}/reset-password` | PUT | 重置用户密码 |
| `/api/admin/users/{id}` | DELETE | 删除用户 |
| `/api/admin/login-logs` | GET | 登录日志（分页） |
| `/api/admin/operation-logs` | GET | 操作日志（分页） |

### 📝 题目管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/questions` | GET | 分页查询题目列表 |
| `/api/questions/{id}` | GET | 获取题目详情 |
| `/api/questions` | POST | 新增题目 |
| `/api/questions/{id}` | PUT | 更新题目 |
| `/api/questions/{id}` | DELETE | 删除题目 |
| `/api/questions/batch` | DELETE | 批量删除题目 |
| `/api/questions/random` | GET | 随机获取题目 |
| `/api/questions/clear` | DELETE | 清空题库 |
| `/api/questions/{id}/mark` | PUT | 标记/取消收藏 |

### 📂 科目管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/subjects` | GET | 获取所有科目列表 |
| `/api/subjects/recount` | POST | 重新统计科目题目数量 |

### 🎯 练习功能接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/practice/submit` | POST | 提交答题记录 |
| `/api/practice/wrong` | GET | 获取错题列表 |
| `/api/practice/statistics` | GET | 获取练习统计 |
| `/api/practice/records` | GET | 获取练习记录 |

### 📥 导入导出接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/import/excel` | POST | 导入Excel题目 |
| `/api/import/template/choice` | GET | 下载选择题模板 |
| `/api/import/template/judge` | GET | 下载判断题模板 |
| `/api/export` | POST | 导出题目 |
| `/api/export/all` | GET | 导出所有可见题目 |
| `/api/import/convert-log` | POST | 保存转换日志 |

## 🎨 功能截图

### 题目管理
- 支持题目的增删改查
- 多条件筛选（科目、题型、难度）
- 分页展示与批量操作
- 科目动态加载与自动统计
- 支持单选题、多选题、判断题三种题型

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

<details>
<summary>点击展开使用说明</summary>

### 1. 导入题目

1. 访问「题目管理」页面
2. 点击「批量导入」按钮
3. 在导入对话框中选择或输入科目名称（支持筛选和新建）
4. 选择Excel文件进行上传
5. 系统自动识别题目类型（单选/多选/判断）
6. 导入成功后自动更新科目列表和题目统计

**注意事项：**
- Excel文件必须包含：题目内容、选项（选择题）、答案等字段
- 单选题答案为单个字母（如A）
- 多选题答案为多个字母（如ABC）
- 判断题答案为"正确"或"错误"
- 导入时会自动创建新科目（如果不存在）

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

</details>

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

**[⬆ 返回顶部](#)**

Made with ❤️ by IceYuanyyy

</div>

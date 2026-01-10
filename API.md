# 📡 Exam Practice System API 文档

> 本文档详细列出了系统的后端接口，包括请求地址、请求方式、参数说明及认证要求。
> 所有接口的前缀均为 `/api`。

---

## 🔐 认证模块 (Auth)

### 1. 用户登录
- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Auth**: 无需认证
- **Body**:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
- **Response**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1Ni...",
      "user": {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "role": "admin",
        "avatarUrl": "..."
      }
    }
  }
  ```

### 2. 用户注册
- **URL**: `/api/auth/register`
- **Method**: `POST`
- **Auth**: 无需认证
- **Body**:
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "nickname": "新用户"
  }
  ```
- **Response**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": { ...user info... }
  }
  ```

### 3. 发送注册验证码
- **URL**: `/api/auth/send-code`
- **Method**: `POST`
- **Auth**: 无需认证
- **Query Params**: `email` (邮箱地址)
- **Response**:
  ```json
  { "code": 200, "message": "验证码已发送，请查收邮件", "data": null }
  ```

### 4. 绑定邮箱
- **URL**: `/api/auth/bind-email`
- **Method**: `POST`
- **Auth**: 需要 Token
- **Body**:
  ```json
  {
    "email": "user@example.com",
    "code": "123456"
  }
  ```
- **Response**:
  ```json
  { "code": 200, "message": "验证成功", "data": "验证成功" }
  ```

### 5. 用户登出
- **URL**: `/api/auth/logout`
- **Method**: `POST`
- **Auth**: 需要 Token
- **Response**:
  ```json
  { "code": 200, "message": "登出成功", "data": null }
  ```

### 6. 获取当前用户
- **URL**: `/api/auth/user`
- **Method**: `GET`
- **Auth**: 需要 Token
- **Response**: 返回当前登录用户的详细信息。

### 7. 检查登录状态
- **URL**: `/api/auth/check`
- **Method**: `GET`
- **Auth**: 无需认证 (用于前端判断 Token 有效性)
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "isLogin": true,
      "userId": 1,
      "tokenTimeout": 7200
    }
  }
  ```

---

## 👤 用户中心 (User)

### 1. 获取个人资料
- **URL**: `/api/user/profile`
- **Method**: `GET`
- **Auth**: 需要 Token
- **Response**: 返回当前用户的个人资料（不含密码等敏感信息）。

### 2. 更新个人资料
- **URL**: `/api/user/profile`
- **Method**: `PUT`
- **Auth**: 需要 Token
- **Body**:
  ```json
  {
    "nickname": "新昵称",
    "email": "newemail@example.com",
    "avatarUrl": "..."
  }
  ```

### 3. 修改密码
- **URL**: `/api/user/password`
- **Method**: `PUT`
- **Auth**: 需要 Token
- **Body**:
  ```json
  {
    "oldPassword": "oldPass",
    "newPassword": "newPass"
  }
  ```

---

## 🛡️ 管理员后台 (Admin)

> 以下接口均需要 `admin` 角色权限。

### 1. 系统统计数据
- **URL**: `/api/admin/statistics`
- **Method**: `GET`
- **Response**:
  ```json
  {
    "code": 200,
    "data": {
      "userCount": 100,
      "questionCount": 5000,
      "todayLoginCount": 25,
      "todayPracticeCount": 150
    }
  }
  ```

### 2. 用户管理
- **获取列表**: `GET /api/admin/users?page=1&size=10&username=xxx`
- **获取详情**: `GET /api/admin/users/{id}`
- **修改状态**: `PUT /api/admin/users/{id}/status` (Body: `{ "status": 0 }`)
- **重置密码**: `PUT /api/admin/users/{id}/reset-password`
- **查看练习记录**: `GET /api/practice/records?userId={id}` (返回该用户的"罪犯档案")
- **删除用户**: `DELETE /api/admin/users/{id}` (物理删除)

### 3. 日志查询
- **登录日志**: `GET /api/admin/login-logs?page=1&size=10`
- **操作日志**: `GET /api/admin/operation-logs?page=1&size=10`

---

## 📝 题目管理 (Question)

### 1. 查询题目列表
- **URL**: `/api/questions`
- **Method**: `GET`
- **Query Params**:
  - `page`: 页码 (默认1)
  - `size`: 每页数量 (默认10)
  - `subject`: 科目名
  - `type`: 题型 (`single-choice`, `multiple-choice`, `judge`)
  - `difficulty`: 难度
  - `keyword`: 关键词搜索
- **Auth**: 需要 Token

### 2. 新增题目
- **URL**: `/api/questions`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "type": "single-choice",
    "subject": "马原",
    "content": "题目内容...",
    "options": ["A:选项1", "B:选项2"],
    "answer": "A",
    "difficulty": "medium"
  }
  ```

    "answer": "A",
    "difficulty": "medium",
    "imageUrl": "https://example.com/image.jpg"
  }
  ```

### 3. 更新/删除/标记
- **更新**: `PUT /api/questions/{id}`
- **删除**: `DELETE /api/questions/{id}`
- **批量删除**: `DELETE /api/questions/batch` (Body: `[1, 2, 3]`)
- **标记/取消收藏**: `PUT /api/questions/{id}/mark`

### 4. 清空题库
- **URL**: `/api/questions/clear`
- **Method**: `DELETE`
- **Query Params**:
  - `subject`: 科目 (可选)
  - `type`: 题型 (可选)
  - `importLogId`: 导入批次ID (可选)
  - `ownerId`: 用户ID (可选, 仅管理员, -1代表公共题库)

### 5. 随机抽题
- **URL**: `/api/questions/random`
- **Method**: `GET`
- **Query Params**: `subject`, `type`, `count`

---

## 🎯 练习功能 (Practice)

### 1. 提交答题
- **URL**: `/api/practice/submit`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "questionId": 123,
    "userAnswer": "A",
    "costTime": 15
  }
  ```
- **Response**: 返回是否正确及正确答案解析。

### 2. 获取错题本
- **URL**: `/api/practice/wrong`
- **Method**: `GET`
- **Query Params**: `page`, `size`, `subject`

### 3. 获取统计信息
- **URL**: `/api/practice/statistics`
- **Method**: `GET`
- **Response**: 返回用户的练习总数、正确率、各科目掌握情况等。

### 4. 获取练习记录
- **URL**: `/api/practice/records`
- **Method**: `GET`
- **Auth**: 需要 Token
- **Query Params**:
  - `page`: 页码 (默认1)
  - `size`: 每页数量 (默认20)
  - `userId`: 目标用户ID (可选，仅管理员可用，普通用户只能查自己)
- **Response**: 返回练习记录分页数据（包含时间、题目ID、用户答案、是否正确、题目内容等）。

---

## 📢 公告管理 (Announcement)

### 1. 获取未读公告
- **URL**: `/api/announcements/unread`
- **Method**: `GET`
- **Auth**: 需要 Token
- **Response**: 返回当前用户尚未阅读的所有公告列表。

### 2. 标记公告为已读
- **URL**: `/api/announcements/{id}/read`
- **Method**: `POST`
- **Auth**: 需要 Token
- **Response**: `{ "code": 200, "message": "已标记为完成", "data": null }`

---

## 📥 导入导出 (Import/Export)

### 1. 导入 Excel
- **URL**: `/api/import/excel`
- **Method**: `POST`
- **ContentType**: `multipart/form-data`
- **Params**:
  - `file`: Excel文件
  - `subject`: 科目名称 (可选)。**注意**：导入时会检查当前用户下是否已存在同名科目，若存在则返回错误。

### 2. 导出题目
- **URL**: `/api/import/export`
- **Method**: `GET`
- **Query Params**:
  - `subject`: 科目名 (可选)
  - `importLogId`: 导入日志ID (可选，管理员可下载任意批次)
- **Response**: 下载 Excel 文件

### 3. 导出所有可见题目 (包含公共题库)
- **URL**: `/api/export/all`
- **Method**: `GET`
- **Response**: 下载 Excel 文件

### 4. 题库转换日志
- **保存日志**: `POST /api/import/convert-log`
- **Body**:
  ```json
  {
    "sourceFileName": "raw.txt",
    "sourceFileContent": "Base64String...",
    "resultFileName": "result.xlsx",
    "resultFileContent": "Base64String...",
    "subjectName": "科目名",
    "choiceCount": 10,
    "judgeCount": 5
  }
  ```
- **下载原始文件**: `GET /api/import/convert-log/{logId}/source`
- **下载结果文件**: `GET /api/import/convert-log/{logId}/result`

### 5. 下载模板
- **选择题模板**: `GET /api/import/template/choice`
- **判断题模板**: `GET /api/import/template/judge`

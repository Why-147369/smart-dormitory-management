# GitHub 上传指南

## ⚠️ 第一步：处理敏感信息（必须做，否则泄露密码和API Key）

你的 `application.yml` 里明文存了：
- 数据库密码：`123456`
- DeepSeek API Key：`sk-4a73a8...`
- JWT 密钥：`dormitory-system-secret-key-2026`

### 1. 创建 application-example.yml（给别人的模板）

在 `dormitory-admin/src/main/resources/` 下新建 `application-example.yml`，把真实密码替换成占位符。

### 2. 在 .gitignore 中排除真实配置文件

项目根目录新建 `.gitignore`，写入：

```
# 敏感配置（真实密码和API Key不上传）
application.yml

# 编译输出
target/
*.class
*.jar

# 前端编译输出
node_modules/
dist/

# IDE
.idea/
*.iml
.vscode/

# 上传文件
uploads/

# 系统文件
.DS_Store
Thumbs.db

# TRAE
.trae/
```

### 3. 把 application.yml 重命名为 application-example.yml

复制一份 `application.yml` → `application-example.yml`，在 example 文件里把敏感值改成占位符：
```yaml
password: your_password    # 改掉
key: sk-your-api-key       # 改掉  
secret: your-jwt-secret    # 改掉
```

然后保留原始的 `application.yml`（gitignore 会忽略它），example 版上传供别人参考。

---

## 第二步：初始化 Git 并推送

```bash
# 进入项目目录
cd "C:/Users/32010/Desktop/宿舍管理系统/宿舍管理系统修改版"

# 初始化
git init

# 添加所有文件（.gitignore 会自动排除敏感文件）
git add .

# 首次提交
git commit -m "🎉 智能宿舍管理系统 v1.0"

# 关联你的 GitHub 仓库（先在 GitHub 网站上创建空仓库）
git remote add origin https://github.com/你的用户名/仓库名.git

# 推送
git branch -M main
git push -u origin main
```

---

## 第三步：完善 README.md

在项目根目录建一个 README.md，包含：
- 项目简介
- 技术栈
- 功能列表
- 运行方法
- 截图展示

---

## 你要先在 GitHub 做的事

1. 打开 https://github.com ，登录（没有就注册）
2. 点右上角 + → New repository
3. Repository name：`smart-dormitory-system`（或其他名字）
4. Description：基于 Spring Boot + Vue 3 的智能宿舍管理系统
5. 选 Public（公开）
6. **不要勾选** Initialize with README（因为你本地已经有代码了）
7. 点 Create repository
8. 复制出现的远程地址（类似 `https://github.com/用户名/仓库名.git`）

然后在终端执行第二步的命令。

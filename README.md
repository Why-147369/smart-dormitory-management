# 智能宿舍管理系统

> Smart Dormitory Management System | 毕业设计 

基于 **Spring Boot + Vue 3** 的前后端分离智能宿舍管理系统，为学生、宿管员、管理员、维修人员四类角色提供宿舍管理全流程数字化服务。

## 功能总览

| 角色 | 主要功能 |
|------|---------|
| 学生 | 在线报修、水电费缴纳、晚归打卡、换寝申请、访客预约、失物招领、AI 智能客服 |
| 宿管员 | 学生管理、床位分配、报修处理、卫生检查、水电费录入、打卡统计 |
| 管理员 | 全角色用户管理、宿舍资源管理、数据统计可视化、Excel 导入导出、公告管理 |
| 维修人员 | 接单、维修中、完成维修（按专业类型自动匹配工单） |

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + ECharts + Vite |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0（29 张表） |
| 工具 | Apache POI（Excel）、JMeter（性能测试） |

## 项目结构

```
├── dormitory-admin/          # 后端（Spring Boot）
│   └── src/main/java/com/dormitory/
│       ├── controller/       # 28 个控制器
│       ├── entity/          # 实体类
│       ├── mapper/          # MyBatis 数据访问
│       ├── service/         # 业务逻辑
│       ├── config/          # Spring 配置
│       └── utils/           # 工具类（JWT等）
├── dormitory-web/            # 前端（Vue 3）
│   └── src/
│       ├── views/           # 52 个页面（按角色分组）
│       ├── router/          # 路由配置
│       ├── store/           # Pinia 状态管理
│       └── utils/           # Axios 封装
├── dormitory_system.sql      # 数据库建表脚本
└── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 1. 初始化数据库
```bash
mysql -u root -p < dormitory_system.sql
```
然后执行 `maintenance_person_upgrade.sql`（维修人员功能升级脚本）。

### 2. 启动后端
```bash
cd dormitory-admin
# 修改 application.yml 中的数据库密码和 API Key
mvn spring-boot:run
```
默认端口：8080

### 3. 启动前端
```bash
cd dormitory-web
npm install
npm run dev
```
默认端口：5173，自动代理 API 到后端 8080

### 4. 登录
- 管理员：admin / 123456
- 学生和宿管账号通过管理员端添加

## 创新功能

- **AI 智能客服**：集成 DeepSeek 大模型，7×24 小时自动回答学生咨询
- **水电超限预警**：按宿舍类型（4/6 人间）区别定价，超限自动推送通知
- **N+1 查询优化**：学生列表从 1501 次数据库查询降到 4 次，响应从 2 秒降至 50ms
- **维修人员智能匹配**：按报修类型自动匹配对应专业的维修师傅

## 系统截图

登录、报修、AI 客服、数据统计等界面截图请见项目内截图或运行后查看。

## License

MIT License

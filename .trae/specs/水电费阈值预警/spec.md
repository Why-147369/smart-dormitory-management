# 水电费阈值预警功能规格说明

## Why
当前系统阈值设置形同虚设，水电超限没有实际预警通知。需要实现完整的水电费预警功能，让管理员、宿管、学生都能及时知道水电异常。

## What Changes
- 管理员端添加警告管理页面，显示超阈值记录
- 宿管端添加超阈值标红提示
- 学生端添加超阈值自动通知

## Impact
- 涉及 UtilityController 修改
- 涉及前端三个端的页面修改
- 需要新增警告记录存储

## ADDED Requirements

### Requirement 1: 管理员端 - 警告管理
系统 SHALL 提供管理员查看所有水电超限记录的功能。

#### Scenario: 查看超限记录
- **GIVEN** 管理员进入水电管理页面
- **WHEN** 点击"警告管理"标签
- **THEN** 显示所有超阈值记录列表
- **AND** 支持按楼栋筛选
- **AND** 支持按年份月份筛选
- **AND** 支持按状态筛选（未处理/已处理）
- **AND** 支持批量导出Excel

#### Scenario: 记录内容
- 每条记录显示：宿舍号、楼栋、超水用量、超电用量、所属月份、状态

---

### Requirement 2: 宿管端 - 超限标红
宿管新增账单时，如果用水或用电超过阈值，应该明显提示。

#### Scenario: 超限标红
- **GIVEN** 宿管在水电费管理新增账单
- **WHEN** 用水量 > 阈值 或 用电量 > 阈值
- **THEN** 页面显示红色警示
- **AND** 提示"用水/用电超限！"

---

### Requirement 3: 学生端 - 超限通知
当宿舍用水用电超限时，自动通知该宿舍所有学生。

#### Scenario: 自动发送通知
- **GIVEN** 宿管录入或导入水电账单
- **WHEN** 用水量 > 阈值 或 用电量 > 阈值
- **THEN** 系统自动发送消息给该宿舍学生
- **AND** 消息内容："本宿舍用水/用电/用水用电超限，请节约使用！"

---

## 数据表设计

### utility_warning 表（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| room_id | BIGINT | 宿舍ID |
| building_id | BIGINT | 楼栋ID |
| year | INT | 年份 |
| month | INT | 月份 |
| water_usage | DECIMAL | 用水量 |
| water_limit | DECIMAL | 用水阈值 |
| electric_usage | DECIMAL | 用电量 |
| electric_limit | DECIMAL | 用电阈值 |
| is_water_over | TINYINT | 用水是否超限(0/1) |
| is_electric_over | TINYINT | 用电是否超限(0/1) |
| status | INT | 状态(0:未处理,1:已处理) |
| create_time | DATETIME | 创建时间 |

---

## API设计

### 警告管理
- GET /api/utility/warning/list - 警告列表（支持筛选）
- PUT /api/utility/warning/{id}/process - 标记为已处理
- GET /api/utility/warning/export - 导出警告记录

### 通知触发
- 在账单创建/导入时自动检查并生成警告记录
- 自动发送消息给学生

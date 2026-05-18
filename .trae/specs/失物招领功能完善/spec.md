# 学生端失物招领功能完善规格

## Why
学生端失物招领功能不完善，缺少筛选和分页功能，影响用户体验。

## What Changes
- 添加按状态筛选（待认领/已找到）
- 添加按物品类型筛选
- 添加分页功能

## Impact
- 前端文件：student/lost-found/index.vue

## ADDED Requirements

### Requirement 1: 筛选功能
系统 SHALL 提供失物和拾物的筛选功能。

#### Scenario: 按状态筛选
- **GIVEN** 用户在失物/拾物列表页面
- **WHEN** 选择状态筛选条件（全部/待认领/已找到）
- **THEN** 列表只显示符合条件的数据

#### Scenario: 按类型筛选
- **GIVEN** 用户在失物/拾物列表页面
- **WHEN** 选择物品类型筛选条件
- **THEN** 列表只显示符合条件的数据

### Requirement 2: 分页功能
系统 SHALL 提供分页展示功能。

#### Scenario: 分页展示
- **GIVEN** 数据超过单页显示数量
- **WHEN** 用户查看列表
- **THEN** 显示分页控件，支持翻页

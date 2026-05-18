# 管理员端失物招领一键清除已认领功能 Spec

## Why
管理员希望能够一键删除所有已认领（status=1）的失物招领数据，方便清理历史数据。

## What Changes
在管理员端失物招领管理页面添加"一键清除已认领"按钮

## Impact
- Affected code: admin/lost-found/index.vue
- Backend: LostAndFoundController.java 需要添加批量删除接口

## ADDED Requirements
### Requirement: 一键清除已认领功能
- 在搜索按钮区域添加"一键清除已认领"按钮
- 点击时弹出确认框，提示将删除所有已找到的数据
- 确认后调用后端接口批量删除 status=1 的记录
- 删除成功后刷新列表并提示用户

#### Scenario: 删除成功
- **WHEN** 管理员点击"一键清除已认领"按钮并确认
- **THEN** 所有已认领的记录被删除，列表刷新，显示成功提示

#### Scenario: 无数据可删除
- **WHEN** 管理员点击"一键清除已认领"按钮，但没有已认领的记录
- **THEN** 提示"暂无已认领的数据"

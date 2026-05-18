# 删除钥匙借用功能 Spec

## Why
用户明确表示不需要钥匙借用功能，因此需要从系统中彻底移除该功能。

## What Changes
- 删除后端 `KeyBorrow` 实体类及相关代码
- 删除 `key_borrow` 数据库表
- 从 ER 图中移除钥匙借用相关实体
- 更新功能描述文档

## Impact
- Affected specs: 功能描述文档重写
- Affected code: 
  - `dormitory-admin/src/main/java/com/dormitory/entity/KeyBorrow.java`
  - `dormitory-admin/src/main/java/com/dormitory/mapper/KeyBorrowMapper.java`
  - `dormitory-admin/src/main/java/com/dormitory/controller/KeyBorrowController.java`
  - `dormitory_er_diagram_improved.drawio`
  - `dormitory_system.sql`

## REMOVED Requirements
### Requirement: 钥匙借用管理
**Reason**: 用户明确表示不需要该功能
**Migration**: 无需迁移，该功能未在前端实现

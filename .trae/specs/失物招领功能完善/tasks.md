# Tasks

## 任务：完善学生端失物招领功能

### Task 1: 添加筛选表单
- [ ] 1.1 添加状态筛选下拉框（全部/待认领/已找到）
- [ ] 1.2 添加物品类型筛选下拉框
- [ ] 1.3 添加搜索按钮和重置按钮

### Task 2: 添加分页功能
- [ ] 2.1 添加分页数据定义（pageNum, pageSize, total）
- [ ] 2.2 添加 el-pagination 组件
- [ ] 2.3 修改 getList 函数支持分页参数

### Task 3: 修改后端接口支持筛选
- [ ] 3.1 LostFoundController 添加 type, status, itemType 筛选参数

# Task Dependencies
- Task 3 → Task 1 和 Task 2（后端先支持筛选参数）

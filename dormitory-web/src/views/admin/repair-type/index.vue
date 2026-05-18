<template>
  <div class="repair-type-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修类型管理</span>
          <el-button type="primary" @click="handleAdd">新增类型</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="typeName" label="类型名称" min-width="150">
          <template #default="{ row }">
            <span v-if="!row.editing">{{ row.typeName }}</span>
            <el-input v-else v-model="row.typeName" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100">
          <template #default="{ row }">
            <span v-if="!row.editing">{{ row.sortOrder }}</span>
            <el-input-number v-else v-model="row.sortOrder" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="!row.editing">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="handleSave(row)">保存</el-button>
              <el-button link @click="handleCancel(row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" title="新增报修类型" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="类型名称" required>
          <el-input v-model="form.typeName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)

const form = reactive({
  typeName: '',
  sortOrder: 0
})

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/type/list')
    tableData.value = (res.data || []).map(item => ({ ...item, editing: false }))
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.typeName = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.typeName) {
    ElMessage.warning('请输入类型名称')
    return
  }
  try {
    await request.post('/repair/type', form)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error('添加失败')
  }
}

const handleEdit = (row) => {
  row.editing = true
}

const handleCancel = (row) => {
  row.editing = false
  getList()
}

const handleSave = async (row) => {
  try {
    await request.put(`/repair/type/${row.id}`, {
      typeName: row.typeName,
      sortOrder: row.sortOrder
    })
    ElMessage.success('保存成功')
    row.editing = false
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/repair/type/${row.id}`, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该类型吗？', '提示', { type: 'warning' })
    await request.delete(`/repair/type/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.repair-type-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

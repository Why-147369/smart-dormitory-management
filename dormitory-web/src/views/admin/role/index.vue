<template>
  <div class="admin-role">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="角色标识" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: null,
  roleName: '',
  roleKey: '',
  description: ''
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

const getList = async () => {
  try {
    const res = await request.get('/role/list')
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
    } else if (Array.isArray(data)) {
      tableData.value = data
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, roleName: '', roleKey: '', description: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const index = tableData.value.findIndex(item => item.id === form.value.id)
          if (index !== -1) {
            tableData.value[index] = { ...form.value }
          }
          ElMessage.success('更新成功')
        } else {
          const newId = tableData.value.length > 0 ? Math.max(...tableData.value.map(t => t.id)) + 1 : 1
          tableData.value.push({ ...form.value, id: newId, createTime: new Date().toLocaleString() })
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 ${row.roleName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tableData.value = tableData.value.filter(item => item.id !== row.id)
    ElMessage.success('删除成功')
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.admin-role {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

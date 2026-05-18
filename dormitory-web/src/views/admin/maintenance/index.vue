<template>
  <div class="maintenance-person">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修人员管理</span>
          <el-button type="primary" @click="handleAdd">新增维修人员</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/电话/擅长类型" style="width: 200px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="username" label="登录账号" width="110" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="specialty" label="擅长维修类型" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="留空则不修改密码" show-password />
        </el-form-item>
        <el-form-item label="擅长维修类型" prop="specialty">
          <el-select v-model="form.specialty" placeholder="请选择擅长维修类型" style="width: 100%;">
            <el-option
              v-for="item in repairTypes"
              :key="item.id"
              :label="item.typeName"
              :value="item.typeName"
            />
          </el-select>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const repairTypes = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: null,
  name: '',
  phone: '',
  specialty: '',
  username: '',
  password: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const getList = async () => {
  try {
    const res = await request.get('/maintenance/list', { params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: searchKeyword.value } })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  getList()
}

const handleReset = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  getList()
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, name: '', phone: '', specialty: '', username: '', password: '' }
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
          await request.put('/maintenance', form.value)
          ElMessage.success('更新成功')
        } else {
          await request.post('/maintenance', form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await request.put(`/maintenance/status/${row.id}`, null, { params: { status: newStatus } })
    ElMessage.success(`${action}成功`)
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除维修人员 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/maintenance/${row.id}`)
      ElMessage.success('删除成功')
      getList()
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(() => {
  getList()
  getRepairTypes()
})

const getRepairTypes = async () => {
  try {
    const res = await request.get('/repair/type/list')
    repairTypes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped lang="scss">
.maintenance-person {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .search-bar {
    display: flex;
    gap: 10px;
    align-items: center;
  }
}
</style>

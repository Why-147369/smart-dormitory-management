<template>
  <div class="admin-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>宿管管理</span>
          <el-button type="primary" @click="handleAdd">添加宿管</el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/账号" style="width: 200px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      
      <el-table :data="tableData" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="buildingName" label="负责楼栋" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="title" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="负责楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋">
            <el-option
              v-for="item in availableBuildings"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="默认密码123456" />
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
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const buildingList = ref([])
const assignedBuildingIds = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: null,
  username: '',
  name: '',
  gender: 1,
  phone: '',
  buildingId: null,
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择负责楼栋', trigger: 'change' }]
}

const title = computed(() => isEdit.value ? '编辑宿管' : '添加宿管')

const availableBuildings = computed(() => {
  return buildingList.value.filter(b => {
    if (isEdit.value && form.value.buildingId === b.id) {
      return true
    }
    return !assignedBuildingIds.value.includes(b.id)
  })
})

const getList = async () => {
  try {
    const res = await request.get('/manager/list', { 
      params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: searchKeyword.value } 
    })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total
    } else if (Array.isArray(data)) {
      tableData.value = data
      total.value = data.length
    }
  } catch (e) {
    console.error(e)
  }
}

const getBuildingList = async () => {
  try {
    const buildingRes = await request.get('/building/list')
    const data = buildingRes.data
    if (data && data.records) {
      buildingList.value = data.records
    } else if (Array.isArray(data)) {
      buildingList.value = data
    }
    
    const managerRes = await request.get('/manager/list', { params: { pageNum: 1, pageSize: 100 } })
    const managerData = managerRes.data
    let managers = []
    if (managerData && managerData.records) {
      managers = managerData.records
    } else if (Array.isArray(managerData)) {
      managers = managerData
    }
    
    assignedBuildingIds.value = managers
      .filter(m => m.buildingId)
      .map(m => m.buildingId)
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
  form.value = {
    id: null,
    username: '',
    name: '',
    gender: 1,
    phone: '',
    buildingId: null,
    password: '123456'
  }
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await request.get(`/manager/${row.id}`)
    const manager = res.data
    form.value = {
      id: manager.id,
      username: manager.username,
      name: manager.name,
      gender: manager.gender,
      phone: manager.phone,
      buildingId: manager.buildingId,
      password: ''
    }
    isEdit.value = true
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put('/manager', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/manager', form.value)
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

const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确定要重置宿管 ${row.name} 的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/manager/password/${row.id}`)
      ElMessage.success('密码已重置为 123456')
    } catch (e) {
      console.error(e)
    }
  })
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 0 ? 1 : 0
  const action = newStatus === 0 ? '启用' : '禁用'
  try {
    await request.put(`/manager/status/${row.id}`, { status: newStatus })
    ElMessage.success(`${action}成功`)
    getList()
  } catch (e) {
    console.error(e)
    getList()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该宿管吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/manager/${row.id}`)
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
  getBuildingList()
})
</script>

<style scoped lang="scss">
.admin-manager {
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

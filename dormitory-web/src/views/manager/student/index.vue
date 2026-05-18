<template>
  <div class="manager-student">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="handleAdd">添加学生</el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/学号/班级/学院/专业" style="width: 200px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleExport">导出Excel</el-button>
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          批量退宿 ({{ selectedRows.length }})
        </el-button>
      </div>
      
      <el-table :data="tableData" style="width: 100%; margin-top: 20px;" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="studentNumber" label="学号" width="150" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" width="150" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="buildingName" label="楼栋" width="80" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="bedNumber" label="床位号" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">在校</el-tag>
            <el-tag v-else type="info">离校</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">退宿</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="getList"
        @size-change="getList"
        layout="total, sizes, prev, pager, next"
      />
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="form.studentNumber" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="form.college" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">在校</el-radio>
            <el-radio :label="0">离校</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="宿舍">
          <el-select v-model="form.roomId" placeholder="选择宿舍" clearable @change="handleRoomChange">
            <el-option v-for="room in roomList" :key="room.id" :label="room.roomNumber" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位">
          <el-select v-model="form.bedId" placeholder="选择床位" clearable :disabled="!form.roomId">
            <el-option v-for="bed in bedList" :key="bed.id" :label="bed.bedNumber" :value="bed.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加学生')
const isEdit = ref(false)
const formRef = ref(null)
const selectedRows = ref([])
const roomList = ref([])
const bedList = ref([])

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const form = ref({
  id: null,
  studentNumber: '',
  name: '',
  gender: 1,
  college: '',
  major: '',
  className: '',
  phone: '',
  status: 0,
  roomId: null,
  bedId: null
})

const rules = {
  studentNumber: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    }
    const res = await request.get('/student/list', { params })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
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

const handleAdd = async () => {
  form.value = {
    id: null,
    studentNumber: '',
    name: '',
    gender: 1,
    college: '',
    major: '',
    className: '',
    phone: '',
    status: 0,
    roomId: null,
    bedId: null,
    originalBedId: null
  }
  bedList.value = []
  await loadRoomList()
  dialogTitle.value = '添加学生'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { 
    id: row.id,
    studentNumber: row.studentNumber,
    name: row.name,
    gender: Number(row.gender),
    college: row.college,
    major: row.major,
    className: row.className,
    phone: row.phone,
    status: Number(row.status),
    roomId: row.roomId || null,
    bedId: row.bedId || null,
    originalBedId: row.bedId || null
  }
  bedList.value = []
  if (row.roomId) {
    loadBedList(row.roomId, row.bedId)
  }
  loadRoomList()
  dialogTitle.value = '编辑学生'
  isEdit.value = true
  dialogVisible.value = true
}

const handleRoomChange = (roomId) => {
  form.value.bedId = null
  bedList.value = []
  if (roomId) {
    loadBedList(roomId, isEdit.value ? form.value.bedId : null)
  }
}

const loadRoomList = async () => {
  try {
    const res = await request.get('/room/list', { params: { pageNum: 1, pageSize: 100 } })
    if (res.data && res.data.records) {
      roomList.value = res.data.records.filter(r => r.currentCount < r.bedCount)
    }
  } catch (e) {
    console.error(e)
  }
}

const loadBedList = async (roomId, currentBedId = null) => {
  try {
    const res = await request.get('/bed/list', { params: { roomId, pageNum: 1, pageSize: 100 } })
    if (res.data && res.data.records) {
      const emptyBeds = res.data.records.filter(b => b.status === 0)
      if (currentBedId) {
        const currentBed = res.data.records.find(b => b.id === currentBedId)
        if (currentBed) {
          bedList.value = [...emptyBeds, currentBed]
        } else {
          bedList.value = emptyBeds
        }
      } else {
        bedList.value = emptyBeds
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要让学生 ${row.name} 退宿吗？退宿后学生信息保留，只是解除宿舍分配关系。`, '提示', { type: 'warning' })
    if (row.bedId) {
      await request.post('/bed/remove', { bedId: row.bedId })
    } else {
      ElMessage.warning('该学生未分配床位')
      return
    }
    ElMessage.success('退宿成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await request.put('/student/update', form.value)
      if (form.value.bedId && form.value.bedId !== form.value.originalBedId) {
        if (form.value.originalBedId) {
          await request.post('/bed/remove', { bedId: form.value.originalBedId })
        }
        await request.post('/bed/assign', { 
          bedId: form.value.bedId,
          studentId: form.value.id
        })
      }
      ElMessage.success('更新成功')
    } else {
      await request.post('/student', form.value)
      const studentListRes = await request.get('/student/list', { params: { studentNumber: form.value.studentNumber } })
      if (studentListRes.data && studentListRes.data.records && studentListRes.data.records.length > 0) {
        const newStudent = studentListRes.data.records[0]
        if (form.value.bedId) {
          await request.post('/bed/assign', { 
            bedId: form.value.bedId,
            studentId: newStudent.id
          })
        }
      }
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '操作失败')
  }
}

const handleExport = async () => {
  try {
    const token = sessionStorage.getItem('manager_token')
    const response = await axios.get('/api/student/export', {
      responseType: 'blob',
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生信息.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要退宿的学生')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要让选中的 ${selectedRows.value.length} 名学生退宿吗？退宿后学生信息保留，只是解除宿舍分配关系。`, '提示', { type: 'warning' })
    const rowsWithBed = selectedRows.value.filter(row => row.bedId)
    for (const row of rowsWithBed) {
      await request.post('/bed/remove', { bedId: row.bedId })
    }
    ElMessage.success('批量退宿成功')
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
.manager-student {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-bar {
    display: flex;
    gap: 10px;
  }
}
</style>

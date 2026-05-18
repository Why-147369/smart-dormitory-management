<template>
  <div class="admin-student">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">添加学生</el-button>
            <el-button type="success" @click="handleImport">导入Excel</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/学号/班级/学院/专业" style="width: 220px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      
      <el-table 
        :data="tableData" 
        style="width: 100%; margin-top: 20px;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentNumber" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="college" label="学院" width="150" />
        <el-table-column prop="major" label="专业" width="120" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100">
          <template #default="{ row }">
            {{ row.roomNumber || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="title" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNumber">
              <el-input v-model="form.studentNumber" :disabled="isEdit" placeholder="学号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-input v-model="form.college" placeholder="学院" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="专业" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="className">
              <el-input v-model="form.className" placeholder="班级" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="初始密码">
              <el-input v-model="form.password" type="password" placeholder="默认为123456" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="importDialogVisible" title="导入学生" width="500px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div>Excel第一行必须包含以下表头：</div>
          <div style="margin-top: 5px;">学号 | 姓名 | 性别 | 电话 | 学院 | 专业 | 班级</div>
          <div style="margin-top: 5px;">性别填写"男"或"女"，默认密码为123456</div>
        </template>
      </el-alert>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :file-list="fileList"
        drag
        action="#"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将Excel文件拖到此处，或<em>点击上传</em>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImportSubmit">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const tableData = ref([])
const selectedRows = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const isEdit = ref(false)
const importing = ref(false)
const fileList = ref([])

const form = ref({
  id: null,
  studentNumber: '',
  name: '',
  gender: 1,
  phone: '',
  college: '',
  major: '',
  className: '',
  buildingId: null,
  roomId: null,
  bedNumber: 1,
  password: '123456'
})

const rules = {
  studentNumber: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const title = computed(() => isEdit.value ? '编辑学生' : '添加学生')

const getList = async () => {
  try {
    const res = await request.get('/student/list', { 
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

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
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
    studentNumber: '',
    name: '',
    gender: 1,
    phone: '',
    college: '',
    major: '',
    className: '',
    buildingId: null,
    roomId: null,
    bedNumber: 1,
    password: '123456'
  }
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await request.get(`/student/${row.id}`)
    const student = res.data
    form.value = {
      id: student.id,
      studentNumber: student.studentNumber,
      name: student.name,
      gender: student.gender,
      phone: student.phone,
      college: student.college,
      major: student.major,
      className: student.className,
      buildingId: student.buildingId,
      roomId: student.roomId,
      bedNumber: student.bedNumber,
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
          await request.put('/student/update', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/student', form.value)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/student/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个学生吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/student/batch-delete', ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleImport = () => {
  fileList.value = []
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  fileList.value = [file]
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个Excel文件')
}

const handleImportSubmit = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  
  const file = fileList.value[0].raw
  const formData = new FormData()
  formData.append('file', file)
  
  importing.value = true
  try {
    await request.post('/student/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    getList()
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.admin-student {
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

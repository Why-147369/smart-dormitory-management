<template>
  <div class="admin-room">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>宿舍管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">添加宿舍</el-button>
            <el-button type="success" @click="handleImport">导入Excel</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>
      
      <div class="search-bar">
        <el-select v-model="searchBuildingId" placeholder="选择楼栋" clearable style="width: 150px;">
          <el-option
            v-for="item in buildingList"
            :key="item.id"
            :label="item.buildingName"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="searchStatus" placeholder="入住状态" clearable style="width: 150px;">
          <el-option label="未满" :value="0" />
          <el-option label="已满" :value="1" />
        </el-select>
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
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="roomType" label="类型" width="80">
          <template #default="{ row }">
            {{ row.roomType === 4 ? '4人间' : '6人间' }}
          </template>
        </el-table-column>
        <el-table-column prop="bedCount" label="床位数" width="80" />
        <el-table-column prop="currentCount" label="已入住" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
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
    
    <el-dialog v-model="dialogVisible" :title="title" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋">
            <el-option
              v-for="item in buildingList"
              :key="item.id"
              :label="item.buildingName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="如：101" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="宿舍类型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="请选择类型" @change="handleRoomTypeChange">
            <el-option label="4人间" :value="4" />
            <el-option label="6人间" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位数" prop="bedCount">
          <el-input-number v-model="form.bedCount" :min="1" :max="form.roomType" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="importDialogVisible" title="导入宿舍" width="500px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div>Excel第一行必须包含以下表头：</div>
          <div style="margin-top: 5px;">楼栋名称 | 宿舍号 | 楼层 | 宿舍类型 | 床位数</div>
          <div style="margin-top: 5px;">楼栋名称必须已存在（如：1号楼），宿舍类型填4或6</div>
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
const buildingList = ref([])
const searchBuildingId = ref(null)
const searchStatus = ref(null)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const isEdit = ref(false)
const importing = ref(false)
const fileList = ref([])

const form = ref({
  id: null,
  buildingId: null,
  roomNumber: '',
  floor: 1,
  roomType: 4,
  bedCount: 4
})

const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入宿舍号', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const title = computed(() => isEdit.value ? '编辑宿舍' : '添加宿舍')

const getList = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchBuildingId.value) {
      params.buildingId = searchBuildingId.value
    }
    if (searchStatus.value !== null) {
      params.status = searchStatus.value
    }
    const res = await request.get('/room/page', { params })
    const data = res.data
    if (data && data.records) {
      const buildingRes = await request.get('/building/list')
      const buildings = buildingRes.data.records || buildingRes.data || []
      const buildingMap = {}
      buildings.forEach(b => {
        buildingMap[b.id] = b.buildingName
      })
      data.records.forEach(r => {
        r.buildingName = buildingMap[r.buildingId]
      })
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
  searchBuildingId.value = null
  searchStatus.value = null
  getList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个宿舍吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/room/batch-delete', ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const getBuildingList = async () => {
  try {
    const res = await request.get('/building/list')
    buildingList.value = res.data || []
  } catch (e) {
    console.error(e)
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
    await request.post('/room/import', formData, {
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

const handleAdd = () => {
  form.value = {
    id: null,
    buildingId: null,
    roomNumber: '',
    floor: 1,
    roomType: 4,
    bedCount: 4
  }
  isEdit.value = false
  dialogVisible.value = true
}

const handleRoomTypeChange = (value) => {
  form.value.bedCount = value
}

const handleEdit = (row) => {
  form.value = { ...row }
  isEdit.value = true
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put('/room', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/room', form.value)
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
    await ElMessageBox.confirm('确定要删除该宿舍吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/room/${row.id}`)
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
.admin-room {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

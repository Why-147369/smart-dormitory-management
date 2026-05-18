<template>
  <div class="admin-bed">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>床位管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增床位</el-button>
            <el-button type="success" @click="handleImport">导入Excel</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>
      
      <div class="search-bar">
        <el-select v-model="searchBuildingId" placeholder="选择楼栋" clearable style="width: 150px;" @change="handleSearchBuildingChange">
          <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
        </el-select>
        <el-select v-model="searchRoomId" placeholder="选择宿舍" clearable style="width: 150px;" @change="handleSearch">
          <el-option v-for="item in filterRoomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
        </el-select>
        <el-select v-model="searchStatus" placeholder="床位状态" clearable style="width: 150px;" @change="handleSearch">
          <el-option label="空闲" :value="0" />
          <el-option label="已占用" :value="1" />
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
        <el-table-column prop="bedNumber" label="床位号" width="100" />
        <el-table-column prop="studentName" label="学生" width="100">
          <template #default="{ row }">
            {{ row.studentName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success">空闲</el-tag>
            <el-tag v-else type="warning">已占用</el-tag>
          </template>
        </el-table-column>
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" @change="handleBuildingChange">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择宿舍">
            <el-option v-for="item in roomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位号" prop="bedNumber">
          <el-input-number v-model="form.bedNumber" :min="1" :max="8" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">空闲</el-radio>
            <el-radio :label="1">已占用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="importDialogVisible" title="导入床位" width="500px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div>Excel第一行必须包含以下表头：</div>
          <div style="margin-top: 5px;">楼栋名称 | 宿舍号 | 床位号 | 状态</div>
          <div style="margin-top: 5px;">楼栋名称和宿舍号必须已存在，状态填0（空闲）或1（已占用）</div>
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
const searchBuildingId = ref(null)
const searchRoomId = ref(null)
const searchStatus = ref(null)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const isEdit = ref(false)
const importing = ref(false)
const fileList = ref([])
const buildingList = ref([])
const allRoomList = ref([])

const form = ref({
  id: null,
  buildingId: null,
  roomId: null,
  bedNumber: 1,
  status: 0
})

const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择宿舍', trigger: 'change' }],
  bedNumber: [{ required: true, message: '请输入床位号', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑床位' : '新增床位')

const roomList = computed(() => allRoomList.value)

const filterRoomList = computed(() => {
  if (searchBuildingId.value) {
    return allRoomList.value.filter(r => r.buildingId === searchBuildingId.value)
  }
  return allRoomList.value
})

const getList = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchBuildingId.value) {
      params.buildingId = searchBuildingId.value
    }
    if (searchRoomId.value) {
      params.roomId = searchRoomId.value
    }
    if (searchStatus.value !== null) {
      params.status = searchStatus.value
    }
    const res = await request.get('/bed/page', { params })
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

const handleSearchBuildingChange = async () => {
  searchRoomId.value = null
  if (searchBuildingId.value) {
    await getRooms(searchBuildingId.value)
  } else {
    await loadBuildingAndRoom()
  }
  handleSearch()
}

const handleReset = () => {
  searchBuildingId.value = null
  searchRoomId.value = null
  searchStatus.value = null
  pageNum.value = 1
  getList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个床位吗？`, '提示', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/bed/batch-delete', ids)
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
    await request.post('/bed/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
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

const getBuildings = async () => {
  try {
    const buildingRes = await request.get('/building/list')
    const bData = buildingRes.data
    if (bData && bData.records) {
      buildingList.value = bData.records
    } else if (Array.isArray(bData)) {
      buildingList.value = bData
    }
    
    const roomRes = await request.get('/room/list')
    const rData = roomRes.data
    if (rData && rData.records) {
      allRoomList.value = rData.records
    } else if (Array.isArray(rData)) {
      allRoomList.value = rData
    }
  } catch (e) {
    console.error(e)
  }
}

const getRooms = async (buildingId) => {
  try {
    const res = await request.get(`/room/building/${buildingId}`)
    const data = res.data
    if (Array.isArray(data)) {
      roomList.value = data
      allRoomList.value = data
    }
  } catch (e) {
    console.error(e)
  }
}

const handleBuildingChange = () => {
  form.value.roomId = null
  if (form.value.buildingId) {
    getRooms(form.value.buildingId)
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, buildingId: null, roomId: null, bedNumber: 1, status: 0 }
  roomList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { 
    id: row.id, 
    buildingId: null, 
    roomId: row.roomId, 
    bedNumber: row.bedNumber, 
    status: row.status 
  }
  if (row.roomId) {
    getRoomsByRoomId(row.roomId)
  }
  dialogVisible.value = true
}

const getRoomsByRoomId = async (roomId) => {
  try {
    const res = await request.get(`/room/${roomId}`)
    const room = res.data
    if (room && room.buildingId) {
      form.value.buildingId = room.buildingId
      getRooms(room.buildingId)
    }
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
          const bed = {
            id: form.value.id,
            roomId: form.value.roomId,
            bedNumber: form.value.bedNumber,
            status: form.value.status
          }
          await request.put('/bed', bed)
          ElMessage.success('更新成功')
        } else {
          await request.post('/bed', form.value)
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

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除床位 ${row.buildingName}-${row.roomNumber}-${row.bedNumber}号床吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/bed/${row.id}`)
      ElMessage.success('删除成功')
      getList()
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(() => {
  getList()
  getBuildings()
})
</script>

<style scoped lang="scss">
.admin-bed {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

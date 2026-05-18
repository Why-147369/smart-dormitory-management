<template>
  <div class="admin-assignment">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>入住分配管理</span>
          <div>
            <el-button type="primary" @click="handleAssign">分配床位</el-button>
            <el-button type="success" @click="handleImport">Excel批量导入分配</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchRemove">
              批量退宿 ({{ selectedRows.length }})
            </el-button>
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
        <el-select v-model="searchStatus" placeholder="分配状态" clearable style="width: 150px;" @change="handleSearch">
          <el-option label="未分配" :value="0" />
          <el-option label="已分配" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      
      <el-table :data="tableData" style="width: 100%; margin-top: 20px;" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="bedNumber" label="床位号" width="80" />
        <el-table-column prop="studentName" label="学生" width="100">
          <template #default="{ row }">
            <span v-if="row.studentName">{{ row.studentName }}</span>
            <span v-else style="color: #999;">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentNumber" label="学号" width="120">
          <template #default="{ row }">
            <span v-if="row.studentNumber">{{ row.studentNumber }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.studentId" type="success">已分配</el-tag>
            <el-tag v-else type="info">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.studentId" link type="primary" @click="handleSingleAssign(row)">分配</el-button>
            <el-button v-else link type="danger" @click="handleRemove(row)">退宿</el-button>
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
    
    <el-dialog v-model="dialogVisible" title="分配床位" width="600px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="床位">
          <span>{{ assignForm.buildingName }} - {{ assignForm.roomNumber }} - {{ assignForm.bedNumber }}号床</span>
        </el-form-item>
        <el-form-item label="学生">
          <el-select v-model="assignForm.studentId" placeholder="选择学生" filterable style="width: 100%;">
            <el-option
              v-for="item in unassignedStudents"
              :key="item.id"
              :label="item.name + ' (' + item.studentNumber + ')'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="importDialogVisible" title="Excel批量导入分配" width="500px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div>Excel第一行必须包含以下表头：</div>
          <div style="margin-top: 5px;">学号 | 楼栋名称 | 宿舍号 | 床位号</div>
          <div style="margin-top: 5px;">请确保学号和学生信息已存在</div>
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
const buildingList = ref([])
const allRoomList = ref([])
const searchBuildingId = ref(null)
const searchRoomId = ref(null)
const searchStatus = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const importing = ref(false)
const fileList = ref([])
const unassignedStudents = ref([])

const assignForm = ref({
  bedId: null,
  buildingName: '',
  roomNumber: '',
  bedNumber: '',
  studentId: null
})

const filterRoomList = computed(() => {
  if (searchBuildingId.value) {
    return allRoomList.value.filter(r => r.buildingId === searchBuildingId.value)
  }
  return allRoomList.value
})

const getList = async () => {
  try {
    console.log('getList调用, searchStatus:', searchStatus.value, 'searchBuildingId:', searchBuildingId.value, 'searchRoomId:', searchRoomId.value)
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      hasStudent: searchStatus.value
    }
    if (searchBuildingId.value) {
      params.buildingId = searchBuildingId.value
    }
    if (searchRoomId.value) {
      params.roomId = searchRoomId.value
    }
    const res = await request.get('/bed/assignmentPage', { params })
    console.log('返回数据:', res.data)
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total
      console.log('tableData:', tableData.value)
    }
  } catch (e) {
    console.error(e)
  }
}

const getBuildings = async () => {
  try {
    const res = await request.get('/building/list')
    buildingList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getRooms = async (buildingId) => {
  try {
    let url = '/room/list'
    if (buildingId) {
      url = `/room/building/${buildingId}`
    }
    const res = await request.get(url)
    allRoomList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getUnassignedStudents = async () => {
  try {
    const res = await request.get('/bed/unassignedStudents')
    unassignedStudents.value = res.data || []
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
    await getRooms()
  }
  handleSearch()
}

const handleReset = async () => {
  searchBuildingId.value = null
  searchRoomId.value = null
  searchStatus.value = null
  pageNum.value = 1
  await getRooms()
  getList()
}

const handleAssign = () => {
  getUnassignedStudents()
  dialogVisible.value = true
}

const handleSingleAssign = (row) => {
  assignForm.value = {
    bedId: row.id,
    buildingName: row.buildingName,
    roomNumber: row.roomNumber,
    bedNumber: row.bedNumber,
    studentId: null
  }
  getUnassignedStudents()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!assignForm.value.studentId) {
    ElMessage.warning('请选择学生')
    return
  }
  try {
    await request.post('/bed/assign', {
      bedId: assignForm.value.bedId,
      studentId: assignForm.value.studentId
    })
    ElMessage.success('分配成功')
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleRemove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要让学生 ${row.studentName} 退宿吗？`, '提示', { type: 'warning' })
    const res = await request.post('/bed/remove', { bedId: row.id })
    console.log('退宿结果:', res)
    ElMessage.success('退宿成功')
    searchBuildingId.value = null
    searchRoomId.value = null
    searchStatus.value = null
    pageNum.value = 1
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchRemove = async () => {
  const selectedWithStudent = selectedRows.value.filter(row => row.studentId)
  if (selectedWithStudent.length === 0) {
    ElMessage.warning('请选择已分配的学生进行退宿')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要批量退宿 ${selectedWithStudent.length} 名学生吗？`, '提示', { type: 'warning' })
    for (const row of selectedWithStudent) {
      await request.post('/bed/remove', { bedId: row.id })
    }
    ElMessage.success('批量退宿成功')
    searchBuildingId.value = null
    searchRoomId.value = null
    searchStatus.value = null
    pageNum.value = 1
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
    await request.post('/bed/assign/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('导入分配成功')
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
  getBuildings()
  getRooms()
})
</script>

<style scoped lang="scss">
.admin-assignment {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .search-bar {
    display: flex;
    gap: 10px;
    margin-top: 20px;
  }
}
</style>

<template>
  <div class="manager-utility">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>水电费管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账单管理" name="bills">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="宿舍">
              <el-select v-model="searchForm.roomId" placeholder="请选择宿舍" clearable style="width: 150px">
                <el-option v-for="item in roomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="月份">
              <el-date-picker
                v-model="searchForm.month"
                type="month"
                placeholder="选择月份"
                value-format="YYYY-MM"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.isPaid" placeholder="全部" clearable style="width: 120px">
                <el-option label="待缴纳" :value="0" />
                <el-option label="已缴纳" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">新增账单</el-button>
              <el-button type="warning" @click="showImportDialog">批量导入</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="tableData" style="width: 100%" v-loading="loading">
            <el-table-column prop="roomNumber" label="宿舍号" width="100" />
            <el-table-column label="月份" width="120">
              <template #default="{ row }">{{ row.year }}-{{ String(row.month).padStart(2, '0') }}</template>
            </el-table-column>
            <el-table-column label="用水量(吨)" width="140">
              <template #default="{ row }">
                <span :style="{ color: row.isWaterOver ? '#f56c6c' : '#606266', fontWeight: row.isWaterOver ? 'bold' : 'normal' }">
                  {{ row.waterUsage }}
                  <el-tag v-if="row.isWaterOver" type="danger" size="small">超限</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="用电量(度)" width="140">
              <template #default="{ row }">
                <span :style="{ color: row.isElectricOver ? '#f56c6c' : '#606266', fontWeight: row.isElectricOver ? 'bold' : 'normal' }">
                  {{ row.electricUsage }}
                  <el-tag v-if="row.isElectricOver" type="danger" size="small">超限</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="waterFee" label="水费(元)" width="100" />
            <el-table-column prop="electricFee" label="电费(元)" width="100" />
            <el-table-column prop="totalFee" label="总计(元)" width="100" />
            <el-table-column prop="isPaid" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isPaid === 0" type="warning">待缴纳</el-tag>
                <el-tag v-else type="success">已缴纳</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="费用统计" name="statistics">
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="6">
              <el-statistic title="宿舍总数" :value="statistics.totalRooms" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已缴费" :value="statistics.paidCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="待缴费" :value="statistics.unpaidCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总费用(元)" :value="statistics.totalFee" />
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="12">
              <el-card>
                <template #header>用水量统计</template>
                <div class="stat-item">
                  <span class="label">总用水量：</span>
                  <span class="value">{{ statistics.totalWaterUsage }} 吨</span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>用电量统计</template>
                <div class="stat-item">
                  <span class="label">总用电量：</span>
                  <span class="value">{{ statistics.totalElectricUsage }} 度</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card>
            <template #header>欠费名单</template>
            <el-table :data="statistics.unpaidList" style="width: 100%">
              <el-table-column prop="roomNumber" label="宿舍号" width="150" />
              <el-table-column label="月份" width="120">
                <template #default="{ row }">{{ row.year }}-{{ String(row.month).padStart(2, '0') }}</template>
              </el-table-column>
              <el-table-column prop="totalFee" label="欠费金额(元)" />
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="宿舍" required>
          <el-select v-model="form.roomId" placeholder="请选择宿舍" style="width: 100%">
            <el-option v-for="item in roomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份" required>
          <el-input-number v-model="form.year" :min="2020" :max="2030" />
        </el-form-item>
        <el-form-item label="月份" required>
          <el-input-number v-model="form.month" :min="1" :max="12" />
        </el-form-item>
        <el-form-item label="用水量(吨)">
          <el-input-number v-model="form.waterUsage" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="用电量(度)">
          <el-input-number v-model="form.electricUsage" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="importDialogVisible" title="批量导入水电费" width="500px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <template #default>
          请按以下格式填写Excel文件：<br>
          第1行表头：宿舍号 | 年份 | 月份 | 用水量(吨) | 用电量(度)<br>
          示例：101 | 2026 | 2 | 5.5 | 120<br>
          <b>注意：只能导入本楼栋的宿舍水电费数据</b>
        </template>
      </el-alert>
      
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        drag
        style="margin-bottom: 15px"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
      </el-upload>
      
      <div v-if="importResult" class="import-result">
        <el-alert
          v-if="importResult.successCount > 0"
          type="success"
          :title="'成功导入 ' + importResult.successCount + ' 条记录'"
          style="margin-bottom: 10px"
        />
        <el-alert
          v-if="importResult.errorCount > 0"
          type="error"
          :title="'导入失败 ' + importResult.errorCount + ' 条'"
        >
          <template #default>
            <div v-for="(err, idx) in importResult.errors" :key="idx">{{ err }}</div>
          </template>
        </el-alert>
      </div>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('bills')
const loading = ref(false)
const tableData = ref([])
const roomList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增账单')
const isEdit = ref(false)
const importDialogVisible = ref(false)
const importResult = ref(null)
const importFile = ref(null)
const importing = ref(false)
const uploadRef = ref(null)

const searchForm = reactive({
  roomId: null,
  month: '',
  isPaid: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  roomId: null,
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  waterUsage: 0,
  electricUsage: 0
})

const statistics = ref({
  totalRooms: 0,
  paidCount: 0,
  unpaidCount: 0,
  totalWaterUsage: 0,
  totalElectricUsage: 0,
  totalFee: 0,
  unpaidList: []
})

const getRoomList = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    const res = await request.get(`/room/all/building/${buildingId}`)
    roomList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const managerId = userStore.userInfo.userId
    let params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      buildingId: userStore.userInfo.buildingId
    }
    
    if (searchForm.roomId) {
      params.roomId = searchForm.roomId
    }
    if (searchForm.month) {
      const [year, month] = searchForm.month.split('-').map(Number)
      params.year = year
      params.month = month
    }
    if (searchForm.isPaid !== null && searchForm.isPaid !== '') {
      params.isPaid = searchForm.isPaid
    }
    
    const res = await request.get('/utility/bill/list', { params })
    const data = res.data
    
    const roomsRes = await request.get('/room/all/building/' + userStore.userInfo.buildingId)
    const rooms = roomsRes.data.records || roomsRes.data || []
    const roomMap = {}
    rooms.forEach(r => { roomMap[r.id] = r.roomNumber })
    
    if (data && data.records) {
      data.records.forEach(item => {
        item.roomNumber = roomMap[item.roomId] || '未知'
      })
      tableData.value = data.records
      pagination.total = data.total || 0
    } else if (Array.isArray(data)) {
      data.forEach(item => {
        item.roomNumber = roomMap[item.roomId] || '未知'
      })
      tableData.value = data
      pagination.total = data.length
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const getStatistics = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    const res = await request.get(`/utility/statistics/building/${buildingId}`)
    statistics.value = res.data || {
      totalRooms: 0,
      paidCount: 0,
      unpaidCount: 0,
      totalWaterUsage: 0,
      totalElectricUsage: 0,
      totalFee: 0,
      unpaidList: []
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleReset = () => {
  searchForm.roomId = null
  searchForm.month = ''
  searchForm.isPaid = null
  pagination.pageNum = 1
  getList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getList()
}

const handleAdd = () => {
  form.id = null
  form.roomId = null
  form.year = new Date().getFullYear()
  form.month = new Date().getMonth() + 1
  form.waterUsage = 0
  form.electricUsage = 0
  isEdit.value = false
  dialogTitle.value = '新增账单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.roomId = row.roomId
  form.year = row.year
  form.month = row.month
  form.waterUsage = row.waterUsage
  form.electricUsage = row.electricUsage
  isEdit.value = true
  dialogTitle.value = '编辑账单'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该账单吗？', '提示', { type: 'warning' })
    await request.delete(`/utility/bill/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const submitForm = async () => {
  if (!form.roomId) {
    ElMessage.warning('请选择宿舍')
    return
  }
  try {
    if (isEdit.value) {
      await request.put('/utility/bill', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/utility/bill', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getList()
    getStatistics()
  } catch (e) {
    console.error(e)
  }
}

const showImportDialog = () => {
  importResult.value = null
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
  importResult.value = null
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先上传文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('buildingId', userStore.userInfo.buildingId)
    
    const res = await request.post('/utility/bill/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (res.code === 200) {
      importResult.value = res.data
      if (res.data.successCount > 0) {
        ElMessage.success('导入成功')
        getList()
        getStatistics()
      }
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  getRoomList()
  getList()
  getStatistics()
})
</script>

<style scoped lang="scss">
.manager-utility {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 15px;
  }
  
  .stat-item {
    text-align: center;
    padding: 20px;
    
    .label {
      font-size: 14px;
      color: #666;
    }
    
    .value {
      font-size: 24px;
      font-weight: 600;
      color: #409eff;
    }
  }
}
</style>

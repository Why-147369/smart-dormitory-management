<template>
  <div class="admin-utility">
    <el-card>
      <template #header>
        <span>水电费管理</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="阈值设置" name="threshold">
          <el-alert
            title="阈值配置说明"
            type="info"
            :closable="false"
            style="margin-bottom: 15px"
          >
            <template #default>
              每个宿舍类型只能配置一套阈值，电费单价和水费单价将用于自动计算账单费用。
              4人间和6人间只能各配置一套，系统会自动根据宿舍类型应用对应配置。
            </template>
          </el-alert>
          
          <el-table :data="thresholdList" style="width: 100%">
            <el-table-column prop="roomType" label="宿舍类型" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.roomType === 4" type="success">4人间</el-tag>
                <el-tag v-else-if="row.roomType === 6" type="warning">6人间</el-tag>
                <el-tag v-else>{{ row.roomType }}人间</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="electricLimit" label="用电额度(度)" width="150" />
            <el-table-column prop="waterLimit" label="用水额度(吨)" width="150" />
            <el-table-column prop="electricPrice" label="电费单价(元/度)" width="150" />
            <el-table-column prop="waterPrice" label="水费单价(元/吨)" width="150" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleEditThreshold(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteThreshold(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div style="margin-top: 20px">
            <el-button 
              type="primary" 
              @click="handleAddThreshold" 
              :disabled="thresholdList.length >= 2"
            >
              新增阈值配置
            </el-button>
            <span v-if="thresholdList.length >= 2" style="margin-left: 10px; color: #999">
              (已达上限，每种宿舍类型仅支持一个配置)
            </span>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="全局统计" name="statistics">
          <el-row :gutter="20" style="margin-bottom: 20px;">
            <el-col :span="6">
              <el-statistic title="已缴费总数" :value="statistics.totalPaid" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="待缴费总数" :value="statistics.totalUnpaid" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总用水量(吨)" :value="statistics.totalWaterUsage" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总用电量(度)" :value="statistics.totalElectricUsage" />
            </el-col>
          </el-row>
          
          <el-card>
            <template #header>各楼栋水电统计</template>
            <el-table :data="statistics.buildingStats" style="width: 100%">
              <el-table-column prop="buildingName" label="楼栋" width="150" />
              <el-table-column prop="waterUsage" label="用水量(吨)" />
              <el-table-column prop="electricUsage" label="用电量(度)" />
              <el-table-column prop="paidCount" label="已缴费" />
              <el-table-column prop="unpaidCount" label="待缴费" />
            </el-table>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="账单管理" name="bills">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="楼栋">
              <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
                <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
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
              <el-button type="success" @click="handleExport">导出Excel</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="tableData" style="width: 100%" v-loading="loading">
            <el-table-column prop="roomNumber" label="宿舍号" width="100" />
            <el-table-column prop="buildingName" label="楼栋" width="100" />
            <el-table-column label="月份" width="120">
              <template #default="{ row }">{{ row.year }}-{{ String(row.month).padStart(2, '0') }}</template>
            </el-table-column>
            <el-table-column prop="waterUsage" label="用水量(吨)" width="120" />
            <el-table-column prop="electricUsage" label="用电量(度)" width="120" />
            <el-table-column prop="waterFee" label="水费(元)" width="100" />
            <el-table-column prop="electricFee" label="电费(元)" width="100" />
            <el-table-column prop="totalFee" label="总计(元)" width="100" />
            <el-table-column prop="isPaid" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isPaid === 0" type="warning">待缴纳</el-tag>
                <el-tag v-else type="success">已缴纳</el-tag>
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

        <el-tab-pane label="警告管理" name="warnings">
          <div style="margin-bottom: 10px;">
            <el-button type="primary" @click="handleRecheck" :loading="recheckLoading">重新检查阈值</el-button>
          </div>
          <div class="search-form">
            <el-form :inline="true" :model="warningSearchForm">
              <el-form-item label="楼栋">
                <el-select v-model="warningSearchForm.buildingId" placeholder="请选择" clearable>
                  <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="年份">
                <el-select v-model="warningSearchForm.year" placeholder="请选择" clearable>
                  <el-option v-for="item in yearOptions" :key="item" :label="item + '年'" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="月份">
                <el-select v-model="warningSearchForm.month" placeholder="请选择" clearable>
                  <el-option v-for="item in 12" :key="item" :label="item + '月'" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="warningSearchForm.status" placeholder="请选择" clearable>
                  <el-option label="未处理" :value="0" />
                  <el-option label="已处理" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadWarnings">搜索</el-button>
                <el-button @click="handleWarningReset">重置</el-button>
                <el-button type="success" @click="handleWarningExport">导出Excel</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="warningTableData" style="width: 100%" v-loading="warningLoading">
            <el-table-column prop="roomNumber" label="宿舍号" width="100" />
            <el-table-column prop="buildingName" label="楼栋" width="120" />
            <el-table-column prop="year" label="年份" width="80" />
            <el-table-column prop="month" label="月份" width="80">
              <template #default="{ row }">{{ row.month }}月</template>
            </el-table-column>
            <el-table-column label="用水量" width="120">
              <template #default="{ row }">
                <span :style="{ color: row.isWaterOver === 1 ? '#f56c6c' : '#606266' }">
                  {{ row.waterUsage }}吨
                  <el-tag v-if="row.isWaterOver === 1" type="danger" size="small">超限</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="用电量" width="120">
              <template #default="{ row }">
                <span :style="{ color: row.isElectricOver === 1 ? '#f56c6c' : '#606266' }">
                  {{ row.electricUsage }}度
                  <el-tag v-if="row.isElectricOver === 1" type="danger" size="small">超限</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">已处理</el-tag>
                <el-tag v-else type="warning">未处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDeleteWarning(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="warningPagination.pageNum"
            v-model:page-size="warningPagination.pageSize"
            :total="warningPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadWarnings"
            @current-change="loadWarnings"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="thresholdDialogVisible" :title="thresholdDialogTitle" width="500px">
      <el-form :model="thresholdForm" label-width="120px">
        <el-form-item label="宿舍类型" required>
          <el-select 
            v-model="thresholdForm.roomType" 
            placeholder="请选择" 
            style="width: 100%"
            :disabled="isEditThreshold"
          >
            <el-option 
              v-for="item in availableRoomTypes" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
              :disabled="item.disabled"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用电额度(度)">
          <el-input-number v-model="thresholdForm.electricLimit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用水额度(吨)">
          <el-input-number v-model="thresholdForm.waterLimit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="电费单价(元/度)">
          <el-input-number v-model="thresholdForm.electricPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="水费单价(元/吨)">
          <el-input-number v-model="thresholdForm.waterPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitThreshold">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const activeTab = ref('threshold')
const loading = ref(false)
const tableData = ref([])
const buildingList = ref([])
const yearOptions = ref([])

const loadYears = async () => {
  try {
    const res = await request.get('/utility/warning/years')
    yearOptions.value = res.data || []
    if (yearOptions.value.length > 0) {
      warningSearchForm.year = yearOptions.value[0]
    }
  } catch (e) {
    yearOptions.value = [2024, 2025, 2026]
    warningSearchForm.year = 2026
  }
}

const thresholdList = ref([])
const thresholdDialogVisible = ref(false)
const thresholdDialogTitle = ref('新增阈值配置')

const warningLoading = ref(false)
const recheckLoading = ref(false)
const warningTableData = ref([])
const warningPagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const warningSearchForm = reactive({
  buildingId: null,
  year: new Date().getFullYear(),
  month: null,
  status: null
})
const isEditThreshold = ref(false)

const searchForm = reactive({
  buildingId: null,
  month: '',
  isPaid: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const thresholdForm = reactive({
  id: null,
  roomType: 4,
  electricLimit: 0,
  waterLimit: 0,
  electricPrice: 0.5,
  waterPrice: 2.0
})

const statistics = ref({
  totalPaid: 0,
  totalUnpaid: 0,
  totalWaterUsage: 0,
  totalElectricUsage: 0,
  buildingStats: []
})

const availableRoomTypes = computed(() => {
  const types = []
  const has4 = thresholdList.value.some(t => t.roomType === 4)
  const has6 = thresholdList.value.some(t => t.roomType === 6)
  
  if (!has4) {
    types.push({ value: 4, label: '4人间', disabled: false })
  } else {
    types.push({ value: 4, label: '4人间(已配置)', disabled: true })
  }
  
  if (!has6) {
    types.push({ value: 6, label: '6人间', disabled: false })
  } else {
    types.push({ value: 6, label: '6人间(已配置)', disabled: true })
  }
  
  return types
})

const getBuildingList = async () => {
  try {
    const res = await request.get('/building/list')
    buildingList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getThresholdList = async () => {
  try {
    const res = await request.get('/utility/threshold/list')
    thresholdList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getStatistics = async () => {
  try {
    const res = await request.get('/utility/statistics/all')
    statistics.value = res.data || {
      totalPaid: 0,
      totalUnpaid: 0,
      totalWaterUsage: 0,
      totalElectricUsage: 0,
      buildingStats: []
    }
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  loading.value = true
  try {
    let params = {
      pageNum: 1,
      pageSize: 1000
    }
    
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
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
    
    let rooms = []
    if (searchForm.buildingId) {
      const roomsRes = await request.get('/room/all/building/' + searchForm.buildingId)
      rooms = roomsRes.data.records || roomsRes.data || []
    } else {
      for (const b of buildingList.value) {
        const roomsRes = await request.get('/room/all/building/' + b.id)
        rooms = rooms.concat(roomsRes.data.records || roomsRes.data || [])
      }
    }
    const roomMap = {}
    const buildingMap = {}
    rooms.forEach(r => { 
      roomMap[r.id] = r.roomNumber
      buildingMap[r.id] = r.buildingId
    })
    
    const buildingNameMap = {}
    buildingList.value.forEach(b => { buildingNameMap[b.id] = b.buildingName })
    
    let allData = []
    if (data && data.records) {
      allData = data.records
    } else if (Array.isArray(data)) {
      allData = data
    }
    
    allData.forEach(item => {
      item.roomNumber = roomMap[item.roomId] || '未知'
      const bid = buildingMap[item.roomId]
      item.buildingName = buildingNameMap[bid] || '未知'
    })
    
    const start = (pagination.pageNum - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    tableData.value = allData.slice(start, end)
    pagination.total = allData.length
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleReset = () => {
  searchForm.buildingId = null
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

const handleEditThreshold = (row) => {
  thresholdForm.id = row.id
  thresholdForm.roomType = row.roomType
  thresholdForm.electricLimit = row.electricLimit
  thresholdForm.waterLimit = row.waterLimit
  thresholdForm.electricPrice = row.electricPrice
  thresholdForm.waterPrice = row.waterPrice
  isEditThreshold.value = true
  thresholdDialogTitle.value = '编辑阈值配置'
  thresholdDialogVisible.value = true
}

const handleDeleteThreshold = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该阈值配置吗？', '提示', { type: 'warning' })
    await request.delete(`/utility/threshold/${row.id}`)
    ElMessage.success('删除成功')
    getThresholdList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleAddThreshold = () => {
  thresholdForm.id = null
  const has4 = thresholdList.value.some(t => t.roomType === 4)
  const has6 = thresholdList.value.some(t => t.roomType === 6)
  
  if (!has4) {
    thresholdForm.roomType = 4
  } else if (!has6) {
    thresholdForm.roomType = 6
  }
  
  thresholdForm.electricLimit = 100
  thresholdForm.waterLimit = 10
  thresholdForm.electricPrice = 0.5
  thresholdForm.waterPrice = 2.0
  isEditThreshold.value = false
  thresholdDialogTitle.value = '新增阈值配置'
  thresholdDialogVisible.value = true
}

const submitThreshold = async () => {
  try {
    if (isEditThreshold.value) {
      await request.put('/utility/threshold', thresholdForm)
      ElMessage.success('修改成功')
    } else {
      const existing = thresholdList.value.find(t => t.roomType === thresholdForm.roomType)
      if (existing) {
        ElMessage.warning('该类型配置已存在，请编辑现有配置')
        return
      }
      await request.post('/utility/threshold', thresholdForm)
      ElMessage.success('添加成功')
    }
    thresholdDialogVisible.value = false
    getThresholdList()
  } catch (e) {
    console.error(e)
  }
}

const handleExport = async () => {
  try {
    let params = {}
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.month) {
      const [year, month] = searchForm.month.split('-').map(Number)
      params.year = year
      params.month = month
    }
    if (searchForm.isPaid !== null && searchForm.isPaid !== '') {
      params.isPaid = searchForm.isPaid
    }
    
    const res = await request.get('/utility/bill/list', { params: { ...params, pageNum: 1, pageSize: 10000 } })
    const data = res.data
    let allData = []
    if (data && data.records) {
      allData = data.records
    } else if (Array.isArray(data)) {
      allData = data
    }
    
    let rooms = []
    if (searchForm.buildingId) {
      const roomsRes = await request.get('/room/all/building/' + searchForm.buildingId)
      rooms = roomsRes.data.records || roomsRes.data || []
    } else {
      for (const b of buildingList.value) {
        const roomsRes = await request.get('/room/all/building/' + b.id)
        rooms = rooms.concat(roomsRes.data.records || roomsRes.data || [])
      }
    }
    const roomMap = {}
    const buildingMap = {}
    rooms.forEach(r => { 
      roomMap[r.id] = r.roomNumber
      buildingMap[r.id] = r.buildingId
    })
    const buildingNameMap = {}
    buildingList.value.forEach(b => { buildingNameMap[b.id] = b.buildingName })
    
    const exportData = allData.map(item => ({
      '楼栋': buildingNameMap[buildingMap[item.roomId]] || '未知',
      '宿舍号': roomMap[item.roomId] || '未知',
      '年份': item.year,
      '月份': item.month,
      '用水量(吨)': item.waterUsage || 0,
      '用电量(度)': item.electricUsage || 0,
      '水费(元)': item.waterFee || 0,
      '电费(元)': item.electricFee || 0,
      '总计(元)': item.totalFee || 0,
      '状态': item.isPaid === 1 ? '已缴纳' : '待缴纳'
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '水电费账单')
    XLSX.writeFile(wb, `水电费账单_${new Date().toISOString().split('T')[0]}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const loadWarnings = async () => {
  warningLoading.value = true
  try {
    const params = {
      pageNum: warningPagination.value.pageNum,
      pageSize: warningPagination.value.pageSize
    }
    if (warningSearchForm.buildingId) params.buildingId = warningSearchForm.buildingId
    if (warningSearchForm.year) params.year = warningSearchForm.year
    if (warningSearchForm.month) params.month = warningSearchForm.month
    if (warningSearchForm.status !== null && warningSearchForm.status !== '') params.status = warningSearchForm.status
    
    const res = await request.get('/utility/warning/list', { params })
    warningTableData.value = res.data?.records || []
    warningPagination.value.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    warningLoading.value = false
  }
}

const handleWarningReset = () => {
  warningSearchForm.buildingId = null
  warningSearchForm.year = null
  warningSearchForm.month = null
  warningSearchForm.status = null
  warningPagination.value.pageNum = 1
  loadWarnings()
}

const handleDeleteWarning = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条警告记录吗？', '提示', { type: 'warning' })
    await request.delete(`/utility/warning/${row.id}`)
    ElMessage.success('删除成功')
    loadWarnings()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

const handleRecheck = async () => {
  recheckLoading.value = true
  try {
    const res = await request.post('/utility/warning/recheck')
    const result = res.data
    ElMessage.success(`检查完成！共检查${result.checked}条记录，新增${result.warningsCreated}条警告，移除${result.warningsRemoved}条`)
    loadWarnings()
  } catch (e) {
    console.error(e)
    ElMessage.error('重新检查失败')
  } finally {
    recheckLoading.value = false
  }
}

const handleWarningExport = async () => {
  try {
    const params = {}
    if (warningSearchForm.buildingId) params.buildingId = warningSearchForm.buildingId
    if (warningSearchForm.year) params.year = warningSearchForm.year
    if (warningSearchForm.month) params.month = warningSearchForm.month
    if (warningSearchForm.status !== null && warningSearchForm.status !== '') params.status = warningSearchForm.status
    
    const res = await request.get('/utility/warning/list', { params: { ...params, pageNum: 1, pageSize: 10000 } })
    const data = res.data?.records || []
    
    const exportData = data.map(item => ({
      '宿舍号': item.roomNumber,
      '楼栋': item.buildingName,
      '年份': item.year,
      '月份': item.month + '月',
      '用水量(吨)': item.waterUsage,
      '用水上限': item.waterLimit,
      '用电量(度)': item.electricUsage,
      '用电上限': item.electricLimit,
      '用水超限': item.isWaterOver === 1 ? '是' : '否',
      '用电超限': item.isElectricOver === 1 ? '是' : '否',
      '状态': item.status === 1 ? '已处理' : '未处理',
      '创建时间': item.createTime
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '超限警告')
    XLSX.writeFile(wb, `水电超限警告_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getBuildingList()
  getThresholdList()
  getStatistics()
  getList()
  loadWarnings()
  loadYears()
})

const handleTabChange = (tabName) => {
  if (tabName === 'warnings') {
    loadWarnings()
  }
}
</script>

<style scoped lang="scss">
.admin-utility {
  padding: 20px;
  
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

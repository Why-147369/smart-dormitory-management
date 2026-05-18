<template>
  <div class="admin-civilized">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="本月排行" name="current">
          <div class="action-bar">
            <el-button type="primary" @click="handleCalculate">计算本月得分</el-button>
          </div>
          
          <el-table :data="currentData" style="width: 100%" v-loading="loading">
            <el-table-column prop="buildingName" label="楼栋" width="150" />
            <el-table-column prop="roomNumber" label="最高分宿舍" width="150" />
            <el-table-column prop="totalScore" label="最高分" width="120">
              <template #default="{ row }">
                {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="历史记录" name="history">
          <div class="action-bar">
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="楼栋">
              <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
                <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="年份">
              <el-select v-model="searchForm.year" placeholder="请选择年份" clearable style="width: 120px">
                <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
              </el-select>
            </el-form-item>
            <el-form-item label="月份">
              <el-select v-model="searchForm.month" placeholder="请选择月份" clearable style="width: 120px">
                <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleExport">导出Excel</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="historyData" style="width: 100%" v-loading="historyLoading" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="buildingName" label="楼栋" width="150" />
            <el-table-column prop="roomNumber" label="宿舍号" width="120" />
            <el-table-column prop="totalScore" label="总分" width="100">
              <template #default="{ row }">
                {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="year" label="年份" width="100" />
            <el-table-column prop="month" label="月份" width="100">
              <template #default="{ row }">
                {{ row.month }}月
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="historyPageNum"
              v-model:page-size="historyPageSize"
              :total="historyTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="getHistoryList"
              @current-change="getHistoryList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import * as XLSX from 'xlsx'

const activeTab = ref('current')
const loading = ref(false)
const historyLoading = ref(false)
const currentData = ref([])
const historyData = ref([])
const selectedRows = ref([])
const historyTotal = ref(0)
const historyPageNum = ref(1)
const historyPageSize = ref(10)
const buildingList = ref([])

const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1
const yearOptions = ref([currentYear])

const getYearOptions = async () => {
  try {
    const res = await request.get('/civilized/years')
    if (Array.isArray(res.data) && res.data.length > 0) {
      yearOptions.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const searchForm = reactive({
  buildingId: null,
  year: null,
  month: null
})

const getBuildingList = async () => {
  try {
    const res = await request.get('/building/list')
    buildingList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getCurrentList = async () => {
  loading.value = true
  try {
    const params = {
      year: currentYear,
      month: currentMonth
    }
    const res = await request.get('/civilized/building-top', { params })
    const data = res.data
    if (Array.isArray(data)) {
      currentData.value = data
    } else if (data && data.records) {
      currentData.value = data.records
    } else {
      currentData.value = []
    }
  } catch (e) {
    console.error(e)
    currentData.value = []
  } finally {
    loading.value = false
  }
}

const getHistoryList = async () => {
  historyLoading.value = true
  try {
    const params = {
      pageNum: historyPageNum.value,
      pageSize: historyPageSize.value
    }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.year) {
      params.year = searchForm.year
    }
    if (searchForm.month) {
      params.month = searchForm.month
    }
    const res = await request.get('/civilized/building-top', { params })
    const data = res.data
    if (data && data.records) {
      historyData.value = data.records
      historyTotal.value = data.total || 0
    } else {
      historyData.value = []
      historyTotal.value = 0
    }
  } catch (e) {
    console.error(e)
    historyData.value = []
    historyTotal.value = 0
  } finally {
    historyLoading.value = false
  }
}

const handleSearch = () => {
  getHistoryList()
}

const handleReset = () => {
  searchForm.buildingId = null
  searchForm.year = null
  searchForm.month = null
  getHistoryList()
}

const handleCalculate = async () => {
  try {
    await ElMessageBox.confirm('确定要重新计算本月文明宿舍得分吗？', '提示', {
      type: 'warning'
    })
    await request.post('/civilized/calculate')
    ElMessage.success('计算完成')
    getCurrentList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleExport = async () => {
  const exportData = historyData.value.map(item => ({
    '楼栋': item.buildingName,
    '宿舍号': item.roomNumber,
    '总分': item.totalScore ? item.totalScore.toFixed(1) : '-',
    '年份': item.year,
    '月份': item.month + '月'
  }))
  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '文明宿舍历史记录')
  XLSX.writeFile(wb, `文明宿舍历史记录_${new Date().toISOString().split('T')[0]}.xlsx`)
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
    await request.delete(`/civilized/${row.id}`)
    ElMessage.success('删除成功')
    getHistoryList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条记录吗？`, '提示', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/civilized/batch-delete', ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    getHistoryList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  getYearOptions()
  getBuildingList()
  getCurrentList()
  getHistoryList()
})
</script>

<style scoped lang="scss">
.admin-civilized {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .action-bar {
    margin-bottom: 15px;
  }
  
  .search-form {
    margin-bottom: 15px;
  }
  
  .pagination {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

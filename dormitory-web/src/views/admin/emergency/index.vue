<template>
  <div class="admin-emergency">
    <el-card>
      <template #header>
        <span>紧急求助管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入宿舍号" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="已发送" :value="0" />
            <el-option label="已接收" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="已解决" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出Excel</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNumber" label="学号" width="120" />
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="content" label="求助内容" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="handleRemark" label="处理备注" min-width="120" />
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const buildingList = ref([])
const dateRange = ref([])

const searchForm = reactive({
  buildingId: null,
  roomNumber: null,
  status: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getBuildingList = async () => {
  try {
    const res = await request.get('/building/list')
    buildingList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getStatusType = (status) => {
  const types = ['', 'warning', 'primary', 'success']
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = ['已发送', '已接收', '处理中', '已解决']
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await request.get('/emergency/list', { params })
    let data = res.data
    if (data && data.records) {
      let list = data.records
      if (searchForm.roomNumber) {
        list = list.filter(item => item.roomNumber && item.roomNumber.includes(searchForm.roomNumber))
      }
      tableData.value = list
      pagination.total = data.total || 0
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

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleReset = () => {
  searchForm.buildingId = null
  searchForm.roomNumber = null
  searchForm.status = null
  searchForm.startDate = null
  searchForm.endDate = null
  dateRange.value = []
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

const handleExport = async () => {
  try {
    const params = { pageNum: 1, pageSize: 10000 }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await request.get('/emergency/list', { params })
    let allData = []
    if (res.data && res.data.records) {
      allData = res.data.records
      if (searchForm.roomNumber) {
        allData = allData.filter(item => item.roomNumber && item.roomNumber.includes(searchForm.roomNumber))
      }
    }
    
    const exportData = allData.map(item => ({
      '学生姓名': item.studentName || '',
      '学号': item.studentNumber || '',
      '楼栋': item.buildingName || '',
      '宿舍号': item.roomNumber || '',
      '求助内容': item.content || '',
      '状态': getStatusText(item.status),
      '发起时间': formatTime(item.createTime),
      '处理时间': formatTime(item.handleTime),
      '处理备注': item.handleRemark || ''
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '紧急求助记录')
    XLSX.writeFile(wb, `紧急求助记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getBuildingList()
  getList()
})
</script>

<style scoped lang="scss">
.admin-emergency {
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

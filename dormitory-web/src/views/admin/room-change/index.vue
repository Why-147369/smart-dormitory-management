<template>
  <div class="admin-room-change">
    <el-card>
      <template #header>
        <span>换寝管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出Excel</el-button>
          <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentName" label="申请人" width="100" />
        <el-table-column prop="studentNumber" label="学号" width="120" />
        <el-table-column prop="currentBuildingName" label="当前楼栋" width="100" />
        <el-table-column prop="currentRoomNumber" label="当前宿舍" width="100" />
        <el-table-column prop="currentBedNumber" label="当前床位" width="100" />
        <el-table-column prop="targetBuildingName" label="目标楼栋" width="100" />
        <el-table-column prop="targetRoomNumber" label="目标宿舍" width="100" />
        <el-table-column prop="targetBedNumber" label="目标床位" width="100" />
        <el-table-column prop="reason" label="换寝原因" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const buildingList = ref([])
const selectedRows = ref([])

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const searchForm = reactive({
  buildingId: null,
  status: null
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

const getList = async () => {
  loading.value = true
  try {
    let params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    
    const res = await request.get('/room/change/list', { params })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      pagination.total = data.total || 0
    } else if (Array.isArray(data)) {
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

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleReset = () => {
  searchForm.buildingId = null
  searchForm.status = null
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
    let params = { pageNum: 1, pageSize: 10000 }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    
    const res = await request.get('/room/change/list', { params })
    const data = res.data
    let allData = []
    if (data && data.records) {
      allData = data.records
    } else if (Array.isArray(data)) {
      allData = data
    }
    
    const exportData = allData.map(item => ({
      '申请人': item.studentName || '未知',
      '学号': item.studentNumber || '未知',
      '当前楼栋': item.currentBuildingName || '未知',
      '当前宿舍': item.currentRoomNumber || '未知',
      '当前床位': item.currentBedNumber || '未知',
      '目标楼栋': item.targetBuildingName || '未知',
      '目标宿舍': item.targetRoomNumber || '未知',
      '目标床位': item.targetBedNumber || '未知',
      '换寝原因': item.reason || '',
      '状态': item.status === 0 ? '待审批' : item.status === 1 ? '已通过' : '已拒绝',
      '拒绝原因': item.rejectReason || '',
      '申请时间': item.createTime || ''
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '换寝记录')
    XLSX.writeFile(wb, `换寝记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该换寝记录吗？', '提示', { type: 'warning' })
    await request.delete('/room/change/' + row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除选中的换寝记录吗？', '提示', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.delete('/room/change/batch', { data: ids })
    ElMessage.success('批量删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  getBuildingList()
  getList()
})
</script>

<style scoped>
.admin-room-change {
  padding: 20px;
}
.search-form {
  margin-bottom: 15px;
}
</style>

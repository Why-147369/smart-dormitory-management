<template>
  <div class="admin-checkin">
    <el-card>
      <template #header>
        <span>打卡管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡日期">
          <el-date-picker
            v-model="searchForm.checkDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出Excel</el-button>
          <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNumber" label="学号" width="120" />
        <el-table-column prop="buildingName" label="楼栋" width="80" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="checkDate" label="打卡日期" width="120" />
        <el-table-column prop="checkTime" label="打卡时间" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 0" type="warning">补卡</el-tag>
            <el-tag v-else type="info">未打卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isLate" label="是否迟到" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isLate === 1" type="danger">迟到</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import * as XLSX from 'xlsx'

const tableData = ref([])
const selectedRows = ref([])
const buildingList = ref([])
const loading = ref(false)

const searchForm = reactive({
  buildingId: null,
  checkDate: ''
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
    const res = await request.get('/checkin/list', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        buildingId: searchForm.buildingId || null,
        checkDate: searchForm.checkDate || null
      }
    })
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
  searchForm.checkDate = ''
  pagination.pageNum = 1
  getList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleExport = () => {
  const exportData = tableData.value.map(item => ({
    '学生姓名': item.studentName,
    '学号': item.studentNumber,
    '楼栋': item.buildingName,
    '宿舍号': item.roomNumber,
    '打卡日期': item.checkDate,
    '打卡时间': item.checkTime,
    '状态': item.status === 1 ? '正常' : '补卡',
    '是否迟到': item.isLate === 1 ? '迟到' : '正常'
  }))
  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '打卡记录')
  XLSX.writeFile(wb, `打卡记录_${new Date().toISOString().split('T')[0]}.xlsx`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条打卡记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/checkin/${row.id}`)
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
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条打卡记录吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(item => item.id).join(',')
    await request.delete(`/checkin/batch?ids=${ids}`)
    ElMessage.success('批量删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getList()
}

onMounted(() => {
  getBuildingList()
  getList()
})
</script>

<style scoped>
.admin-checkin {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>

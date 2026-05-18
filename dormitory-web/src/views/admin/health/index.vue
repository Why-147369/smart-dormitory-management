<template>
  <div class="admin-health">
    <el-card>
      <template #header>
        <span>卫生检查管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出Excel</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="score" label="得分" width="80">
          <template #default="{ row }">
            <el-tag :type="row.score >= 8 ? 'success' : row.score >= 6 ? 'warning' : 'danger'">
              {{ row.score }}分
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="检查备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <el-button v-if="row.images" type="primary" size="small" @click="showImages(row.images)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="检查人" width="100" />
        <el-table-column prop="checkDate" label="检查日期" width="120" />
        <el-table-column label="操作" width="80">
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
    
    <el-dialog v-model="imageDialogVisible" title="现场照片" width="600px">
      <div class="image-preview">
        <el-image 
          v-for="(img, index) in previewImages" 
          :key="index"
          :src="img" 
          :preview-src-list="previewImages"
          fit="cover"
          style="width: 150px; height: 150px; margin: 5px"
        />
      </div>
    </el-dialog>
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

const searchForm = reactive({
  buildingId: null
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
    
    const res = await request.get('/health/list', { params })
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
    
    const res = await request.get('/health/list', { params })
    const data = res.data
    let allData = []
    if (data && data.records) {
      allData = data.records
    } else if (Array.isArray(data)) {
      allData = data
    }
    
    const exportData = allData.map(item => ({
      '楼栋': item.buildingName || '未知',
      '宿舍号': item.roomNumber || '未知',
      '得分': item.score || 0,
      '检查备注': item.description || '',
      '检查人': item.managerName || '未知',
      '检查日期': item.checkDate || '',
      '创建时间': item.createTime || ''
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '卫生检查记录')
    XLSX.writeFile(wb, `卫生检查记录_${new Date().toISOString().split('T')[0]}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

const handleDelete = async (row) => {
  try {
    await request.delete(`/health/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

const imageDialogVisible = ref(false)
const previewImages = ref([])

const showImages = (images) => {
  previewImages.value = images.split(',')
  imageDialogVisible.value = true
}

onMounted(() => {
  getBuildingList()
  getList()
})
</script>

<style scoped>
.admin-health {
  padding: 20px;
}
.search-form {
  margin-bottom: 15px;
}
.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

<template>
  <div class="admin-lost-found">
    <el-card>
      <template #header>
        <span>失物招领管理</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="失物" :value="1" />
            <el-option label="拾物" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="物品类型">
          <el-select v-model="searchForm.itemType" placeholder="请选择物品类型" clearable style="width: 120px">
            <el-option v-for="t in itemTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待认领" :value="0" />
            <el-option label="已找到" :value="1" />
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
          <el-button type="danger" @click="handleDeleteClaimed">一键清除已认领</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image 
              v-if="getImages(row.images).length > 0" 
              :src="getImages(row.images)[0]" 
              style="width: 50px; height: 50px" 
              fit="cover" 
              :preview-src-list="getImages(row.images)"
              :z-index="2000"
              preview-teleported
              @error="(e) => e.target.style.display = 'none'"
            >
              <template #error>
                <div style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; color: #999; font-size: 12px;">无图</div>
              </template>
            </el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'danger' : 'success'">{{ row.type === 1 ? '失物' : '拾物' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="物品名称" width="120" />
        <el-table-column prop="itemType" label="物品类型" width="100" />
        <el-table-column prop="lostTime" label="丢失/拾取时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.lostTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lostPlace" label="地点" width="120" />
        <el-table-column prop="description" label="物品描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ row.status === 0 ? '待认领' : '已找到' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
const itemTypes = ref(['证件', '电子产品', '衣物', '书籍', '钱包', '钥匙', '其他'])
const dateRange = ref([])

const searchForm = reactive({
  type: null,
  itemType: null,
  status: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getItemTypes = async () => {
  try {
    const res = await request.get('/lost-found/types')
    if (Array.isArray(res.data) && res.data.length > 0) {
      itemTypes.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    const arr = JSON.parse(imagesStr)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.type !== null && searchForm.type !== '') {
      params.type = searchForm.type
    }
    if (searchForm.itemType) {
      params.itemType = searchForm.itemType
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await request.get('/lost-found/list', { params })
    if (res.data && res.data.records) {
      tableData.value = res.data.records
      pagination.total = res.data.total || 0
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
  searchForm.type = null
  searchForm.itemType = null
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

const handleDeleteClaimed = async () => {
  try {
    await ElMessageBox.confirm('确定要删除所有已认领（已找到）的失物招领记录吗？此操作不可恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    loading.value = true
    const res = await request.delete('/lost-found/delete-by-status/1')
    const deletedCount = res.data || 0
    
    if (deletedCount > 0) {
      ElMessage.success(`成功删除 ${deletedCount} 条已认领记录`)
    } else {
      ElMessage.info('暂无已认领的数据')
    }
    
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该失物招领信息吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/lost-found/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleExport = async () => {
  try {
    const params = { pageNum: 1, pageSize: 10000 }
    if (searchForm.type !== null && searchForm.type !== '') {
      params.type = searchForm.type
    }
    if (searchForm.itemType) {
      params.itemType = searchForm.itemType
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await request.get('/lost-found/list', { params })
    let allData = []
    if (res.data && res.data.records) {
      allData = res.data.records
    }
    
    const exportData = allData.map(item => ({
      '类型': item.type === 1 ? '失物' : '拾物',
      '物品名称': item.itemName || '',
      '物品类型': item.itemType || '',
      '丢失/拾取时间': formatTime(item.lostTime),
      '地点': item.lostPlace || '',
      '物品描述': item.description || '',
      '联系方式': item.contact || '',
      '状态': item.status === 0 ? '待认领' : '已找到',
      '发布人': item.publisherName || '',
      '发布时间': formatTime(item.createTime)
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '失物招领')
    XLSX.writeFile(wb, `失物招领_${new Date().toISOString().split('T')[0]}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getItemTypes()
  getList()
})
</script>

<style scoped lang="scss">
.admin-lost-found {
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

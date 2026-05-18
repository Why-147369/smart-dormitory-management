<template>
  <div class="student-message">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button v-if="unreadCount > 0" type="primary" link @click="handleReadAll">全部已读</el-button>
        </div>
      </template>
      
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="状态">
            <el-select v-model="searchForm.isRead" placeholder="请选择" clearable>
              <el-option label="未读" :value="0" />
              <el-option label="已读" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="messageContent" label="内容">
          <template #default="{ row }">
            <span :style="{ fontWeight: row.isRead === 0 ? 'bold' : 'normal' }">
              {{ row.messageContent || row.content }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRead === 0" type="danger">未读</el-tag>
            <el-tag v-else type="info">已读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" type="primary" size="small" @click="handleRead(row)">标记已读</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const unreadCount = ref(0)
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const searchForm = reactive({
  isRead: null
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.isRead !== null && searchForm.isRead !== '') {
      params.isRead = searchForm.isRead
    }
    const res = await request.get('/message/list', { params })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    
    const countRes = await request.get('/message/unread/count')
    unreadCount.value = countRes.data || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.isRead = null
  pagination.pageNum = 1
  loadData()
}

const handleRead = async (row) => {
  try {
    await request.put(`/message/read/${row.id}`)
    row.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('标记成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleReadAll = async () => {
  try {
    await request.put('/message/read/all')
    unreadCount.value = 0
    loadData()
    ElMessage.success('全部已读')
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', { type: 'warning' })
    await request.delete(`/message/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})

defineExpose({ unreadCount, loadData })
</script>

<style scoped lang="scss">
.student-message {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

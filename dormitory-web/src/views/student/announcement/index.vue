<template>
  <div class="announcement-page">
    <el-card>
      <template #header>
        <span>系统公告</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select v-model="searchForm.noticeType" placeholder="全部" clearable style="width: 120px">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="宿舍调整" value="宿舍调整" />
            <el-option label="安全提示" value="安全提示" />
            <el-option label="寒暑假" value="寒暑假" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.dateRange"
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
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="isTop" label="置顶" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="noticeType" label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.noticeType === '系统通知'" type="primary" size="small">系统通知</el-tag>
            <el-tag v-else-if="row.noticeType === '宿舍调整'" type="success" size="small">宿舍调整</el-tag>
            <el-tag v-else-if="row.noticeType === '安全提示'" type="warning" size="small">安全提示</el-tag>
            <el-tag v-else-if="row.noticeType === '寒暑假'" type="info" size="small">寒暑假</el-tag>
            <el-tag v-else size="small">{{ row.noticeType || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" title="公告详情" width="600px">
      <div v-if="currentAnnouncement">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h3>{{ currentAnnouncement.title }}</h3>
          <el-tag v-if="currentAnnouncement.isTop === 1" type="danger">置顶</el-tag>
        </div>
        <p style="color: #999; font-size: 14px;">
          分类：{{ currentAnnouncement.noticeType || '其他' }} &nbsp;&nbsp;|&nbsp;&nbsp; 
          发布时间：{{ currentAnnouncement.createTime }}
        </p>
        <el-divider />
        <div style="line-height: 2; white-space: pre-wrap;">{{ currentAnnouncement.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentAnnouncement = ref(null)

const searchForm = ref({
  noticeType: '',
  dateRange: []
})

const getList = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.value.noticeType) {
      params.noticeType = searchForm.value.noticeType
    }
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startTime = searchForm.value.dateRange[0]
      params.endTime = searchForm.value.dateRange[1]
    }
    
    const res = await request.get('/notice/list', { params: { pageNum: 1, pageSize: 100, ...params } })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
    } else if (Array.isArray(data)) {
      tableData.value = data
    } else {
      tableData.value = []
    }
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  getList()
}

const handleReset = () => {
  searchForm.value = { noticeType: '', dateRange: [] }
  getList()
}

const handleView = (row) => {
  currentAnnouncement.value = row
  dialogVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.announcement-page {
  padding: 20px;
  
  h3 {
    margin-bottom: 10px;
  }
  
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

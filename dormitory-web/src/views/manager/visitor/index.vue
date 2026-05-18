<template>
  <div class="manager-visitor">
    <el-card>
      <template #header>
        <span>访客管理</span>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审批" name="pending">
          <el-table :data="pendingData" style="width: 100%" v-loading="loading">
            <el-table-column prop="studentName" label="申请人" width="100" />
            <el-table-column prop="studentNumber" label="学号" width="120" />
            <el-table-column prop="roomNumber" label="宿舍号" width="80" />
            <el-table-column prop="visitorName" label="访客姓名" width="100" />
            <el-table-column label="性别" width="60">
              <template #default="{ row }">
                {{ row.gender === 1 ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="source" label="来源地" width="120" />
            <el-table-column prop="visitTime" label="到访时间" width="160" />
            <el-table-column prop="purpose" label="到访目的" min-width="150" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleApprove(row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="全部记录" name="all">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已取消" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="tableData" style="width: 100%" v-loading="loading">
            <el-table-column prop="studentName" label="申请人" width="100" />
            <el-table-column prop="studentNumber" label="学号" width="120" />
            <el-table-column prop="roomNumber" label="宿舍号" width="80" />
            <el-table-column prop="visitorName" label="访客姓名" width="100" />
            <el-table-column label="性别" width="60">
              <template #default="{ row }">
                {{ row.gender === 1 ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="source" label="来源地" width="120" />
            <el-table-column prop="visitTime" label="到访时间" width="160" />
            <el-table-column prop="purpose" label="到访目的" min-width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
                <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
                <el-tag v-else type="info">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 1" 
                  type="success" 
                  size="small" 
                  @click="handleComplete(row)"
                >
                  完成
                </el-button>
                <span v-else>-</span>
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
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('pending')
const loading = ref(false)
const pendingData = ref([])
const tableData = ref([])
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRow = ref(null)

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getPendingList = async () => {
  loading.value = true
  try {
    const res = await request.get('/visitor/list', {
      params: { buildingId: userStore.userInfo.buildingId, status: 0 }
    })
    pendingData.value = res.data?.records || res.data || []
  } catch (e) {
    console.error(e)
    pendingData.value = []
  } finally {
    loading.value = false
  }
}

const getList = async () => {
  loading.value = true
  try {
    let params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      buildingId: userStore.userInfo.buildingId
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    const res = await request.get('/visitor/list', { params })
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

const handleApprove = async (row) => {
  try {
    await request.put(`/visitor/approve/${row.id}`)
    ElMessage.success('审批通过')
    getPendingList()
    if (activeTab.value === 'all') {
      getList()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '操作失败')
  }
}

const handleReject = (row) => {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await request.put(`/visitor/reject/${currentRow.value.id}`, null, {
      params: { reason: rejectReason.value }
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    getPendingList()
    if (activeTab.value === 'all') {
      getList()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '操作失败')
  }
}

const handleComplete = async (row) => {
  try {
    await request.put(`/visitor/complete/${row.id}`)
    ElMessage.success('标记完成')
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '操作失败')
  }
}

watch(activeTab, (val) => {
  if (val === 'pending') {
    getPendingList()
  } else {
    getList()
  }
})

onMounted(() => {
  getPendingList()
})
</script>

<style scoped>
.manager-visitor {
  padding: 20px;
}
.search-form {
  margin-bottom: 15px;
}
</style>

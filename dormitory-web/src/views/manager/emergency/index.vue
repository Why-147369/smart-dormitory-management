<template>
  <div class="manager-emergency">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>紧急求助管理</span>
          <el-button type="primary" @click="getList">刷新</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="handleReceive(row)">接收</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="handleProcessing(row)">处理中</el-button>
            <el-button v-if="row.status !== 3" link type="success" @click="handleResolve(row)">已解决</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" title="紧急求助提醒" width="400px" :close-on-click-modal="false">
      <div class="emergency-alert">
        <p><strong>学生姓名：</strong>{{ currentHelp.studentName }}</p>
        <p><strong>宿舍：</strong>{{ currentHelp.buildingName }} - {{ currentHelp.roomNumber }}</p>
        <p><strong>求助内容：</strong>{{ currentHelp.content }}</p>
        <p><strong>发起时间：</strong>{{ formatTime(currentHelp.createTime) }}</p>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleReceive(currentHelp)">接收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const currentHelp = ref({})
const userStore = useUserStore()
const buildingId = userStore.userInfo?.buildingId
let timer = null

const searchForm = reactive({
  roomNumber: null,
  status: null
})

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
    const params = { pageNum: 1, pageSize: 100 }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    
    const res = await request.get('/emergency/list', { params })
    let data = res.data
    if (data && data.records) {
      let list = data.records
      if (buildingId) {
        list = list.filter(item => item.buildingId === buildingId)
      }
      if (searchForm.roomNumber) {
        list = list.filter(item => item.roomNumber && item.roomNumber.includes(searchForm.roomNumber))
      }
      tableData.value = list
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

const checkNewEmergency = async () => {
  try {
    const params = { status: 0 }
    if (buildingId) {
      params.buildingId = buildingId
    }
    const res = await request.get('/emergency/list', { params })
    if (res.data && res.data.records && res.data.records.length > 0) {
      const help = res.data.records[0]
      if (!currentHelp.value.id || currentHelp.value.id !== help.id) {
        currentHelp.value = help
        dialogVisible.value = true
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  getList()
}

const handleReset = () => {
  searchForm.roomNumber = null
  searchForm.status = null
  getList()
}

const handleReceive = async (row) => {
  try {
    await request.put(`/emergency/receive/${row.id}`)
    ElMessage.success('已接收')
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleProcessing = async (row) => {
  try {
    await request.put(`/emergency/handle/${row.id}`)
    ElMessage.success('已标记为处理中')
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleResolve = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理备注', '标记为已解决', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.put(`/emergency/resolve/${row.id}?remark=${encodeURIComponent(value || '')}`)
    ElMessage.success('已解决')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  getList()
  timer = setInterval(checkNewEmergency, 5000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.manager-emergency {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 15px;
  }
  
  .emergency-alert {
    p {
      margin: 10px 0;
      font-size: 14px;
    }
  }
}
</style>

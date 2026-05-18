<template>
  <div class="manager-checkin">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>打卡管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="打卡记录" name="records">
          <div class="search-bar">
            <el-date-picker
              v-model="searchDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              clearable
            />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </div>
          
          <el-table :data="tableData" style="width: 100%; margin-top: 20px;" v-loading="loading">
            <el-table-column prop="studentNumber" label="学号" width="150" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="checkDate" label="打卡日期" width="120" />
            <el-table-column prop="checkTime" label="打卡时间" width="120" />
            <el-table-column prop="isLate" label="是否迟到" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isLate === 1 ? 'warning' : 'success'">
                  {{ row.isLate === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">正常</el-tag>
                <el-tag v-else-if="row.status === 0" type="warning">补卡</el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            style="margin-top: 20px; text-align: right;"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            @current-change="getList"
            @size-change="getList"
            layout="total, sizes, prev, pager, next"
          />
        </el-tab-pane>
        
        <el-tab-pane label="补打卡申请" name="apply">
          <el-table :data="applyTableData" style="width: 100%; margin-top: 20px;" v-loading="applyLoading">
            <el-table-column prop="studentNumber" label="学号" width="150" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="applyDate" label="申请补卡日期" width="150" />
            <el-table-column prop="reason" label="补卡原因" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="primary" size="small" @click="handleApprove(row)">通过</el-button>
                <el-button v-if="row.status === 0" type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="打卡统计" name="statistics">
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="6">
              <el-statistic title="今日应打卡人数" :value="statistics.total" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已打卡人数" :value="statistics.checkedIn" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="未打卡人数" :value="statistics.notCheckedIn" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="打卡率" :value="statistics.rate.toFixed(1)" suffix="%" />
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('records')
const loading = ref(false)
const applyLoading = ref(false)
const tableData = ref([])
const applyTableData = ref([])
const searchDate = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref({ total: 0, checkedIn: 0, notCheckedIn: 0, rate: 0 })

const rejectDialogVisible = ref(false)
const rejectForm = ref({ id: null, reason: '' })

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchDate.value) {
      params.checkDate = searchDate.value
    }
    const buildingId = userStore.userInfo?.buildingId
    if (buildingId) {
      params.buildingId = buildingId
    }
    const res = await request.get('/checkin/list', { params })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getApplyList = async () => {
  applyLoading.value = true
  try {
    const buildingId = userStore.userInfo?.buildingId
    const res = await request.get('/checkin/apply/list', {
      params: { pageNum: 1, pageSize: 100, buildingId }
    })
    const data = res.data
    if (data && data.records) {
      applyTableData.value = data.records
    }
  } catch (e) {
    console.error(e)
  } finally {
    applyLoading.value = false
  }
}

const getStatistics = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    if (!buildingId) return
    
    const today = new Date().toISOString().split('T')[0]
    
    const studentRes = await request.get('/student/list', { 
      params: { buildingId, pageSize: 1000 } 
    })
    const totalStudents = studentRes.data?.total || 0
    
    const checkInRes = await request.get('/checkin/list', {
      params: { buildingId, checkDate: today, pageSize: 1000 }
    })
    const checkedInCount = checkInRes.data?.records?.length || 0
    
    statistics.value.total = totalStudents
    statistics.value.checkedIn = checkedInCount
    statistics.value.notCheckedIn = totalStudents - checkedInCount
    statistics.value.rate = totalStudents > 0 
      ? Math.round((checkedInCount / totalStudents) * 10000) / 100 
      : 0
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  getList()
}

const handleReset = () => {
  searchDate.value = null
  pageNum.value = 1
  getList()
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过该补打卡申请吗？', '提示', { type: 'info' })
    await request.put(`/checkin/apply/approve/${row.id}`)
    ElMessage.success('审批通过')
    getApplyList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleReject = (row) => {
  rejectForm.value.id = row.id
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await request.put(`/checkin/apply/reject/${rejectForm.value.id}?reason=${encodeURIComponent(rejectForm.value.reason)}`)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    getApplyList()
  } catch (e) {
    console.error(e)
  }
}

const handleExport = async () => {
  try {
    const token = sessionStorage.getItem('manager_token')
    const buildingId = userStore.userInfo?.buildingId
    const params = { buildingId }
    if (searchDate.value) params.checkDate = searchDate.value
    const response = await axios.get('/api/checkin/export', {
      params,
      responseType: 'blob',
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '打卡记录.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getList()
  getApplyList()
  getStatistics()
})
</script>

<style scoped lang="scss">
.manager-checkin {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-bar {
    display: flex;
    gap: 10px;
  }
}
</style>

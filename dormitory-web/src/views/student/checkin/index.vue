<template>
  <div class="student-checkin">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: hasCheckedIn ? 'linear-gradient(135deg, #67c23a 0%, #85ce61 100%)' : 'linear-gradient(135deg, #909399 0%, #a6a9ad 100%)' }">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ hasCheckedIn ? '已打卡' : '未打卡' }}</div>
              <div class="stat-label">今日状态</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);">
              <el-icon :size="28"><Calendar /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ checkInCount }}</div>
              <div class="stat-label">本月打卡</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ applyCount }}</div>
              <div class="stat-label">补卡申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card class="checkin-card">
          <div class="checkin-main">
            <div class="time-display">
              <div class="current-time">{{ currentTime }}</div>
              <div class="current-date">{{ currentDate }}</div>
            </div>
            
            <div class="checkin-action">
              <div v-if="hasCheckedIn" class="checkin-success">
                <el-icon :size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
                <div class="success-text">已打卡</div>
                <div class="checkin-time">{{ checkInTime }}</div>
              </div>
              <div v-else>
                <el-button type="primary" size="large" class="checkin-btn" @click="handleCheckIn">
                  <el-icon><Clock /></el-icon>立即打卡
                </el-button>
              </div>
              
              <el-button v-if="hasCheckedIn" type="warning" size="small" @click="handleApplyLate">
                申请补打卡
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="record-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="打卡记录" name="checkin">
              <el-table :data="tableData" style="width: 100%" max-height="300">
                <el-table-column prop="checkInDate" label="日期" width="120" />
                <el-table-column prop="checkInTime" label="打卡时间" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 1" type="success">正常</el-tag>
                    <el-tag v-else-if="row.status === 0" type="warning">补卡</el-tag>
                    <el-tag v-else type="info">未打卡</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="补卡申请" name="apply">
              <el-table :data="applyTableData" style="width: 100%" max-height="300">
                <el-table-column prop="applyDate" label="申请日期" width="120" />
                <el-table-column prop="reason" label="补卡原因" min-width="150" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
                    <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                    <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="rejectReason" label="拒绝原因" min-width="150">
                  <template #default="{ row }">
                    {{ row.rejectReason || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
    
    <el-dialog v-model="applyDialogVisible" title="申请补打卡" width="400px">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="补卡日期">
          <el-date-picker v-model="applyForm.applyDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="补卡原因">
          <el-input v-model="applyForm.reason" type="textarea" placeholder="请输入补卡原因" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { CircleCheckFilled, Clock, Calendar, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()

const currentTime = ref('')
const currentDate = ref('')
const hasCheckedIn = ref(false)
const checkInTime = ref('')
const checkInCount = ref(0)
const applyCount = ref(0)
const tableData = ref([])
const applyTableData = ref([])
const activeTab = ref('checkin')
const applyDialogVisible = ref(false)
const applyForm = ref({
  applyDate: '',
  reason: ''
})

let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit',
    second: '2-digit'
  })
  currentDate.value = now.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
}

const getCheckInStatus = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/checkin/status/${userId}`)
    if (res.data && res.data.studentId) {
      hasCheckedIn.value = true
      checkInTime.value = res.data.checkInTime
    }
  } catch (e) {
    console.error(e)
  }
}

const getCheckInList = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/checkin/history/${userId}`)
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      checkInCount.value = data.total || data.records.length
    } else if (Array.isArray(data)) {
      tableData.value = data
      checkInCount.value = data.length
    }
  } catch (e) {
    console.error(e)
  }
}

const getApplyList = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get('/checkin/apply/my', {
      params: { studentId: userId }
    })
    const data = res.data
    if (data && data.records) {
      applyTableData.value = data.records
      applyCount.value = data.total || data.records.length
    } else if (Array.isArray(data)) {
      applyTableData.value = data
      applyCount.value = data.length
    }
  } catch (e) {
    console.error(e)
  }
}

const handleCheckIn = async () => {
  try {
    const userId = userStore.userInfo.userId
    await request.post('/checkin/record', null, { params: { studentId: userId } })
    ElMessage.success('打卡成功')
    hasCheckedIn.value = true
    const now = new Date()
    checkInTime.value = now.toTimeString().split(' ')[0]
    getCheckInList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '打卡失败')
  }
}

const handleApplyLate = () => {
  applyForm.value.applyDate = ''
  applyForm.value.reason = ''
  applyDialogVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.value.applyDate) {
    ElMessage.warning('请选择补卡日期')
    return
  }
  if (!applyForm.value.reason) {
    ElMessage.warning('请输入补卡原因')
    return
  }
  try {
    await request.post('/checkin/apply', {
      studentId: userStore.userInfo.userId,
      applyDate: applyForm.value.applyDate,
      reason: applyForm.value.reason
    })
    ElMessage.success('申请已提交')
    applyDialogVisible.value = false
    getApplyList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '提交失败')
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  getCheckInStatus()
  getCheckInList()
  getApplyList()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.student-checkin {
  padding: 16px;
  
  .stat-row {
    margin-bottom: 16px;
    
    .stat-card {
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
      }
      
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          margin-right: 16px;
          transition: transform 0.3s ease;
        }
        
        &:hover .stat-icon {
          transform: scale(1.1) rotate(5deg);
        }
        
        .stat-text {
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #333;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 2px;
          }
        }
      }
    }
  }
  
  .checkin-card {
    .checkin-main {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 0;
      
      .time-display {
        text-align: center;
        margin-bottom: 24px;
        
        .current-time {
          font-size: 36px;
          font-weight: 700;
          color: #303133;
          animation: timePulse 1s ease-in-out infinite;
        }
        
        @keyframes timePulse {
          0%, 100% {
            opacity: 1;
          }
          50% {
            opacity: 0.8;
          }
        }
        
        .current-date {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
      
      .checkin-action {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16px;
        
        .checkin-success {
          text-align: center;
          animation: bounceIn 0.6s ease;
          
          @keyframes bounceIn {
            0% {
              transform: scale(0);
              opacity: 0;
            }
            50% {
              transform: scale(1.2);
            }
            100% {
              transform: scale(1);
              opacity: 1;
            }
          }
          
          .success-icon {
            animation: successPop 0.5s ease;
            
            @keyframes successPop {
              0% {
                transform: scale(0);
              }
              50% {
                transform: scale(1.3);
              }
              100% {
                transform: scale(1);
              }
            }
          }
          
          .success-text {
            font-size: 18px;
            font-weight: 600;
            color: #67c23a;
            margin-top: 8px;
          }
          
          .checkin-time {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
        
        .checkin-btn {
          width: 160px;
          height: 48px;
          font-size: 18px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
          }
          
          &:active {
            transform: translateY(0);
          }
        }
      }
    }
  }
  
  .record-card {
    min-height: 400px;
  }
}
</style>

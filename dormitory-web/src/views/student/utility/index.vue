<template>
  <div class="student-utility">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">¥{{ unpaidAmount }}</div>
              <div class="stat-label">待缴费用</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ paidCount }}</div>
              <div class="stat-label">已缴费</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ unpaidCount }}</div>
              <div class="stat-label">未缴费</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="info-card">
      <template #header>
        <span>当前宿舍信息</span>
      </template>
      <el-descriptions :column="3" border v-if="roomInfo">
        <el-descriptions-item label="楼栋">{{ roomInfo.buildingName }}</el-descriptions-item>
        <el-descriptions-item label="宿舍号">{{ roomInfo.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="床位号">{{ roomInfo.bedNumber }}号床</el-descriptions-item>
      </el-descriptions>
      <div v-else class="no-room">暂无宿舍信息</div>
    </el-card>

    <el-card style="margin-top: 15px">
      <template #header>
        <span>水电费账单</span>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="月份">
          <el-date-picker
            v-model="searchForm.month"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isPaid" placeholder="全部" clearable style="width: 120px">
            <el-option label="待缴纳" :value="0" />
            <el-option label="已缴纳" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column label="月份" width="120">
          <template #default="{ row }">{{ row.year }}-{{ String(row.month).padStart(2, '0') }}</template>
        </el-table-column>
        <el-table-column prop="waterUsage" label="用水量(吨)" width="120" />
        <el-table-column prop="electricUsage" label="用电量(度)" width="120" />
        <el-table-column prop="waterFee" label="水费(元)" width="100">
          <template #default="{ row }">{{ row.waterFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="electricFee" label="电费(元)" width="100">
          <template #default="{ row }">{{ row.electricFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="totalFee" label="总计(元)" width="100">
          <template #default="{ row }">{{ row.totalFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="isPaid" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isPaid === 0" type="warning">待缴纳</el-tag>
            <el-tag v-else type="success">已缴纳</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" min-width="160">
          <template #default="{ row }">
            {{ row.payTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.isPaid === 0" 
              type="primary" 
              size="small"
              @click="handlePay(row)"
            >
              立即缴费
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="payDialogVisible" title="扫码支付" width="420px" center :close-on-click-modal="false">
      <div class="pay-dialog">
        <div class="qr-code">
          <div class="qr-placeholder">
            <el-icon :size="80"><CreditCard /></el-icon>
            <div>二维码</div>
          </div>
        </div>
        <div class="pay-amount">
          需支付：<span class="amount">¥{{ currentPay.totalFee }}</span>
        </div>
        <div class="pay-tip">
          <span v-if="countdown > 0">请在 {{ countdown }} 秒内完成支付</span>
          <span v-else class="timeout">支付超时，请重新发起</span>
        </div>
        <div class="pay-method">
          <el-radio-group v-model="payMethod">
            <el-radio label="wechat">微信支付</el-radio>
            <el-radio label="alipay">支付宝</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelPay">取消</el-button>
        <el-button type="primary" @click="confirmPay" :disabled="countdown <= 0">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { CreditCard, Money, CircleCheck, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()

const tableData = ref([])
const roomInfo = ref(null)
const payDialogVisible = ref(false)
const currentPay = ref({})
const payMethod = ref('wechat')
const countdown = ref(15)
let countdownTimer = null

const unpaidAmount = ref(0)
const paidCount = ref(0)
const unpaidCount = ref(0)

const searchForm = reactive({
  month: '',
  isPaid: null
})

const getRoomInfo = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/student/${userId}`)
    if (res.data) {
      roomInfo.value = {
        buildingName: res.data.buildingName || '未知',
        roomNumber: res.data.roomNumber || '未知',
        bedNumber: res.data.bedNumber || '-'
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/utility/bill/student/${userId}`)
    let data = res.data || []
    
    if (searchForm.month) {
      const [year, month] = searchForm.month.split('-').map(Number)
      data = data.filter(item => item.year === year && item.month === month)
    }
    if (searchForm.isPaid !== null && searchForm.isPaid !== '') {
      data = data.filter(item => item.isPaid === searchForm.isPaid)
    }
    
    tableData.value = data
    
    const unpaid = data.filter(item => item.isPaid === 0)
    const paid = data.filter(item => item.isPaid === 1)
    unpaidAmount.value = unpaid.reduce((sum, item) => sum + (item.totalFee || 0), 0)
    unpaidCount.value = unpaid.length
    paidCount.value = paid.length
  } catch (e) {
    console.error(e)
    tableData.value = []
  }
}

const handleSearch = () => {
  getList()
}

const handleReset = () => {
  searchForm.month = ''
  searchForm.isPaid = null
  getList()
}

const handlePay = (row) => {
  currentPay.value = row
  payMethod.value = 'wechat'
  payDialogVisible.value = true
  startCountdown()
}

const startCountdown = () => {
  countdown.value = 15
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

const cancelPay = () => {
  payDialogVisible.value = false
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const confirmPay = async () => {
  if (countdown.value <= 0) {
    ElMessage.warning('支付超时，请重新发起')
    return
  }
  try {
    await request.post(`/utility/bill/pay/${currentPay.value.id}`, null, { 
      params: { payMethod: payMethod.value } 
    })
    ElMessage.success('支付成功')
    cancelPay()
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '支付失败')
  }
}

onMounted(() => {
  getRoomInfo()
  getList()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped lang="scss">
.student-utility {
  padding: 16px;
  
  .stat-row {
    margin-bottom: 16px;
    
    .stat-card {
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
  
  .info-card {
    .no-room {
      color: #999;
      text-align: center;
      padding: 20px;
    }
  }
  
  .search-form {
    margin-bottom: 15px;
  }
  
  .pay-dialog {
    text-align: center;
    
    .qr-code {
      margin: 20px 0;
      
      .qr-placeholder {
        width: 150px;
        height: 150px;
        margin: 0 auto;
        background: #f5f7fa;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #999;
      }
    }
    
    .pay-amount {
      font-size: 16px;
      margin-bottom: 10px;
      
      .amount {
        font-size: 24px;
        font-weight: 600;
        color: #f56c6c;
      }
    }
    
    .pay-tip {
      font-size: 12px;
      color: #999;
      margin-bottom: 15px;
      
      .timeout {
        color: #f56c6c;
      }
    }
    
    .pay-method {
      margin-top: 10px;
    }
  }
}
</style>

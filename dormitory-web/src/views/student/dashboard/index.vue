<template>
  <div class="student-dashboard">
    <el-row :gutter="12" class="action-row">
      <el-col :span="4">
        <el-button type="primary" class="action-btn" @click="$router.push('/student/checkin')">
          <el-icon><Clock /></el-icon>打卡
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="warning" class="action-btn" @click="$router.push('/student/repair')">
          <el-icon><Tools /></el-icon>报修
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="success" class="action-btn" @click="$router.push('/student/utility')">
          <el-icon><Money /></el-icon>水电费
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="info" class="action-btn" @click="$router.push('/student/announcement')">
          <el-icon><Bell /></el-icon>公告
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="danger" class="action-btn" @click="$router.push('/student/lost-found')">
          <el-icon><Search /></el-icon>失物招领
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="danger" class="action-btn" @click="handleEmergencyHelp">
          <el-icon><Warning /></el-icon>紧急求助
        </el-button>
      </el-col>
    </el-row>
    
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/student/checkin')">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ checkInStatus }}</div>
              <div class="stat-label">今日打卡</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/student/repair')">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);">
              <el-icon :size="28"><Tools /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ repairCount }} <span class="unit">单</span></div>
              <div class="stat-label">我的报修</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/student/utility')">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">¥{{ utilityBill }}</div>
              <div class="stat-label">水电费待缴</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-emergency" shadow="hover" @click="handleEmergencyHelp">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">紧急</div>
              <div class="stat-label">求助</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="16" class="main-row">
      <el-col :span="10">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <span>📋 宿舍信息</span>
          </template>
          <div class="dormitory-info">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">楼栋</span>
                <span class="value">{{ userStore.userInfo.buildingName || '未分配' }}</span>
              </div>
              <div class="info-item">
                <span class="label">宿舍</span>
                <span class="value">{{ userStore.userInfo.roomNumber || '未分配' }}</span>
              </div>
              <div class="info-item">
                <span class="label">床位</span>
                <span class="value">{{ userStore.userInfo.bedNumber ? '第' + userStore.userInfo.bedNumber + '床' : '未分配' }}</span>
              </div>
              <div class="info-item">
                <span class="label">姓名</span>
                <span class="value">{{ userStore.userInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">学院</span>
                <span class="value">{{ userStore.userInfo.college || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">专业</span>
                <span class="value">{{ userStore.userInfo.major || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">班级</span>
                <span class="value">{{ userStore.userInfo.className || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">学号</span>
                <span class="value">{{ userStore.userInfo.studentNumber || '-' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card class="lost-found-card" shadow="hover" @click="$router.push('/student/lost-found')">
          <template #header>
            <div class="card-header">
              <span>🎁 失物招领</span>
              <el-tag type="warning" size="small">{{ lostFoundList.length }}条待认领</el-tag>
            </div>
          </template>
          <div v-if="lostFoundList.length > 0" class="carousel-wrapper">
            <div class="carousel-item">
              <div class="item-header">
                <el-tag :type="currentItem.type === 1 ? 'danger' : 'success'" size="large">
                  {{ currentItem.type === 1 ? '🔴 失物' : '🟢 拾物' }}
                </el-tag>
                <span class="item-time">{{ formatTime(currentItem.lostTime) }}</span>
              </div>
              <div class="item-body">
                <div class="item-name">{{ currentItem.itemName }}</div>
                <div class="item-details">
                  <div class="detail-row">
                    <span class="label">物品类型：</span>
                    <span class="value">{{ currentItem.itemType }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">丢失地点：</span>
                    <span class="value">{{ currentItem.lostPlace || '未知' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">物品描述：</span>
                    <span class="value">{{ currentItem.description || '无描述' }}</span>
                  </div>
                  <div class="detail-row contact-row">
                    <span class="label">联系方式：</span>
                    <span class="value phone">{{ currentItem.contact }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="carousel-indicators">
              <span 
                v-for="(item, index) in lostFoundList" 
                :key="index"
                class="indicator"
                :class="{ active: index === currentIndex }"
                @click.stop="currentIndex = index"
              ></span>
            </div>
          </div>
          <el-empty v-else description="暂无失物招领信息，点击可发布" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Clock, Tools, Money, Warning, Bell, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()

const checkInStatus = ref('未打卡')
const repairCount = ref(0)
const utilityBill = ref(0)

const lostFoundList = ref([])
const currentIndex = ref(0)
let carouselTimer = null

const currentItem = computed(() => {
  if (lostFoundList.value.length > 0) {
    return lostFoundList.value[currentIndex.value] || {}
  }
  return {}
})

const route = useRoute()

const refreshUserInfo = async () => {
  try {
    const res = await request.get('/auth/info')
    if (res.data) {
      userStore.setUserInfo(res.data)
    }
  } catch (e) {
    console.error(e)
  }
}

const loadStatistics = async () => {
  const studentId = userStore.userInfo.id
  if (!studentId) return
  
  try {
    const checkInRes = await request.get(`/checkin/status/${studentId}`)
    if (checkInRes.data && checkInRes.data.id) {
      checkInStatus.value = '已打卡'
    } else {
      checkInStatus.value = '未打卡'
    }
  } catch (e) {
    console.error('获取打卡状态失败:', e)
  }
  
  try {
    const repairRes = await request.get('/repair/list', {
      params: { studentId, pageNum: 1, pageSize: 100 }
    })
    repairCount.value = repairRes.data?.total || 0
  } catch (e) {
    console.error('获取报修数量失败:', e)
  }
  
  try {
    const utilityRes = await request.get(`/utility/bill/student/${studentId}`)
    if (utilityRes.data && utilityRes.data.length > 0) {
      const unpaidBills = utilityRes.data.filter(b => b.isPaid === 0)
      const totalUnpaid = unpaidBills.reduce((sum, b) => sum + (b.totalFee || 0), 0)
      utilityBill.value = totalUnpaid
    }
  } catch (e) {
    console.error('获取水电费失败:', e)
  }
}

watch(() => route.fullPath, () => {
  refreshUserInfo()
})

const handleEmergencyHelp = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请描述您的紧急情况', '紧急求助', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入求助内容'
    })
    
    const data = {
      studentId: userStore.userInfo.id,
      roomId: userStore.userInfo.roomId,
      content: value
    }
    await request.post('/emergency/help', data)
    ElMessage.success('求助已发送，请等待宿管处理')
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('发送失败')
    }
  }
}

onMounted(() => {
  refreshUserInfo()
  loadStatistics()
  checkNewMessages()
  loadLostFoundList()
  startCarousel()
})

onUnmounted(() => {
  stopCarousel()
})

const checkNewMessages = async () => {
  try {
    const res = await request.get('/message/list', {
      params: { pageNum: 1, pageSize: 1 }
    })
    const latestMsg = res.data?.records?.[0]
    if (latestMsg && latestMsg.isRead === 0) {
      ElNotification({
        title: latestMsg.title || '新消息',
        message: latestMsg.messageContent || latestMsg.content,
        type: 'warning',
        duration: 10000
      })
      await request.put(`/message/read/${latestMsg.id}`)
    }
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const loadLostFoundList = async () => {
  try {
    const [lostRes, foundRes] = await Promise.all([
      request.get('/lost-found/list', { params: { type: 1, status: 0, pageNum: 1, pageSize: 10 } }),
      request.get('/lost-found/list', { params: { type: 2, status: 0, pageNum: 1, pageSize: 10 } })
    ])
    const allList = [
      ...(lostRes.data?.records || []),
      ...(foundRes.data?.records || [])
    ]
    lostFoundList.value = allList
  } catch (e) {
    console.error('获取失物招领失败:', e)
  }
}

const startCarousel = () => {
  if (carouselTimer) clearInterval(carouselTimer)
  carouselTimer = setInterval(() => {
    if (lostFoundList.value.length > 0) {
      currentIndex.value = (currentIndex.value + 1) % lostFoundList.value.length
    }
  }, 3000)
}

const stopCarousel = () => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
    carouselTimer = null
  }
}
</script>

<style scoped lang="scss">
.student-dashboard {
  padding: 16px;
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .action-row {
    margin-bottom: 8px;
    
    .action-btn {
      width: 100%;
      height: 44px;
      font-size: 14px;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        
        .el-icon {
          transform: scale(1.2);
        }
      }
      
      &:active {
        transform: translateY(0);
      }
      
      .el-icon {
        margin-right: 4px;
        transition: transform 0.3s ease;
      }
    }
  }
  
  .stat-row {
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
        padding: 8px 0;
        
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
            transition: all 0.3s ease;
            
            .unit {
              font-size: 14px;
              font-weight: 400;
              color: #909399;
            }
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
  
  .main-row {
    flex: 1;
    
    .info-card {
      height: 100%;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .dormitory-info {
        .info-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;
          
          .info-item {
            padding: 12px;
            background: #f5f7fa;
            border-radius: 8px;
            display: flex;
            flex-direction: column;
            transition: all 0.3s ease;
            
            &:hover {
              background: #eef1f5;
              transform: translateX(4px);
            }
            
            .label {
              font-size: 12px;
              color: #909399;
              margin-bottom: 4px;
            }
            
            .value {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
            }
          }
        }
      }
    }
    
    .lost-found-card {
      height: 100%;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 16px;
      }
      
      .carousel-wrapper {
        .carousel-item {
          padding: 16px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e8eef3 100%);
          border-radius: 12px;
          animation: fadeIn 0.5s ease;
          
          @keyframes fadeIn {
            from {
              opacity: 0;
              transform: translateY(10px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
          
          .item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .item-time {
              font-size: 13px;
              color: #909399;
            }
          }
          
          .item-body {
            .item-name {
              font-size: 22px;
              font-weight: 700;
              color: #303133;
              margin-bottom: 12px;
            }
            
            .item-details {
              .detail-row {
                display: flex;
                font-size: 14px;
                margin-bottom: 6px;
                
                .label {
                  color: #909399;
                  width: 80px;
                  flex-shrink: 0;
                }
                
                .value {
                  color: #606266;
                  flex: 1;
                }
                
                &.contact-row .value.phone {
                  color: #e6a23c;
                  font-weight: 600;
                }
              }
            }
          }
        }
        
        .carousel-indicators {
          display: flex;
          justify-content: center;
          margin-top: 16px;
          
          .indicator {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #dcdfe6;
            margin: 0 4px;
            cursor: pointer;
            transition: all 0.3s ease;
            
            &:hover {
              background: #909399;
            }
            
            &.active {
              background: #409eff;
              width: 20px;
              border-radius: 4px;
            }
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>

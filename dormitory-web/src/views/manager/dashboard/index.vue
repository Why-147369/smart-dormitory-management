<template>
  <div class="manager-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.studentCount }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="30"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.roomCount }}</div>
            <div class="stat-label">宿舍数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="30"><Tools /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.repairCount }}</div>
            <div class="stat-label">待处理报修</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="30"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.checkInRate.toFixed(1) }}%</div>
            <div class="stat-label">今日打卡率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理事项</span>
          </template>
          <el-table :data="pendingTasks" style="width: 100%">
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="description" label="内容" />
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleTask(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-row :gutter="10">
              <el-col :span="6">
                <div class="action-item" @click="$router.push('/manager/repair')">
                  <el-icon :size="30"><Tools /></el-icon>
                  <span>报修管理</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="action-item" @click="$router.push('/manager/student')">
                  <el-icon :size="30"><User /></el-icon>
                  <span>学生管理</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="action-item" @click="$router.push('/manager/utility')">
                  <el-icon :size="30"><Money /></el-icon>
                  <span>水电管理</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="action-item" @click="$router.push('/manager/health')">
                  <el-icon :size="30"><FirstAidKit /></el-icon>
                  <span>卫生检查</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, House, Tools, Clock, Money, FirstAidKit } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const userStore = useUserStore()

const stats = ref({
  studentCount: 0,
  roomCount: 0,
  repairCount: 0,
  checkInRate: 0
})

const pendingTasks = ref([])

const loadStatistics = async () => {
  const buildingId = userStore.userInfo.buildingId
  if (!buildingId) return
  
  try {
    const studentRes = await request.get('/student/list', { params: { buildingId, pageSize: 1000 } })
    stats.value.studentCount = studentRes.data?.total || 0
  } catch (e) {
    console.error('获取学生数失败:', e)
  }
  
  try {
    const roomRes = await request.get('/room/list', { params: { buildingId, pageSize: 1000 } })
    stats.value.roomCount = roomRes.data?.total || 0
  } catch (e) {
    console.error('获取宿舍数失败:', e)
  }
  
  try {
    const repairRes = await request.get('/repair/list', { params: { buildingId, status: 0, pageSize: 1000 } })
    stats.value.repairCount = repairRes.data?.total || 0
  } catch (e) {
    console.error('获取报修数失败:', e)
  }
  
  try {
    const today = new Date().toISOString().split('T')[0]
    const checkInRes = await request.get('/checkin/list', { params: { buildingId, checkDate: today, pageSize: 1000 } })
    const total = stats.value.studentCount
    const checked = checkInRes.data?.records?.length || 0
    stats.value.checkInRate = total > 0 ? Math.round(checked / total * 10000) / 100 : 0
  } catch (e) {
    console.error('获取打卡率失败:', e)
  }
  
  const tasks = []
  
  try {
    const repairRes = await request.get('/repair/list', { params: { buildingId, status: 0, pageSize: 5 } })
    const repairs = repairRes.data?.records || []
    repairs.forEach(item => {
      tasks.push({
        type: '报修',
        description: `${item.roomNumber || ''} - ${item.title || item.description || '未知'}`,
        time: item.createTime,
        route: '/manager/repair'
      })
    })
  } catch (e) {
    console.error('获取报修失败:', e)
  }
  
  try {
    const visitorRes = await request.get('/visitor/list', { params: { buildingId, status: 0, pageSize: 5 } })
    const visitors = visitorRes.data?.records || []
    visitors.forEach(item => {
      tasks.push({
        type: '访客',
        description: `${item.visitorName} - 访客申请`,
        time: item.createTime,
        route: '/manager/visitor'
      })
    })
  } catch (e) {
    console.error('获取访客失败:', e)
  }
  
  try {
    const roomChangeRes = await request.get('/room/change/list', { params: { buildingId, status: 0, pageSize: 5 } })
    const changes = roomChangeRes.data?.records || []
    changes.forEach(item => {
      tasks.push({
        type: '换寝',
        description: `${item.studentName} - 换寝申请`,
        time: item.createTime,
        route: '/manager/room-change'
      })
    })
  } catch (e) {
    console.error('获取换寝失败:', e)
  }
  
  pendingTasks.value = tasks
}

const handleTask = (row) => {
  console.log('处理任务:', row)
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.manager-dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
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
    
    .stat-content {
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }
      
      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
  
  .quick-actions {
    .action-item {
      text-align: center;
      padding: 20px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s ease;
      
      &:hover {
        background: #f5f7fa;
        transform: translateY(-2px);
        
        .el-icon {
          transform: scale(1.2);
        }
      }
      
      .el-icon {
        color: #409eff;
        margin-bottom: 10px;
        transition: transform 0.3s ease;
      }
      
      span {
        display: block;
        font-size: 14px;
        color: #666;
      }
    }
  }
}
</style>

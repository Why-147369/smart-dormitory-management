<template>
  <div class="dashboard">
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
            <div class="stat-value">{{ stats.buildingCount }}</div>
            <div class="stat-label">楼栋数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
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
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="30"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.checkInRate }}%</div>
            <div class="stat-label">入住率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>入住统计</span>
          </template>
          <div ref="occupancyChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>报修类型分布</span>
          </template>
          <div ref="repairChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { User, House, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const stats = ref({
  studentCount: 0,
  buildingCount: 0,
  roomCount: 0,
  checkInRate: 0
})

const occupancyList = ref([])
const repairTypeList = ref([])
const occupancyChart = ref(null)
const repairChart = ref(null)

const getTypeName = (typeId) => {
  const typeMap = {
    1: '水电',
    2: '门窗',
    3: '网络',
    4: '家具',
    5: '卫浴',
    6: '其他'
  }
  return typeMap[typeId] || '未知'
}

const loadStatistics = async () => {
  try {
    const res = await request.get('/statistics')
    const data = res.data
    
    stats.value = {
      studentCount: data.studentCount || 0,
      buildingCount: data.buildingCount || 0,
      roomCount: data.roomCount || 0,
      checkInRate: data.checkInRate || 0
    }
    
    occupancyList.value = data.occupancyList || []
    repairTypeList.value = data.repairTypeList || []
    
    initCharts()
  } catch (e) {
    console.error(e)
  }
}

const initCharts = () => {
  if (!occupancyChart.value || !repairChart.value) return
  
  const occupancyChartInstance = echarts.init(occupancyChart.value)
  occupancyChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { 
      type: 'category', 
      data: occupancyList.value.map(item => item.buildingName)
    },
    yAxis: { type: 'value', name: '入住人数' },
    series: [{
      data: occupancyList.value.map(item => item.occupiedCount),
      type: 'bar',
      itemStyle: { color: '#409eff' }
    }]
  })
  
  const repairChartInstance = echarts.init(repairChart.value)
  repairChartInstance.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: repairTypeList.value.filter(item => item.value > 0)
    }]
  })
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 16px;
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
}
</style>

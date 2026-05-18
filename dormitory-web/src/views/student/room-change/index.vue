<template>
  <div class="room-change-page">
    <el-card>
      <template #header>
        <span>换寝申请</span>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="申请换寝" name="apply">
          <el-alert
            v-if="!currentRoomInfo"
            title="您当前未分配宿舍，无法申请换寝"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px"
          />
          
          <div v-if="currentRoomInfo" class="current-room-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="当前楼栋">{{ currentRoomInfo.buildingName }}</el-descriptions-item>
              <el-descriptions-item label="当前宿舍">{{ currentRoomInfo.roomNumber }}</el-descriptions-item>
              <el-descriptions-item label="当前床位">{{ currentRoomInfo.bedNumber }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <el-form v-if="currentRoomInfo" ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px; margin-top: 20px">
            <el-form-item label="目标宿舍" prop="targetRoomId">
              <el-select v-model="form.targetRoomId" placeholder="请选择目标宿舍" style="width: 100%" @change="handleRoomChange">
                <el-option
                  v-for="item in availableRooms"
                  :key="item.id"
                  :label="`${item.roomNumber} (剩余${item.bedCount}个床位)`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="目标床位" prop="targetBedId">
              <el-select v-model="form.targetBedId" placeholder="请先选择宿舍" style="width: 100%">
                <el-option
                  v-for="item in availableBeds"
                  :key="item.id"
                  :label="`${item.bedNumber}号床`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="换寝原因" prop="reason">
              <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入换寝原因" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit">提交申请</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="申请记录" name="records">
          <el-table :data="tableData" style="width: 100%" v-loading="loading">
            <el-table-column prop="currentRoomNumber" label="当前宿舍" width="100" />
            <el-table-column prop="currentBedNumber" label="当前床位" width="100" />
            <el-table-column prop="targetRoomNumber" label="目标宿舍" width="100" />
            <el-table-column prop="targetBedNumber" label="目标床位" width="100" />
            <el-table-column prop="reason" label="换寝原因" min-width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rejectReason" label="拒绝原因" width="150" show-overflow-tooltip />
            <el-table-column prop="createTime" label="申请时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('apply')
const loading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const currentRoomInfo = ref(null)
const availableRooms = ref([])
const availableBeds = ref([])

const form = reactive({
  targetRoomId: null,
  targetBedId: null,
  reason: ''
})

const rules = {
  targetRoomId: [{ required: true, message: '请选择目标宿舍', trigger: 'change' }],
  targetBedId: [{ required: true, message: '请选择目标床位', trigger: 'change' }],
  reason: [{ required: true, message: '请输入换寝原因', trigger: 'blur' }]
}

const getCurrentRoomInfo = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/student/${userId}`)
    if (res.data && res.data.roomId) {
      currentRoomInfo.value = {
        roomId: res.data.roomId,
        buildingName: res.data.buildingName || '未知',
        roomNumber: res.data.roomNumber || '未知',
        bedNumber: res.data.bedNumber || '未知'
      }
      userStore.userInfo.buildingId = res.data.buildingId
      getAvailableRooms()
    }
  } catch (e) {
    console.error(e)
  }
}

const getAvailableRooms = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    if (!buildingId) {
      console.error('buildingId is null')
      return
    }
    const res = await request.get(`/room/all/building/${buildingId}`)
    const rooms = res.data.records || res.data || []
    
    console.log('Rooms:', rooms)
    console.log('Current roomId:', currentRoomInfo.value?.roomId)
    
    availableRooms.value = rooms.filter(r => {
      const occupiedBeds = r.currentCount || 0
      const hasEmptyBed = occupiedBeds < r.bedCount
      return r.id !== currentRoomInfo.value?.roomId && hasEmptyBed
    })
    console.log('Available rooms:', availableRooms.value)
  } catch (e) {
    console.error(e)
  }
}

const handleRoomChange = async () => {
  form.targetBedId = null
  availableBeds.value = []
  
  if (!form.targetRoomId) return
  
  try {
    const res = await request.get(`/bed/all`, { params: { roomId: form.targetRoomId } })
    const beds = res.data || []
    availableBeds.value = beds.filter(b => b.status === 0)
  } catch (e) {
    console.error(e)
  }
}

const getMyList = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/room/change/my/${userId}`)
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/room/change/apply', {
          studentId: userStore.userInfo.userId,
          targetRoomId: form.targetRoomId,
          targetBedId: form.targetBedId,
          changeType: 1,
          reason: form.reason
        })
        ElMessage.success('申请提交成功')
        handleReset()
        activeTab.value = 'records'
        getMyList()
      } catch (e) {
        console.error(e)
        ElMessage.error(e.message || '提交失败')
      }
    }
  })
}

const handleReset = () => {
  form.targetRoomId = null
  form.targetBedId = null
  form.reason = ''
  availableBeds.value = []
  formRef.value?.resetFields()
}

onMounted(() => {
  getCurrentRoomInfo()
  getMyList()
})
</script>

<style scoped>
.room-change-page {
  padding: 20px;
}
.current-room-info {
  margin-bottom: 20px;
}
</style>

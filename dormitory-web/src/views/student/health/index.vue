<template>
  <div class="health-page">
    <el-card>
      <template #header>
        <span>卫生检查记录</span>
      </template>
      
      <div class="current-room" v-if="roomInfo">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="楼栋">{{ roomInfo.buildingName }}</el-descriptions-item>
          <el-descriptions-item label="宿舍号">{{ roomInfo.roomNumber }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-loading="loading" style="margin-top: 20px">
        <el-empty v-if="tableData.length === 0" description="暂无卫生检查记录" />
        <el-table v-else :data="tableData" style="width: 100%">
          <el-table-column prop="checkDate" label="检查日期" width="120" />
          <el-table-column prop="score" label="得分" width="100">
            <template #default="{ row }">
              <el-tag :type="row.score >= 8 ? 'success' : row.score >= 6 ? 'warning' : 'danger'">
                {{ row.score }}分
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="检查备注" min-width="200" show-overflow-tooltip />
          <el-table-column label="照片" width="100">
            <template #default="{ row }">
              <el-button v-if="row.images" type="primary" size="small" @click="showImages(row.images)">查看</el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="managerName" label="检查人" width="100" />
          <el-table-column prop="createTime" label="检查时间" width="180" />
        </el-table>
      </div>
    </el-card>
    
    <el-dialog v-model="imageDialogVisible" title="现场照片" width="600px">
      <div class="image-preview">
        <el-image 
          v-for="(img, index) in previewImages" 
          :key="index"
          :src="img" 
          :preview-src-list="previewImages"
          fit="cover"
          style="width: 150px; height: 150px; margin: 5px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)
const userStore = useUserStore()
const roomInfo = ref(null)
const imageDialogVisible = ref(false)
const previewImages = ref([])

const showImages = (images) => {
  previewImages.value = images.split(',')
  imageDialogVisible.value = true
}

const getList = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get('/health/list', {
      params: { studentId: userId, pageNum: 1, pageSize: 100 }
    })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records || []
    } else if (Array.isArray(data)) {
      tableData.value = data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getRoomInfo = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/student/${userId}`)
    if (res.data) {
      roomInfo.value = {
        buildingName: res.data.buildingName || '未知',
        roomNumber: res.data.roomNumber || '未知'
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  getRoomInfo()
  getList()
})
</script>

<style scoped lang="scss">
.health-page {
  padding: 20px;
  
  .current-room {
    margin-bottom: 20px;
  }
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

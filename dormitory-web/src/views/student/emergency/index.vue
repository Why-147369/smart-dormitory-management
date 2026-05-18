<template>
  <div class="student-emergency">
    <el-card>
      <template #header>
        <span>我的求助记录</span>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="buildingName" label="楼栋" width="120" />
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
        <el-table-column prop="handleRemark" label="处理备注" min-width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const tableData = ref([])
const userStore = useUserStore()

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
    const studentId = userStore.userInfo.id
    const res = await request.get(`/emergency/my/${studentId}`)
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.student-emergency {
  padding: 20px;
}
</style>

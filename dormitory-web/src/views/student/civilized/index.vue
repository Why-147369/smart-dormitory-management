<template>
  <div class="civilized-page">
    <el-card>
      <template #header>
        <span>文明宿舍排行榜</span>
      </template>
      
      <div v-loading="loading">
        <el-empty v-if="tableData.length === 0" description="暂无排名数据" />
        <el-table v-else :data="tableData" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80">
            <template #default="{ $index }">
              <el-tag :type="$index < 3 ? 'danger' : ''">{{ $index + 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="buildingName" label="楼栋" width="120" />
          <el-table-column prop="roomNumber" label="宿舍号" width="120" />
          <el-table-column prop="totalScore" label="得分" width="100">
            <template #default="{ row }">
              {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="month" label="月份" width="100" />
        </el-table>
      </div>
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

const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1

const getList = async () => {
  loading.value = true
  try {
    const params = {
      year: currentYear,
      month: currentMonth
    }
    if (userStore.userInfo?.buildingId) {
      params.buildingId = userStore.userInfo.buildingId
    }
    const res = await request.get('/civilized/top10', { params })
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

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
</style>

<template>
  <div class="rules-page">
    <el-card>
      <template #header>
        <span>宿舍公约</span>
      </template>
      
      <div v-loading="loading">
        <el-empty v-if="tableData.length === 0" description="暂无公约信息" />
        <div v-else>
          <el-card v-for="item in tableData" :key="item.id" style="margin-bottom: 15px;">
            <h4>{{ item.title }}</h4>
            <p style="color: #666; line-height: 1.8; white-space: pre-wrap;">{{ item.content }}</p>
            <p style="color: #999; font-size: 12px; margin-top: 10px;">发布时间：{{ item.createTime }}</p>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/rule/list')
    tableData.value = res.data || []
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
.rules-page {
  h4 {
    margin-bottom: 10px;
    color: #333;
  }
}
</style>

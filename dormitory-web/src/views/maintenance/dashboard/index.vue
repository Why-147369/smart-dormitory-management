<template>
  <div>
    <h3 style="margin-bottom:20px">工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:36px;color:#409EFF">{{ stats.pending }}</div><div style="color:#999;margin-top:8px">待接单</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:36px;color:#E6A23C">{{ stats.repairing }}</div><div style="color:#999;margin-top:8px">维修中</div></div></el-card></el-col>
      <el-col :span="8"><el-card><div style="text-align:center"><div style="font-size:36px;color:#67C23A">{{ stats.completed }}</div><div style="color:#999;margin-top:8px">已完成</div></div></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
const stats = ref({ pending: 0, repairing: 0, completed: 0 })
onMounted(async () => {
  const uid = userStore.userInfo?.userId
  if (!uid) return
  const [p, r, c] = await Promise.all([
    request.get('/maintenance/repairs', { params: { maintenanceId: uid, status: 0, pageSize: 1 } }),
    request.get('/maintenance/repairs', { params: { maintenanceId: uid, status: 2, pageSize: 1 } }),
    request.get('/maintenance/repairs', { params: { maintenanceId: uid, status: 3, pageSize: 1 } })
  ])
  stats.value.pending = p.data?.total || 0
  stats.value.repairing = r.data?.total || 0
  stats.value.completed = c.data?.total || 0
})
</script>

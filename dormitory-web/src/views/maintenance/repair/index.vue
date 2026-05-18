<template>
  <div>
    <h3 style="margin-bottom:16px">我的工单</h3>
    <el-radio-group v-model="filterStatus" @change="getList" style="margin-bottom:16px">
      <el-radio-button :value="undefined">全部</el-radio-button>
      <el-radio-button :value="0">待接单</el-radio-button>
      <el-radio-button :value="2">维修中</el-radio-button>
      <el-radio-button :value="3">已完成</el-radio-button>
    </el-radio-group>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="repairNumber" label="报修单号" width="160" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="typeName" label="类型" width="90" />
      <el-table-column prop="studentName" label="报修学生" width="100" />
      <el-table-column prop="roomNumber" label="宿舍" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{row}"><el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button v-if="row.status===0" type="primary" size="small" @click="handleAccept(row)">接单</el-button>
          <el-button v-if="row.status===2" type="success" size="small" @click="handleComplete(row)">完成</el-button>
          <el-button v-if="row.status===0||row.status===2" text size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="total,prev,next" @current-change="getList" style="margin-top:16px;justify-content:flex-end"/>
    <el-dialog v-model="detailVisible" title="工单详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报修单号">{{ detail.repairNumber }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detail.description }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.typeName }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ detail.studentName }}（{{ detail.studentNumber }}）</el-descriptions-item>
        <el-descriptions-item label="宿舍">{{ detail.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="是否紧急"><el-tag v-if="detail.isEmergency===1" type="danger">紧急</el-tag><span v-else>普通</span></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    <el-dialog v-model="remarkVisible" title="完成维修" width="400px" @close="remark=''">
      <el-input v-model="remark" placeholder="处理备注（选填）" type="textarea" :rows="3"/>
      <template #footer><el-button @click="remarkVisible=false">取消</el-button><el-button type="primary" @click="submitComplete">确认完成</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const filterStatus = ref(undefined)
const detailVisible = ref(false)
const detail = ref({})
const remarkVisible = ref(false)
const remark = ref('')
const currentRow = ref(null)
const statusMap = { 0: { text: '待接单', type: 'warning' }, 1: { text: '已接单', type: '' }, 2: { text: '维修中', type: '' }, 3: { text: '已完成', type: 'success' } }

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/maintenance/repairs', { params: { maintenanceId: userStore.userInfo.userId, pageNum: pageNum.value, status: filterStatus.value } })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch(e) { console.error(e) }
  loading.value = false
}
const handleAccept = async (row) => {
  await request.put(`/maintenance/repair/accept/${row.id}?maintenanceId=${userStore.userInfo.userId}`)
  ElMessage.success('已接单')
  getList()
}
const handleComplete = (row) => { currentRow.value = row; remarkVisible.value = true }
const submitComplete = async () => {
  await request.put(`/maintenance/repair/complete/${currentRow.value.id}?remark=${encodeURIComponent(remark.value||'')}`)
  ElMessage.success('维修完成')
  remarkVisible.value = false
  getList()
}
const showDetail = (row) => { detail.value = row; detailVisible.value = true }
onMounted(getList)
</script>

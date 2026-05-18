<template>
  <div class="manager-repair">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修管理</span>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="repairNumber" label="报修单号" width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="repairTypeName" label="报修类型" width="100" />
        <el-table-column prop="description" label="故障描述" :show-overflow-tooltip="true" />
        <el-table-column prop="isEmergency" label="紧急" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isEmergency === 1" type="danger" size="small">紧急</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="info">已接单</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">维修中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="报修详情" width="550px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报修单号">{{ detailData.repairNumber }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="宿舍号">{{ detailData.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="报修类型">{{ detailData.repairTypeName }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="是否紧急">
          <el-tag v-if="detailData.isEmergency === 1" type="danger" size="small">紧急</el-tag>
          <span v-else>普通</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="warning">待处理</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="info">已接单</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="primary">维修中</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="success">已完成</el-tag>
          <el-tag v-else-if="detailData.status === 4" type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.repairPerson" label="维修人员">{{ detailData.repairPerson }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.handleRemark" label="处理备注">{{ detailData.handleRemark }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.images && detailData.images.length > 0" label="现场照片">
          <div class="image-list">
            <el-image
              v-for="(img, index) in detailData.images"
              :key="index"
              :src="img"
              :preview-src-list="detailData.images"
              :initial-index="index"
              style="width: 80px; height: 80px; margin-right: 10px; border-radius: 4px;"
              fit="cover"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="commentData" label="学生评价">
          <div>
            <el-rate v-model="commentData.rating" disabled :colors="['#99A9BF', '#F7BA2A', '#67C23A']" />
            <p v-if="commentData.content">{{ commentData.content }}</p>
            <span style="color: #999; font-size: 12px;">评价时间：{{ commentData.createTime }}</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
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
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const completeVisible = ref(false)
const completeFormRef = ref(null)
const detailData = ref({})
const commentData = ref(null)
const maintenancePersons = ref([])

const completeForm = ref({
  id: null,
  repairPerson: '',
  handleRemark: ''
})

const completeRules = {
  repairPerson: [{ required: true, message: '请输入维修人员姓名', trigger: 'blur' }]
}

const getList = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    const res = await request.get('/repair/list', { 
      params: { 
        buildingId,
        pageNum: pageNum.value,
        pageSize: pageSize.value
      } 
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  }
}

const handleView = async (row) => {
  detailData.value = { ...row }
  if (!detailData.value.images) {
    detailData.value.images = []
  } else if (typeof detailData.value.images === 'string') {
    try {
      detailData.value.images = JSON.parse(detailData.value.images)
    } catch (e) {
      detailData.value.images = []
    }
  }
  detailVisible.value = true

  commentData.value = null
  try {
    const res = await request.get(`/repair/comment/repair/${row.id}`)
    if (res.data && res.data.length > 0) {
      commentData.value = res.data[0]
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAccept = async (row) => {
  try {
    await request.put(`/repair/accept/${row.id}`)
    ElMessage.success('已接单')
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleStart = async (row) => {
  try {
    await request.put(`/repair/start/${row.id}`)
    ElMessage.success('已开始维修')
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleComplete = (row) => {
  completeForm.value = { id: row.id, typeId: row.typeId, repairPerson: '', handleRemark: '' }
  getMaintenancePersonsByType(row.typeId)
  completeVisible.value = true
}

const getMaintenancePersonsByType = async (typeId) => {
  try {
    if (!typeId) {
      const res = await request.get('/maintenance/all')
      maintenancePersons.value = res.data || []
      return
    }
    const typeRes = await request.get(`/repair/type/${typeId}`)
    const typeName = typeRes.data?.typeName || ''
    
    const res = await request.get('/maintenance/by-type', {
      params: { specialty: typeName }
    })
    maintenancePersons.value = res.data || []
  } catch (e) {
    console.error(e)
    maintenancePersons.value = []
  }
}

const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  await completeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.put(`/repair/complete/${completeForm.value.id}`, null, {
          params: {
            remark: completeForm.value.handleRemark,
            repairPerson: completeForm.value.repairPerson
          }
        })
        ElMessage.success('已完成')
        completeVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  getList()
  getMaintenancePersons()
})

const getMaintenancePersons = async () => {
  try {
    const res = await request.get('/maintenance/all')
    maintenancePersons.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped lang="scss">
.manager-repair {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .pagination {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>

<template>
  <div class="student-repair">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ pendingCount }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);">
              <el-icon :size="28"><Loading /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ processingCount }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ completedCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);">
              <el-icon :size="28"><Edit /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ totalCount }}</div>
              <div class="stat-label">全部报修</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的报修</span>
          <el-button type="primary" @click="handleAdd">提交报修</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="searchForm.status" placeholder="状态筛选" clearable style="width: 140px;">
          <el-option label="待处理" :value="0" />
          <el-option label="已接单" :value="1" />
          <el-option label="维修中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          clearable
          style="width: 240px;"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%; margin-top: 15px;">
        <el-table-column prop="repairNumber" label="报修单号" width="150" />
        <el-table-column prop="repairTypeName" label="报修类型" width="100" />
        <el-table-column prop="description" label="故障描述" :show-overflow-tooltip="true" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="info">已接单</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">维修中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 0" 
              link 
              type="danger" 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="row.status === 3" 
              link 
              type="success" 
              @click="handleComment(row)"
            >
              评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交报修弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交报修" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="报修类型" prop="repairTypeId">
          <el-select v-model="form.repairTypeId" placeholder="请选择报修类型" style="width: 100%;">
            <el-option
              v-for="item in repairTypes"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请描述故障情况"
          />
        </el-form-item>
        <el-form-item label="是否紧急">
          <el-switch v-model="form.isEmergency" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px; color: #999;">紧急报修将优先处理</span>
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 5px;">最多上传3张照片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="报修详情" width="550px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报修单号">{{ detailData.repairNumber }}</el-descriptions-item>
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
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="commentVisible" title="评价" width="450px">
      <el-form ref="commentFormRef" :model="commentForm" :rules="commentRules" label-width="80px">
        <el-form-item label="满意度" prop="rating">
          <el-rate v-model="commentForm.rating" :colors="['#99A9BF', '#F7BA2A', '#67C23A']" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入评价内容（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComment">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible">
      <img w-full :src="previewUrl" style="width: 100%;" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, Loading, CircleCheck, Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const tableData = ref([])
const pendingCount = ref(0)
const processingCount = ref(0)
const completedCount = ref(0)
const totalCount = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const commentVisible = ref(false)
const formRef = ref(null)
const commentFormRef = ref(null)
const repairTypes = ref([])
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const detailData = ref({})

const searchForm = reactive({
  status: '',
  dateRange: []
})

const form = ref({
  studentId: null,
  title: '',
  repairTypeId: '',
  description: '',
  isEmergency: 0
})

const commentForm = ref({
  repairId: null,
  rating: 3,
  content: ''
})

const rules = {
  repairTypeId: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  description: [{ required: true, message: '请描述故障情况', trigger: 'blur' }]
}

const commentRules = {
  rating: [{ required: true, message: '请选择满意度', trigger: 'change' }]
}

const getList = async () => {
  try {
    const res = await request.get('/repair/my/' + userStore.userInfo.userId)
    let data = res.data || []
    data.forEach(item => {
      if (item.images) {
        try {
          item.images = JSON.parse(item.images)
        } catch {
          item.images = []
        }
      }
    })

    if (searchForm.status !== '' && searchForm.status !== null) {
      data = data.filter(item => item.status === searchForm.status)
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const [startDate, endDate] = searchForm.dateRange
      data = data.filter(item => {
        const createTime = item.createTime?.split('T')[0]
        return createTime >= startDate && createTime <= endDate
      })
    }

    tableData.value = data
    
    totalCount.value = data.length
    pendingCount.value = data.filter(item => item.status === 0).length
    processingCount.value = data.filter(item => item.status === 1 || item.status === 2).length
    completedCount.value = data.filter(item => item.status === 3).length
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  getList()
}

const handleReset = () => {
  searchForm.status = ''
  searchForm.dateRange = []
  getList()
}

const getRepairTypes = async () => {
  try {
    const res = await request.get('/repair/type/list')
    repairTypes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file.raw)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

const handleAdd = () => {
  form.value = { repairTypeId: '', description: '', isEmergency: 0 }
  fileList.value = []
  dialogVisible.value = true
}

const handleRemove = (file, fileList) => {
  fileList.value = fileList
}

const handlePictureCardPreview = async (file) => {
  if (file.url) {
    previewUrl.value = file.url
  } else if (file.raw) {
    previewUrl.value = await fileToBase64(file)
  }
  previewVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        form.value.studentId = userStore.userInfo.userId
        form.value.typeId = form.value.repairTypeId
        form.value.title = repairTypes.value.find(t => t.id === form.value.repairTypeId)?.typeName || '报修'

        if (fileList.value.length > 0) {
          const base64Images = await Promise.all(fileList.value.map(f => fileToBase64(f)))
          form.value.images = JSON.stringify(base64Images)
        }

        await request.post('/repair/submit', form.value)
        ElMessage.success('提交成功')
        dialogVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleView = (row) => {
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
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该报修吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/repair/cancel/${row.id}`)
      ElMessage.success('已取消')
      getList()
    } catch (e) {
      console.error(e)
    }
  })
}

const handleComment = (row) => {
  commentForm.value = { repairId: row.id, rating: 3, content: '' }
  commentVisible.value = true
}

const handleSubmitComment = async () => {
  if (!commentFormRef.value) return

  await commentFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/repair/comment', {
          repairId: commentForm.value.repairId,
          studentId: userStore.userInfo.userId,
          rating: commentForm.value.rating,
          content: commentForm.value.content
        })
        ElMessage.success('评价成功')
        commentVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  getList()
  getRepairTypes()
})
</script>

<style scoped lang="scss">
.student-repair {
  padding: 16px;
  
  .stat-row {
    margin-bottom: 16px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          margin-right: 16px;
        }
        
        .stat-text {
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #333;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 2px;
          }
        }
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-bar {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

:deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
  line-height: 80px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
}
</style>

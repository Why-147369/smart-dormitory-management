<template>
  <div class="manager-health">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>卫生检查</span>
          <div>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
              批量删除{{ selectedRows.length > 0 ? ` (${selectedRows.length})` : '' }}
            </el-button>
            <el-button type="success" @click="showImportDialog">批量导入</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="录入检查" name="check">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
            <el-form-item label="宿舍" prop="roomId">
              <el-select v-model="form.roomId" placeholder="请选择宿舍" style="width: 100%">
                <el-option v-for="item in roomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="得分" prop="score">
              <div class="score-slider">
                <el-slider v-model="form.score" :min="1" :max="10" show-stops />
                <span class="score-value">{{ form.score }}分</span>
              </div>
            </el-form-item>
            <el-form-item label="现场照片">
              <el-upload
                v-model:file-list="fileList"
                action="/api/upload"
                list-type="picture-card"
                :on-success="handleUploadSuccess"
                :on-remove="handleRemove"
                :on-error="handleUploadError"
                :limit="3"
                :headers="uploadHeaders"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">最多上传3张照片</div>
            </el-form-item>
            <el-form-item label="检查备注">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入检查备注" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit">提交</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="历史记录" name="history">
          <template #label>
            <span style="display: flex; align-items: center;">
              历史记录
              <el-badge v-if="selectedRows.length > 0" :value="selectedRows.length" type="danger" style="margin-left: 5px;" />
            </span>
          </template>
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="宿舍">
              <el-select v-model="searchForm.roomId" placeholder="全部" clearable style="width: 120px">
                <el-option v-for="item in roomList" :key="item.id" :label="item.roomNumber" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleResetSearch">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="tableData" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="roomNumber" label="宿舍号" width="100" />
            <el-table-column prop="score" label="得分" width="80">
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
            <el-table-column prop="checkDate" label="检查日期" width="120" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
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
    
    <el-dialog v-model="importDialogVisible" title="批量导入卫生检查" width="600px">
      <el-alert
        title="Excel模板说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div>Excel第一行必须包含以下表头：</div>
          <div style="margin-top: 5px;">宿舍号 | 得分 | 检查备注</div>
          <div style="margin-top: 5px;">得分填写1-10的数字，检查备注选填</div>
        </template>
      </el-alert>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :file-list="importFileList"
        drag
        action="#"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将Excel文件拖到此处，或<em>点击上传</em>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImportSubmit">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('check')
const loading = ref(false)
const formRef = ref(null)
const roomList = ref([])
const tableData = ref([])
const selectedRows = ref([])
const fileList = ref([])
const imageUrls = ref([])

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (sessionStorage.getItem('manager_token') || '')
}))

const handleUploadSuccess = (response, uploadFile) => {
  if (response && response.data) {
    uploadFile.url = response.data
    imageUrls.value.push(response.data)
  }
}

const handleRemove = (uploadFile, fileList) => {
  const url = uploadFile.url
  const index = imageUrls.value.indexOf(url)
  if (index > -1) {
    imageUrls.value.splice(index, 1)
  }
}

const form = reactive({
  roomId: null,
  score: 8,
  description: '',
  images: ''
})

const rules = {
  roomId: [{ required: true, message: '请选择宿舍', trigger: 'change' }],
  score: [{ required: true, message: '请选择得分', trigger: 'change' }]
}

const searchForm = reactive({
  roomId: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getRoomList = async () => {
  try {
    const buildingId = userStore.userInfo.buildingId
    const res = await request.get(`/room/all/building/${buildingId}`)
    roomList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  loading.value = true
  try {
    let params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      buildingId: userStore.userInfo.buildingId
    }
    if (searchForm.roomId) {
      params.roomId = searchForm.roomId
    }
    
    const res = await request.get('/health/list', { params })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      pagination.total = data.total || 0
    } else if (Array.isArray(data)) {
      tableData.value = data
      pagination.total = data.length
    } else {
      tableData.value = []
      pagination.total = 0
    }
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
        form.images = imageUrls.value.join(',')
        await request.post('/health/check', {
          roomId: form.roomId,
          managerId: userStore.userInfo.userId,
          score: form.score,
          description: form.description,
          images: form.images
        })
        ElMessage.success('提交成功')
        handleReset()
        activeTab.value = 'history'
        getList()
      } catch (e) {
        console.error(e)
        ElMessage.error(e.message || '提交失败')
      }
    }
  })
}

const handleReset = () => {
  form.roomId = null
  form.score = 8
  form.description = ''
  form.images = ''
  fileList.value = []
  imageUrls.value = []
  formRef.value?.resetFields()
}

const imageDialogVisible = ref(false)
const previewImages = ref([])

const importDialogVisible = ref(false)
const importFileList = ref([])
const importing = ref(false)
const uploadRef = ref(null)

const showImportDialog = () => {
  importFileList.value = []
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFileList.value = [file]
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleImportSubmit = async () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  
  const file = importFileList.value[0].raw
  if (!file) {
    ElMessage.warning('请先选择Excel文件')
    return
  }
  
  importing.value = true
  try {
    const data = await file.arrayBuffer()
    const workbook = XLSX.read(data)
    const sheet = workbook.Sheets[workbook.SheetNames[0]]
    const jsonData = XLSX.utils.sheet_to_json(sheet)
    
    if (jsonData.length === 0) {
      ElMessage.warning('Excel文件为空')
      return
    }
    
    const checks = []
    for (const row of jsonData) {
      const roomNumber = row['宿舍号'] || row['roomNumber']
      const score = row['得分'] || row['score']
      const description = row['检查备注'] || row['description'] || ''
      
      if (!roomNumber) {
        ElMessage.warning('宿舍号不能为空')
        continue
      }
      
      const room = roomList.value.find(r => r.roomNumber === String(roomNumber))
      if (!room) {
        ElMessage.warning(`宿舍号[${roomNumber}]不存在，请先在宿舍管理中添加`)
        continue
      }
      
      if (!score) {
        ElMessage.warning(`宿舍号[${roomNumber}]的得分不能为空`)
        continue
      }
      
      checks.push({
        roomId: room.id,
        score: parseInt(score),
        description: description
      })
    }
    
    if (checks.length === 0) {
      ElMessage.warning('没有有效的数据可导入')
      return
    }
    
    const res = await request.post(`/health/batch?managerId=${userStore.userInfo.userId}`, checks)
    const result = res.data
    ElMessage.success(`导入成功${result.success}条，失败${result.failed}条`)
    importDialogVisible.value = false
    activeTab.value = 'history'
    getList()
  } catch (e) {
    console.error(e)
    ElMessage.error('导入失败: ' + (e.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

const showImages = (images) => {
  if (!images) return
  let imageList = images.split(',').filter(img => img)
  imageList = imageList.map(img => {
    if (img.startsWith('/api/uploads/')) {
      return 'http://localhost:8080' + img
    }
    return img
  })
  previewImages.value = imageList
  imageDialogVisible.value = true
}

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleResetSearch = () => {
  searchForm.roomId = null
  pagination.pageNum = 1
  getList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getList()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该检查记录吗？', '提示', { type: 'warning' })
    await request.delete(`/health/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条检查记录吗？`, '提示', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/health/batch-delete', ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  getRoomList()
  getList()
})
</script>

<style scoped>
.manager-health {
  padding: 20px;
}
.score-slider {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 20px;
}
.score-slider .el-slider {
  flex: 1;
}
.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.search-form {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  min-width: 50px;
}
.search-form {
  margin-bottom: 15px;
}
</style>

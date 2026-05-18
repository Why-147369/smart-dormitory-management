<template>
  <div class="student-lost-found">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="失物" name="lost">
          <div class="search-form">
            <el-form :inline="true" :model="lostSearchForm">
              <el-form-item label="物品类型">
                <el-select v-model="lostSearchForm.itemType" placeholder="请选择" clearable>
                  <el-option v-for="t in itemTypes" :key="t" :label="t" :value="t" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="lostSearchForm.status" placeholder="请选择" clearable>
                  <el-option label="待认领" :value="0" />
                  <el-option label="已找到" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadLostList">搜索</el-button>
                <el-button @click="handleResetLost">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="action-bar">
            <el-button type="primary" @click="handlePublish(1)">发布失物</el-button>
          </div>
          <el-table :data="lostList" style="width: 100%" v-loading="loading">
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <el-image 
                  v-if="getImages(row.images).length > 0" 
                  :src="getImages(row.images)[0]" 
                  style="width: 50px; height: 50px" 
                  fit="cover" 
                  :preview-src-list="getImages(row.images)"
                  :z-index="2000"
                  preview-teleported
                >
                  <template #error>
                    <div style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; color: #999; font-size: 12px;">无图</div>
                  </template>
                </el-image>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemName" label="物品名称" width="120" />
            <el-table-column prop="itemType" label="物品类型" width="100" />
            <el-table-column prop="lostTime" label="丢失时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.lostTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="lostPlace" label="丢失地点" width="120" />
            <el-table-column prop="description" label="物品描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="contact" label="联系方式" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ row.status === 0 ? '待认领' : '已找到' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0 && row.publisherId === userStore.userInfo.id" link type="success" @click="handleClaim(row)">确认找到</el-button>
                <span v-else style="color: #999; font-size: 12px;">-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="lostPagination.pageNum"
            v-model:page-size="lostPagination.pageSize"
            :total="lostPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadLostList"
            @current-change="loadLostList"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="拾物" name="found">
          <div class="search-form">
            <el-form :inline="true" :model="foundSearchForm">
              <el-form-item label="物品类型">
                <el-select v-model="foundSearchForm.itemType" placeholder="请选择" clearable>
                  <el-option v-for="t in itemTypes" :key="t" :label="t" :value="t" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="foundSearchForm.status" placeholder="请选择" clearable>
                  <el-option label="待认领" :value="0" />
                  <el-option label="已找到" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadFoundList">搜索</el-button>
                <el-button @click="handleResetFound">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="action-bar">
            <el-button type="primary" @click="handlePublish(2)">发布拾物</el-button>
          </div>
          <el-table :data="foundList" style="width: 100%" v-loading="loading">
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <el-image 
                  v-if="getImages(row.images).length > 0" 
                  :src="getImages(row.images)[0]" 
                  style="width: 50px; height: 50px" 
                  fit="cover" 
                  :preview-src-list="getImages(row.images)"
                  :z-index="2000"
                  preview-teleported
                >
                  <template #error>
                    <div style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; color: #999; font-size: 12px;">无图</div>
                  </template>
                </el-image>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemName" label="物品名称" width="120" />
            <el-table-column prop="itemType" label="物品类型" width="100" />
            <el-table-column prop="lostTime" label="拾取时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.lostTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="lostPlace" label="拾取地点" width="120" />
            <el-table-column prop="description" label="物品描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="contact" label="联系方式" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ row.status === 0 ? '待认领' : '已找到' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0 && row.publisherId === userStore.userInfo.id" link type="success" @click="handleClaim(row)">确认领走</el-button>
                <span v-else style="color: #999; font-size: 12px;">-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="foundPagination.pageNum"
            v-model:page-size="foundPagination.pageSize"
            :total="foundPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadFoundList"
            @current-change="loadFoundList"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="publishType === 1 ? '发布失物' : '发布拾物'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="物品名称" required>
          <el-input v-model="form.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="物品类型" required>
          <el-select v-model="form.itemType" placeholder="请选择物品类型" style="width: 100%">
            <el-option v-for="t in itemTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item :label="publishType === 1 ? '丢失时间' : '拾取时间'" required>
          <el-date-picker v-model="form.lostTime" type="datetime" placeholder="请选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="publishType === 1 ? '丢失地点' : '拾取地点'">
          <el-input v-model="form.lostPlace" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="物品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述物品特征" />
        </el-form-item>
        <el-form-item label="联系方式" required>
          <el-input v-model="form.contact" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="物品图片">
          <el-upload
            v-model:file-list="fileList"
            action="/api/upload"
            list-type="picture-card"
            :auto-upload="true"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
            :on-preview="handlePreview"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 5px;">最多上传3张照片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="imageDialogVisible" title="图片预览" width="500px" :z-index="3000" destroy-on-close>
      <el-image :src="previewImage" style="width: 100%; max-height: 400px;" fit="contain" />
    </el-dialog>
    
    <el-image-viewer
      v-if="showViewer"
      :url-list="viewerImages"
      :z-index="3000"
      @close="showViewer = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const activeTab = ref('lost')
const loading = ref(false)
const lostList = ref([])
const foundList = ref([])
const dialogVisible = ref(false)
const publishType = ref(1)
const userStore = useUserStore()
const itemTypes = ref(['证件', '电子产品', '衣物', '书籍', '钱包', '钥匙', '其他'])

const lostSearchForm = reactive({ itemType: '', status: '' })
const foundSearchForm = reactive({ itemType: '', status: '' })

const lostPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const foundPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const form = reactive({
  itemName: '',
  itemType: '',
  lostTime: '',
  lostPlace: '',
  description: '',
  contact: ''
})

const fileList = ref([])

const getAuthToken = () => {
  const userType = sessionStorage.getItem('userType') || ''
  const tokenKey = {
    1: 'student_token',
    2: 'manager_token',
    3: 'admin_token'
  }[Number(userType)] || 'token'
  return sessionStorage.getItem(tokenKey) || ''
}

const uploadHeaders = ref({
  Authorization: ''
})

onMounted(() => {
  uploadHeaders.value.Authorization = 'Bearer ' + getAuthToken()
})

const imageDialogVisible = ref(false)
const previewImage = ref('')
const showViewer = ref(false)
const viewerImages = ref([])

const handleUploadChange = (file, files) => {
  fileList.value = files
}

const handleUploadRemove = (file, files) => {
  fileList.value = files
}

const handleUploadSuccess = (response, file) => {
  console.log('上传成功', response)
  if (response.code === 200 && response.data) {
    file.url = response.data
  }
}

const handleUploadError = (error, file) => {
  console.error('上传失败', error)
  ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
}

const handlePreview = (file) => {
  previewImage.value = file.url
  imageDialogVisible.value = true
}

const getImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    const arr = JSON.parse(imagesStr)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getList = async () => {
  loading.value = true
  try {
    const lostParams = { 
      type: 1,
      pageNum: lostPagination.pageNum,
      pageSize: lostPagination.pageSize
    }
    if (lostSearchForm.itemType) {
      lostParams.itemType = lostSearchForm.itemType
    }
    if (lostSearchForm.status !== null && lostSearchForm.status !== '') {
      lostParams.status = lostSearchForm.status
    }
    
    const foundParams = { 
      type: 2,
      pageNum: foundPagination.pageNum,
      pageSize: foundPagination.pageSize
    }
    if (foundSearchForm.itemType) {
      foundParams.itemType = foundSearchForm.itemType
    }
    if (foundSearchForm.status !== null && foundSearchForm.status !== '') {
      foundParams.status = foundSearchForm.status
    }
    
    const [lostRes, foundRes] = await Promise.all([
      request.get('/lost-found/list', { params: lostParams }),
      request.get('/lost-found/list', { params: foundParams })
    ])
    lostList.value = lostRes.data?.records || []
    lostPagination.total = lostRes.data?.total || 0
    foundList.value = foundRes.data?.records || []
    foundPagination.total = foundRes.data?.total || 0
  } catch (e) {
    console.error(e)
    lostList.value = []
    foundList.value = []
  } finally {
    loading.value = false
  }
}

const loadLostList = async () => {
  loading.value = true
  try {
    const params = { 
      type: 1,
      pageNum: lostPagination.pageNum,
      pageSize: lostPagination.pageSize
    }
    if (lostSearchForm.itemType) {
      params.itemType = lostSearchForm.itemType
    }
    if (lostSearchForm.status !== null && lostSearchForm.status !== '') {
      params.status = lostSearchForm.status
    }
    
    const res = await request.get('/lost-found/list', { params })
    lostList.value = res.data?.records || []
    lostPagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadFoundList = async () => {
  loading.value = true
  try {
    const params = { 
      type: 2,
      pageNum: foundPagination.pageNum,
      pageSize: foundPagination.pageSize
    }
    if (foundSearchForm.itemType) {
      params.itemType = foundSearchForm.itemType
    }
    if (foundSearchForm.status !== null && foundSearchForm.status !== '') {
      params.status = foundSearchForm.status
    }
    
    const res = await request.get('/lost-found/list', { params })
    foundList.value = res.data?.records || []
    foundPagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handlePublish = (type) => {
  publishType.value = type
  form.itemName = ''
  form.itemType = ''
  form.lostTime = ''
  form.lostPlace = ''
  form.description = ''
  form.contact = userStore.userInfo?.phone || ''
  fileList.value = []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.itemName || !form.itemType || !form.lostTime || !form.contact) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  try {
    const data = {
      type: publishType.value,
      itemName: form.itemName,
      itemType: form.itemType,
      lostTime: form.lostTime,
      lostPlace: form.lostPlace,
      description: form.description,
      contact: form.contact,
      publisherId: userStore.userInfo.id,
      images: fileList.value.length > 0 ? JSON.stringify(fileList.value.map(f => f.url)) : ''
    }
    await request.post('/lost-found', data)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    fileList.value = []
    if (activeTab.value === 'lost') {
      loadLostList()
    } else {
      loadFoundList()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('发布失败')
  }
}

const handleClaim = async (row) => {
  try {
    await request.put(`/lost-found/claim/${row.id}`)
    ElMessage.success('状态已更新')
    if (row.type === 1) {
      loadLostList()
    } else {
      loadFoundList()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'lost') {
    loadLostList()
  } else {
    loadFoundList()
  }
}

const handleResetLost = () => {
  lostSearchForm.itemType = ''
  lostSearchForm.status = ''
  lostPagination.pageNum = 1
  loadLostList()
}

const handleResetFound = () => {
  foundSearchForm.itemType = ''
  foundSearchForm.status = ''
  foundPagination.pageNum = 1
  loadFoundList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.student-lost-found {
  padding: 20px;
  
  .search-form {
    margin-bottom: 15px;
  }
  
  .action-bar {
    margin-bottom: 15px;
  }
}
</style>

<template>
  <div class="admin-notice">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">发布公告</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select v-model="searchForm.noticeType" placeholder="全部" clearable style="width: 120px">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="宿舍调整" value="宿舍调整" />
            <el-option label="安全提示" value="安全提示" />
            <el-option label="寒暑假" value="寒暑假" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.startTime" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.endTime" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="noticeType" label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.noticeType === '系统通知'" type="primary">系统通知</el-tag>
            <el-tag v-else-if="row.noticeType === '宿舍调整'" type="success">宿舍调整</el-tag>
            <el-tag v-else-if="row.noticeType === '安全提示'" type="warning">安全提示</el-tag>
            <el-tag v-else-if="row.noticeType === '寒暑假'" type="info">寒暑假</el-tag>
            <el-tag v-else type="info">{{ row.noticeType || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="title" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="分类" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择分类" style="width: 100%">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="宿舍调整" value="宿舍调整" />
            <el-option label="安全提示" value="安全提示" />
            <el-option label="寒暑假" value="寒暑假" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="置顶" prop="isTop">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const searchForm = ref({
  noticeType: '',
  startTime: '',
  endTime: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = ref({
  id: null,
  title: '',
  noticeType: '',
  content: '',
  isTop: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const title = computed(() => isEdit.value ? '编辑公告' : '发布公告')

const getList = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      noticeType: searchForm.value.noticeType || undefined,
      startTime: searchForm.value.startTime || undefined,
      endTime: searchForm.value.endTime || undefined
    }
    const res = await request.get('/notice/list', { params })
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
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

const handleReset = () => {
  searchForm.value = { noticeType: '', startTime: '', endTime: '' }
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

const handleAdd = () => {
  form.value = { id: null, title: '', noticeType: '', content: '', isTop: 0 }
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  isEdit.value = true
  dialogVisible.value = true
}

const handleTop = async (row) => {
  try {
    const newTop = row.isTop === 1 ? 0 : 1
    await request.put(`/notice/top/${row.id}?isTop=${newTop}`)
    ElMessage.success(newTop === 1 ? '置顶成功' : '取消置顶成功')
    getList()
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          ...form.value,
          publisherId: userStore.userInfo.id || userStore.userInfo.userId
        }
        if (isEdit.value) {
          await request.put('/notice', submitData)
          ElMessage.success('修改成功')
        } else {
          await request.post('/notice', submitData)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/notice/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.admin-notice {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .search-form {
    margin-bottom: 15px;
  }
}
</style>

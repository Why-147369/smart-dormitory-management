<template>
  <div class="student-visitor">
    <el-card>
      <template #header>
        <span>访客预约</span>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="预约申请" name="apply">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px">
            <el-form-item label="访客姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入访客手机号" />
            </el-form-item>
            <el-form-item label="来源地" prop="source">
              <el-input v-model="form.source" placeholder="请输入访客来源地" />
            </el-form-item>
            <el-form-item label="到访时间" prop="visitTime">
              <el-date-picker
                v-model="form.visitTime"
                type="datetime"
                placeholder="选择到访时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="到访目的" prop="purpose">
              <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入到访目的" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit">提交预约</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="我的预约" name="records">
          <el-table :data="tableData" style="width: 100%">
            <el-table-column prop="visitorName" label="访客姓名" width="100" />
            <el-table-column label="性别" width="60">
              <template #default="{ row }">
                {{ row.gender === 1 ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="source" label="来源地" width="120" />
            <el-table-column prop="visitTime" label="到访时间" width="160" />
            <el-table-column prop="purpose" label="到访目的" min-width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
                <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
                <el-tag v-else type="info">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 0" 
                  type="danger" 
                  size="small" 
                  @click="handleCancel(row)"
                >
                  取消
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('apply')
const formRef = ref(null)
const tableData = ref([])

const form = reactive({
  studentId: null,
  visitorName: '',
  gender: 1,
  phone: '',
  source: '',
  visitTime: '',
  purpose: ''
})

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  source: [{ required: true, message: '请输入来源地', trigger: 'blur' }],
  visitTime: [{ required: true, message: '请选择到访时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入到访目的', trigger: 'blur' }]
}

const getMyList = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await request.get(`/visitor/my/${userId}`)
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
    tableData.value = []
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/visitor/apply', {
          studentId: userStore.userInfo.userId,
          visitorName: form.visitorName,
          gender: form.gender,
          phone: form.phone,
          source: form.source,
          visitTime: form.visitTime,
          purpose: form.purpose
        })
        ElMessage.success('预约提交成功')
        handleReset()
        activeTab.value = 'records'
        getMyList()
      } catch (e) {
        console.error(e)
        ElMessage.error(e.message || '提交失败')
      }
    }
  })
}

const handleReset = () => {
  form.visitorName = ''
  form.gender = 1
  form.phone = ''
  form.source = ''
  form.visitTime = ''
  form.purpose = ''
  formRef.value?.resetFields()
}

const handleCancel = async (row) => {
  try {
    await request.put(`/visitor/cancel/${row.id}`)
    ElMessage.success('取消成功')
    getMyList()
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '取消失败')
  }
}

onMounted(() => {
  getMyList()
})
</script>

<style scoped>
.student-visitor {
  padding: 20px;
}
</style>

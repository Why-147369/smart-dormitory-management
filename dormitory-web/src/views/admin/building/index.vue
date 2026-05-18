<template>
  <div class="admin-building">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>楼栋管理</span>
          <el-button type="primary" @click="handleAdd">添加楼栋</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="buildingNumber" label="楼栋号" width="100" />
        <el-table-column prop="buildingName" label="楼栋名称" width="150" />
        <el-table-column prop="floorCount" label="楼层数" width="80" />
        <el-table-column prop="roomCount" label="每层房间数" width="100" />
        <el-table-column prop="managerName" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="title" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="楼栋号" prop="buildingNumber">
          <el-input v-model="form.buildingNumber" placeholder="如：A1" />
        </el-form-item>
        <el-form-item label="楼栋名称" prop="buildingName">
          <el-input v-model="form.buildingName" placeholder="如：1号楼" />
        </el-form-item>
        <el-form-item label="楼层数" prop="floorCount">
          <el-input-number v-model="form.floorCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="每层房间数" prop="roomCount">
          <el-input-number v-model="form.roomCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerId">
          <el-select 
            v-model="form.managerId" 
            placeholder="请选择负责人" 
            clearable
            @change="handleManagerChange"
          >
            <el-option
              v-for="item in managerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const managerList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: null,
  buildingNumber: '',
  buildingName: '',
  floorCount: 6,
  roomCount: 10,
  managerId: null,
  phone: ''
})

const rules = {
  buildingNumber: [{ required: true, message: '请输入楼栋号', trigger: 'blur' }],
  buildingName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  floorCount: [{ required: true, message: '请输入楼层数', trigger: 'blur' }]
}

const title = computed(() => isEdit.value ? '编辑楼栋' : '添加楼栋')

const getList = async () => {
  try {
    const res = await request.get('/building/list')
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getManagerList = async () => {
  try {
    const res = await request.get('/manager/list', { params: { pageNum: 1, pageSize: 100 } })
    const data = res.data
    if (data && data.records) {
      managerList.value = data.records
    } else if (Array.isArray(data)) {
      managerList.value = data
    }
  } catch (e) {
    console.error(e)
  }
}

const handleManagerChange = (managerId) => {
  if (managerId) {
    const manager = managerList.value.find(m => m.id === managerId)
    if (manager) {
      form.value.phone = manager.phone || ''
    }
  } else {
    form.value.phone = ''
  }
}

const handleAdd = () => {
  form.value = {
    id: null,
    buildingNumber: '',
    buildingName: '',
    floorCount: 6,
    roomCount: 10,
    managerId: null,
    phone: ''
  }
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { 
    ...row,
    managerId: row.managerId || null
  }
  isEdit.value = true
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put('/building', form.value)
          ElMessage.success('修改成功')
        } else {
          await request.post('/building', form.value)
          ElMessage.success('添加成功')
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
    await ElMessageBox.confirm('确定要删除该楼栋吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/building/${row.id}`)
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
  getManagerList()
})
</script>

<style scoped lang="scss">
.admin-building {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

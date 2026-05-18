<template>
  <div class="rules-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>宿舍公约管理</span>
          <el-button type="primary" @click="handleAdd">添加公约</el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 200px; margin-right: 10px;" />
        <el-select v-model="ruleType" placeholder="公约类型" clearable style="width: 150px; margin-right: 10px;">
          <el-option label="作息规定" :value="1" />
          <el-option label="卫生规定" :value="2" />
          <el-option label="安全规定" :value="3" />
          <el-option label="处罚条例" :value="4" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%; margin-top: 15px;" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="ruleType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.ruleType === 1" type="success">作息规定</el-tag>
            <el-tag v-else-if="row.ruleType === 2" type="primary">卫生规定</el-tag>
            <el-tag v-else-if="row.ruleType === 3" type="warning">安全规定</el-tag>
            <el-tag v-else-if="row.ruleType === 4" type="danger">处罚条例</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.ruleType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="作息规定" :value="1" />
            <el-option label="卫生规定" :value="2" />
            <el-option label="安全规定" :value="3" />
            <el-option label="处罚条例" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入内容" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const ruleType = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('添加公约')
const form = ref({
  id: null,
  title: '',
  ruleType: 1,
  content: ''
})

const loadData = async () => {
  try {
    const res = await request.get('/rule/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: keyword.value,
        ruleType: ruleType.value
      }
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  keyword.value = ''
  ruleType.value = null
  loadData()
}

const handleAdd = () => {
  form.value = {
    id: null,
    title: '',
    ruleType: 1,
    content: ''
  }
  dialogTitle.value = '添加公约'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑公约'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    if (form.value.id) {
      await request.put('/rule', form.value)
      ElMessage.success('修改成功')
    } else {
      await request.post('/rule', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条公约吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/rule/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (val) => {
  selectedRows.value = val
}

const selectedRows = ref([])

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.rules-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-bar {
    margin-bottom: 15px;
  }
  
  .pagination {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

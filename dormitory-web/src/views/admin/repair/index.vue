<template>
  <div class="admin-repair">
    <el-card>
      <template #header>
        <span>报修管理</span>
      </template>

      <div class="search-bar">
        <el-select v-model="searchForm.buildingId" placeholder="楼栋筛选" clearable style="width: 150px;">
          <el-option v-for="item in buildings" :key="item.id" :label="item.buildingName" :value="item.id" />
        </el-select>
        <el-select v-model="searchForm.typeId" placeholder="报修类型" clearable style="width: 140px;">
          <el-option v-for="item in repairTypes" :key="item.id" :label="item.typeName" :value="item.id" />
        </el-select>
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
        <el-button type="success" @click="handleExport">导出Excel</el-button>
      </div>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">报修总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-value" style="color: #e6a23c;">{{ statistics.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-value" style="color: #409eff;">{{ statistics.processing }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-value" style="color: #67c23a;">{{ statistics.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="tableData" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="repairNumber" label="报修单号" width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="buildingName" label="楼栋" width="80" />
        <el-table-column prop="roomNumber" label="宿舍号" width="100" />
        <el-table-column prop="repairTypeName" label="报修类型" width="100" />
        <el-table-column prop="isEmergency" label="紧急" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isEmergency === 1" type="danger" size="small">紧急</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import * as XLSX from 'xlsx'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const buildings = ref([])
const repairTypes = ref([])

const searchForm = reactive({
  buildingId: '',
  typeId: '',
  status: '',
  dateRange: []
})

const statistics = ref({
  total: 0,
  pending: 0,
  processing: 0,
  completed: 0
})

const loadStatistics = async () => {
  try {
    const res = await request.get('/repair/list', { params: { pageNum: 1, pageSize: 1000 } })
    const data = res.data?.records || []
    statistics.value = {
      total: res.data?.total || 0,
      pending: data.filter(d => d.status === 0).length,
      processing: data.filter(d => d.status === 1 || d.status === 2).length,
      completed: data.filter(d => d.status === 3).length
    }
  } catch (e) {
    console.error(e)
  }
}

const getBuildings = async () => {
  try {
    const res = await request.get('/building/list')
    buildings.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getRepairTypes = async () => {
  try {
    const res = await request.get('/repair/type/list')
    repairTypes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchForm.buildingId) params.buildingId = searchForm.buildingId
    if (searchForm.typeId) params.typeId = searchForm.typeId
    if (searchForm.status !== '' && searchForm.status !== null) params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await request.get('/repair/list', { params })
    let data = res.data?.records || []
    data.forEach(item => {
      if (item.images) {
        try {
          item.images = JSON.parse(item.images)
        } catch {
          item.images = []
        }
      }
    })
    tableData.value = data
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  getList()
}

const handleReset = () => {
  searchForm.buildingId = ''
  searchForm.typeId = ''
  searchForm.status = ''
  searchForm.dateRange = []
  getList()
}

const handleExport = async () => {
  const exportData = tableData.value.map(item => ({
    '报修单号': item.repairNumber,
    '学生姓名': item.studentName,
    '楼栋': item.buildingName,
    '宿舍号': item.roomNumber,
    '报修类型': item.repairTypeName,
    '故障描述': item.description,
    '是否紧急': item.isEmergency === 1 ? '是' : '否',
    '状态': ['', '待处理', '已接单', '维修中', '已完成', '已取消'][item.status] || '',
    '提交时间': item.createTime
  }))

  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '报修记录')
  XLSX.writeFile(workbook, `报修记录_${new Date().toISOString().split('T')[0]}.xlsx`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条报修记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/repair/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  getBuildings()
  getRepairTypes()
  getList()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.admin-repair {
  .search-bar {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
  }

  .pagination {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
  }

  .stat-card {
    .stat-content {
      text-align: center;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }

      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 5px;
      }
    }
  }
}
</style>

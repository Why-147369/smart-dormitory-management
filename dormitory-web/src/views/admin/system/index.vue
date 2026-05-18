<template>
  <div class="admin-system">
    <el-card>
      <template #header>
        <span>系统设置</span>
      </template>
      
      <el-form label-width="120px">
        <el-form-item label="数据库备份">
          <el-button type="primary" @click="handleBackup" :loading="backingUp">
            {{ backingUp ? '备份中...' : '立即备份' }}
          </el-button>
          <span class="tip">点击按钮将数据库备份到项目根目录的 backups 文件夹</span>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <el-descriptions title="系统信息" :column="2" border>
        <el-descriptions-item label="系统版本">智能宿舍管理系统 v1.0</el-descriptions-item>
        <el-descriptions-item label="技术栈">Spring Boot + Vue3 + MySQL</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const backingUp = ref(false)

const handleBackup = async () => {
  backingUp.value = true
  try {
    const res = await request.post('/system/backup')
    if (res.code === 200) {
      ElMessage.success('备份成功：' + res.data.fileName)
    } else {
      ElMessage.error(res.message || '备份失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('备份失败')
  } finally {
    backingUp.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-system {
  .tip {
    margin-left: 15px;
    color: #999;
    font-size: 12px;
  }
}
</style>

<template>
  <el-container style="height:100vh">
    <el-aside width="200px" style="background:#1a1a2e;color:#fff">
      <div style="padding:20px;text-align:center;font-size:18px;font-weight:bold;border-bottom:1px solid #333">维修人员端</div>
      <el-menu router :default-active="route.path" background-color="#1a1a2e" text-color="#bfcbd9" active-text-color="#409EFF" style="border:none">
        <el-menu-item index="/maintenance/dashboard">工作台</el-menu-item>
        <el-menu-item index="/maintenance/repair">我的工单</el-menu-item>
        <el-menu-item index="/maintenance/profile">修改密码</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:flex-end;padding:0 20px">
        <span style="margin-right:16px;color:#666">{{ userStore.userInfo?.name || '维修人员' }}</span>
        <el-button text @click="handleLogout">退出</el-button>
      </el-header>
      <el-main style="background:#f5f7fa"><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    userStore.logout(); router.push('/login')
  }).catch(() => {})
}
</script>

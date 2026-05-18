<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>宿管端</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/manager/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/manager/student">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/repair">
          <el-icon><Tools /></el-icon>
          <span>报修管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/checkin">
          <el-icon><Clock /></el-icon>
          <span>打卡管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/utility">
          <el-icon><Coin /></el-icon>
          <span>水电费管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/visitor">
          <el-icon><User /></el-icon>
          <span>访客管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/health">
          <el-icon><Histogram /></el-icon>
          <span>卫生检查</span>
        </el-menu-item>
        <el-menu-item index="/manager/room-change">
          <el-icon><Switch /></el-icon>
          <span>换寝审批</span>
        </el-menu-item>
        <el-menu-item index="/manager/civilized">
          <el-icon><Histogram /></el-icon>
          <span>文明宿舍</span>
        </el-menu-item>
        <el-menu-item index="/manager/emergency">
          <el-icon><Warning /></el-icon>
          <span>紧急求助</span>
        </el-menu-item>
        <el-menu-item index="/manager/notice">
          <el-icon><Bell /></el-icon>
          <span>公告查看</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-title">智能宿舍管理系统 - 宿管端</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo.name || userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  HomeFilled, Tools, User, ArrowDown, Clock, Coin, Histogram, Switch, Bell, Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/manager/profile')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(async () => {
      try { await logout() } catch (e) { console.log(e) }
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #2b3a4a;
    
    h2 {
      color: #fff;
      font-size: 18px;
      margin: 0;
    }
  }
  
  .sidebar-menu {
    border-right: none;
  }
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  
  .header-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      gap: 8px;
    }
  }
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>

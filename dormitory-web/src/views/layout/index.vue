<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>宿舍管理</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/student">学生管理</el-menu-item>
          <el-menu-item index="/admin/manager">宿管管理</el-menu-item>
          <el-menu-item index="/admin/maintenance">维修人员管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="dormitory">
          <template #title>
            <el-icon><House /></el-icon>
            <span>宿舍管理</span>
          </template>
          <el-menu-item index="/admin/building">楼栋管理</el-menu-item>
          <el-menu-item index="/admin/room">宿舍管理</el-menu-item>
          <el-menu-item index="/admin/bed">床位管理</el-menu-item>
          <el-menu-item index="/admin/assignment">入住分配</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="business">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/admin/repair">报修管理</el-menu-item>
          <el-menu-item index="/admin/repair-type">报修类型</el-menu-item>
          <el-menu-item index="/admin/visitor">访客管理</el-menu-item>
          <el-menu-item index="/admin/checkin">打卡管理</el-menu-item>
          <el-menu-item index="/admin/utility">水电费管理</el-menu-item>
          <el-menu-item index="/admin/room-change">换寝管理</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/admin/health">
          <el-icon><FirstAidKit /></el-icon>
          <span>卫生检查</span>
        </el-menu-item>
        <el-menu-item index="/admin/civilized">
          <el-icon><Trophy /></el-icon>
          <span>文明宿舍</span>
        </el-menu-item>
        <el-menu-item index="/admin/emergency">
          <el-icon><Warning /></el-icon>
          <span>紧急求助</span>
        </el-menu-item>
        <el-menu-item index="/admin/lost-found">
          <el-icon><Search /></el-icon>
          <span>失物招领</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/logs">
          <el-icon><Document /></el-icon>
          <span>系统日志</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/system">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>客服管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/notice">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/rules">
          <el-icon><Document /></el-icon>
          <span>公约管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
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
  HomeFilled, Setting, House, Document, User, ArrowDown, Bell, FirstAidKit, Trophy, Warning, Search, Grid, ChatDotRound 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const currentRoute = computed(() => {
  return route.meta?.title || ''
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(async () => {
      try { await logout() } catch (e) { console.log(e) }
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/admin/profile')
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

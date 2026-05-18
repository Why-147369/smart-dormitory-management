<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>学生端</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/student/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/student/checkin">
          <el-icon><Clock /></el-icon>
          <span>打卡</span>
        </el-menu-item>
        <el-menu-item index="/student/repair">
          <el-icon><Tools /></el-icon>
          <span>报修</span>
        </el-menu-item>
        <el-menu-item index="/student/utility">
          <el-icon><Money /></el-icon>
          <span>水电费</span>
        </el-menu-item>
        <el-menu-item index="/student/visitor">
          <el-icon><User /></el-icon>
          <span>访客预约</span>
        </el-menu-item>
        <el-menu-item index="/student/room-change">
          <el-icon><Switch /></el-icon>
          <span>换寝申请</span>
        </el-menu-item>
        <el-menu-item index="/student/announcement">
          <el-icon><Bell /></el-icon>
          <span>公告</span>
        </el-menu-item>
        <el-menu-item index="/student/rules">
          <el-icon><Document /></el-icon>
          <span>宿舍公约</span>
        </el-menu-item>
        <el-menu-item index="/student/civilized">
          <el-icon><Trophy /></el-icon>
          <span>文明宿舍</span>
        </el-menu-item>
        <el-menu-item index="/student/emergency">
          <el-icon><Warning /></el-icon>
          <span>紧急求助</span>
        </el-menu-item>
        <el-menu-item index="/student/lost-found">
          <el-icon><Search /></el-icon>
          <span>失物招领</span>
        </el-menu-item>
        <el-menu-item index="/student/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能客服</span>
        </el-menu-item>
        <el-menu-item index="/student/health">
          <el-icon><FirstAidKit /></el-icon>
          <span>卫生检查</span>
        </el-menu-item>
        <el-menu-item index="/student/message">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-title">智能宿舍管理系统 - 学生端</div>
        <div class="header-right">
          <el-badge :value="unreadMessageCount" :hidden="unreadMessageCount === 0" :max="99">
            <el-icon class="header-icon" @click="router.push('/student/message')"><Bell /></el-icon>
          </el-badge>
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
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { 
  HomeFilled, Tools, Money, User, ArrowDown, Clock, Bell, Document, Trophy, FirstAidKit, Switch, Warning, Search, ChatDotRound 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const unreadMessageCount = ref(0)

const loadUnreadCount = async () => {
  try {
    const res = await request.get('/message/unread/count')
    unreadMessageCount.value = res.data || 0
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadUnreadCount()
  setInterval(loadUnreadCount, 30000)
})

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/student/profile')
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
    display: flex;
    align-items: center;
    gap: 20px;
    
    .header-icon {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      
      &:hover {
        color: #409eff;
      }
    }
    
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

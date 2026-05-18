<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>智能宿舍管理系统</h1>
      </div>
      <el-tabs v-model="activeTab" class="role-tabs">
        <el-tab-pane label="学生登录" name="student">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item label="学号" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入学号" 
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="宿管登录" name="manager">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item label="账号" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入账号" 
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="维修人员" name="maintenance">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item label="账号" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入账号" prefix-icon="User" size="large"/>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="管理员登录" name="admin">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item label="账号" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入账号" 
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const activeTab = ref('student')

const loginForm = ref({
  username: '',
  password: ''
})

const userTypeMap = {
  student: 1,
  manager: 2,
  admin: 3,
  maintenance: 4
}

const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const userType = userTypeMap[activeTab.value]
        const res = await login({ ...loginForm.value, userType })
        const data = res.data
        userStore.setToken(data.token, data.userType)
        userStore.setUserInfo({
          userId: data.userId,
          userType: data.userType,
          username: data.username,
          name: data.name,
          buildingId: data.buildingId
        })
        
        ElMessage.success('登录成功')
        
        const loginUserType = data.userType
        if (loginUserType === 1) {
          router.push('/student/dashboard')
        } else if (loginUserType === 2) {
          router.push('/manager/dashboard')
        } else if (loginUserType === 4) {
          router.push('/maintenance/dashboard')
        } else {
          router.push('/dashboard')
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 22px;
    font-weight: 500;
    color: #1a1a1a;
    margin: 0;
  }
}

.role-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: #e4e7ed;
  }
  
  :deep(.el-tabs__item) {
    font-size: 15px;
    height: 44px;
    line-height: 44px;
    color: #606266;
    
    &.is-active {
      color: #1a1a1a;
      font-weight: 600;
    }
    
    &:hover {
      color: #1a1a1a;
    }
  }
  
  :deep(.el-tabs__active-bar) {
    background: #1a1a1a;
    height: 3px;
    border-radius: 2px;
  }
}

.login-form {
  .el-input {
    height: 42px;
  }
  
  :deep(.el-input__wrapper) {
    background: #fff !important;
    box-shadow: 0 0 0 1px #e4e7ed inset;
    border-radius: 4px;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #1a1a1a inset;
    }
  }
  
  :deep(.el-input__inner) {
    background: transparent !important;
  }
  
  :deep(.el-input__inner::-webkit-input-placeholder) {
    color: #c0c4cc;
  }
  
  :deep(input:-webkit-autofill),
  :deep(input:-webkit-autofill:hover),
  :deep(input:-webkit-autofill:focus) {
    background: #fff !important;
    -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  }
  
  .login-button {
    width: 100%;
    height: 42px;
    font-size: 15px;
    font-weight: 500;
    background: #1a1a1a;
    border-color: #1a1a1a;
    border-radius: 4px;
    
    &:hover {
      background: #333;
      border-color: #333;
    }
  }
}

:deep(.el-form-item__label) {
  color: #3a3a3a;
  font-weight: 500;
}
</style>

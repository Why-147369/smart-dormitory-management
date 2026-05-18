<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="studentInfo.avatar || defaultAvatar">
              <el-icon :size="50"><User /></el-icon>
            </el-avatar>
            <el-upload
              class="avatar-upload"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-button size="small" type="primary">更换头像</el-button>
            </el-upload>
          </div>
          <div class="user-info">
            <h3>{{ studentInfo.name || '学生' }}</h3>
            <p class="username">{{ studentInfo.studentNumber }}</p>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">楼栋：</span>
              <span class="value">{{ studentInfo.buildingName || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="label">宿舍号：</span>
              <span class="value">{{ studentInfo.roomNumber || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="label">床位号：</span>
              <span class="value">{{ studentInfo.bedNumber || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="label">专业：</span>
              <span class="value">{{ studentInfo.major || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">班级：</span>
              <span class="value">{{ studentInfo.className || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="profile">
              <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
                <el-form-item label="学号" prop="studentNumber">
                  <el-input v-model="profileForm.studentNumber" disabled />
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="profileForm.name" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeTab = ref('profile')
const studentInfo = ref({})

const profileFormRef = ref(null)
const profileForm = reactive({
  studentNumber: '',
  name: '',
  phone: ''
})

const profileRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}

const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadStudentInfo = async () => {
  try {
    const res = await request.get('/student/me')
    studentInfo.value = res.data || {}
    profileForm.studentNumber = studentInfo.value.studentNumber || ''
    profileForm.name = studentInfo.value.name || ''
    profileForm.phone = studentInfo.value.phone || ''
  } catch (e) {
    console.error(e)
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleAvatarUpload = async (options) => {
  const file = options.file
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await request.post('/upload', formData)
    const avatarUrl = res.data
    
    await request.put('/student/profile', { avatar: avatarUrl })
    studentInfo.value.avatar = avatarUrl
    userStore.userInfo.avatar = avatarUrl
    ElMessage.success('头像更新成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('头像上传失败')
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.put('/student/profile', {
          name: profileForm.name,
          phone: profileForm.phone
        })
        studentInfo.value.name = profileForm.name
        studentInfo.value.phone = profileForm.phone
        userStore.userInfo.name = profileForm.name
        ElMessage.success('信息更新成功')
      } catch (e) {
        console.error(e)
        ElMessage.error('更新失败')
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.put('/student/password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (e) {
        console.error(e)
        ElMessage.error(e.message || '修改密码失败')
      }
    }
  })
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  .profile-card {
    text-align: center;
    
    .avatar-section {
      margin-bottom: 20px;
      
      .el-avatar {
        margin-bottom: 15px;
      }
      
      .avatar-upload {
        display: inline-block;
      }
    }
    
    .user-info {
      h3 {
        margin: 0 0 5px;
        font-size: 20px;
      }
      
      .username {
        color: #999;
        margin: 0 0 10px;
      }
    }
    
    .info-list {
      text-align: left;
      
      .info-item {
        padding: 8px 0;
        
        .label {
          color: #666;
        }
        
        .value {
          color: #333;
        }
      }
    }
  }
  
  .profile-form,
  .password-form {
    max-width: 400px;
    margin-top: 20px;
  }
}
</style>

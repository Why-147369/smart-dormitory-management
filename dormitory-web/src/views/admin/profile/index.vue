<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="adminInfo.avatar || defaultAvatar">
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
            <h3>{{ adminInfo.name || '管理员' }}</h3>
            <p class="username">@{{ adminInfo.username }}</p>
            <el-tag :type="adminInfo.status === 1 ? 'success' : 'danger'">
              {{ adminInfo.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ adminInfo.createTime || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="profile">
              <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getCurrentAdmin, updateProfile, changePassword, uploadFile } from '@/api/admin'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('profile')
const defaultAvatar = ''

const adminInfo = ref({})
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  username: '',
  name: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadAdminInfo = async () => {
  try {
    const res = await getCurrentAdmin()
    adminInfo.value = res.data || {}
    profileForm.username = adminInfo.value.username || ''
    profileForm.name = adminInfo.value.name || ''
    profileForm.phone = adminInfo.value.phone || ''
  } catch (e) {
    console.error(e)
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateProfile({
          name: profileForm.name,
          phone: profileForm.phone
        })
        ElMessage.success('保存成功')
        loadAdminInfo()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        userStore.logout()
        router.push('/login')
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  const file = options.file
  
  try {
    const res = await uploadFile(file)
    const avatarUrl = res.data
    
    await updateProfile({ avatar: avatarUrl })
    adminInfo.value.avatar = avatarUrl
    ElMessage.success('头像更新成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  loadAdminInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  .profile-card {
    text-align: center;

    .avatar-section {
      padding: 20px 0;

      .el-avatar {
        margin-bottom: 15px;
      }

      .avatar-upload {
        margin-top: 10px;
      }
    }

    .user-info {
      h3 {
        margin: 10px 0 5px;
        font-size: 20px;
      }

      .username {
        color: #999;
        margin-bottom: 10px;
      }
    }

    .info-list {
      text-align: left;

      .info-item {
        padding: 8px 0;
        display: flex;

        .label {
          color: #999;
          width: 80px;
        }

        .value {
          color: #333;
        }
      }
    }
  }

  .profile-form,
  .password-form {
    max-width: 500px;
    padding: 20px;
  }
}
</style>

<template>
  <el-card style="max-width:500px">
    <template #header><span>修改密码</span></template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">修改密码</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
const formRef = ref(null)
const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== form.value.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const uid = userStore.userInfo.userId
    await request.put('/maintenance/password', null, { params: { id: uid, oldPassword: form.value.oldPassword, newPassword: form.value.newPassword } })
    ElMessage.success('密码修改成功')
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  })
}
</script>

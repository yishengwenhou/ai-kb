<template>
  <div class="personal-center">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar :size="120" :src="avatarUrl || undefined">
              <el-icon v-if="!avatarUrl" :size="60"><UserFilled /></el-icon>
            </el-avatar>
            <div class="upload-tip">
              <el-icon><Upload /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
        </div>

        <!-- 信息表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="所属部门">
            <el-input v-model="deptName" disabled />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改密码 -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handlePasswordChange" :loading="passwordLoading">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Upload } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import type { User } from '@/types'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'

const userStore = useUserStore()

// 上传配置
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/resource/avatar`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 当前用户信息
const avatarUrl = computed(() => userStore.avatarUrl || userStore.userInfo?.avatar)
const deptName = computed(() => userStore.userInfo?.deptName || '无')

// 表单数据
const form = ref<Partial<User>>({
  username: '',
  realName: '',
  email: '',
  phone: ''
})

const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单验证规则
const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)

// 密码验证规则
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
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

// 加载用户信息
onMounted(() => {
  if (userStore.userInfo) {
    form.value = {
      username: userStore.userInfo.username,
      realName: userStore.userInfo.realName,
      email: userStore.userInfo.email || '',
      phone: userStore.userInfo.phone || ''
    }
  }
})

// 头像上传前校验
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.success && response.data) {
    userStore.setUserInfo({
      ...userStore.userInfo!,
      avatarUrl: response.data
    })
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 提交个人信息修改
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userApi.updateProfile(form.value)
        // 更新 store 中的用户信息
        if (userStore.userInfo) {
          userStore.setUserInfo({
            ...userStore.userInfo,
            ...form.value
          })
        }
        ElMessage.success('个人信息修改成功')
      } catch (error) {
        ElMessage.error('个人信息修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 修改密码
const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await userApi.changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 清空表单
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        // 退出登录
        userStore.clearAuth()
        window.location.href = '/login'
      } catch (error) {
        ElMessage.error('密码修改失败，请检查原密码是否正确')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.personal-center {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.profile-content {
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.avatar-uploader :deep(.el-avatar) {
  border: 2px dashed #dcdfe6;
  transition: all 0.3s;
}

.avatar-uploader:hover :deep(.el-avatar) {
  border-color: #409eff;
}

.upload-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s;
}

.avatar-uploader:hover .upload-tip {
  color: #409eff;
}

.profile-form {
  flex: 1;
}

.password-card {
  max-width: 600px;
}

.password-form {
  max-width: 400px;
}
</style>

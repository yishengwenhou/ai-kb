<template>
  <div class="user-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建用户
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="search-wrapper">
      <el-form :model="filterForm" inline class="search-form">
        <div class="search-inputs">
          <el-form-item>
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索用户名/姓名/手机号"
              style="width: 240px"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-tree-select
              v-model="filterForm.deptId"
              :data="departments"
              :props="{ label: 'deptName', value: 'id' }"
              placeholder="所属部门"
              clearable
              check-strictly
              style="width: 180px"
            />
          </el-form-item>

          <el-form-item>
            <el-select
              v-model="filterForm.status"
              placeholder="用户状态"
              clearable
              style="width: 140px"
            >
              <template #prefix>
                <el-icon><InfoFilled /></el-icon>
              </template>
              <el-option label="正常" :value="0" />
              <el-option label="禁用" :value="1" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-select
              v-model="filterForm.role"
              placeholder="用户角色"
              clearable
              style="width: 160px"
              :loading="rolesLoading"
            >
              <template #prefix>
                <el-icon><Avatar /></el-icon>
              </template>
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="search-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl || row.avatar || undefined">
              <el-icon v-if="!row.avatarUrl && !row.avatar"><UserIcon /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="deptName" label="部门" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.status === 0 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新建用户'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="头像" prop="avatarUrl">
          <div class="avatar-upload-wrapper">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-avatar v-if="formData.avatarUrl" :size="80" :src="formData.avatarUrl" />
              <div v-else class="avatar-uploader-icon">
                <el-icon :size="24"><Plus /></el-icon>
                <span class="upload-tip">点击上传</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="所属部门" prop="deptId">
          <el-tree-select
            v-model="formData.deptId"
            :data="departments"
            :props="{ label: 'deptName', value: 'id' }"
            placeholder="请选择所属部门"
            clearable
            check-strictly
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { departmentApi } from '@/api/department'
import { roleApi } from '@/api/role'
import type { User, Department, Role } from '@/types'
// 记得在 import 中添加 InfoFilled, Refresh, Avatar
import { 
  Plus, 
  Search, 
  User as UserIcon, 
  Refresh,     // 新增
  InfoFilled,  // 新增
  Avatar       // 新增
} from '@element-plus/icons-vue'

// 筛选表单
const filterForm = reactive({
  keyword: '',
  deptId: undefined as number | undefined,
  status: undefined as number | undefined,
  role: undefined as number | undefined
})

const users = ref<User[]>([])
const loading = ref(false)
const departments = ref<Department[]>([])
const roles = ref<Role[]>([])
const rolesLoading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive<Partial<User>>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatarUrl: '',
  avatarHash: undefined,
  gender: 0,
  deptId: undefined,
  status: 0
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.getList({
      keyword: filterForm.keyword || undefined,
      deptId: filterForm.deptId,
      status: filterForm.status,
      role: filterForm.role,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    users.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载部门列表
const loadDepartments = async () => {
  try {
    const response = await departmentApi.getTree()
    // 直接使用树形结构，不要扁平化
    departments.value = response
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

// 加载角色列表
const loadRoles = async () => {
  rolesLoading.value = true
  try {
    const response = await roleApi.getAll()
    roles.value = response.records
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    rolesLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadUsers()
}

// 重置筛选条件
const handleReset = () => {
  filterForm.keyword = ''
  filterForm.deptId = undefined
  filterForm.status = undefined
  filterForm.role = undefined
  pagination.pageNum = 1
  loadUsers()
}

// 新建
const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    username: '',
    realName: '',
    email: '',
    phone: '',
    avatarUrl: '',
    avatarHash: undefined,
    gender: 0,
    deptId: undefined,
    status: 0
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (item: User) => {
  isEdit.value = true
  Object.assign(formData, {
    ...item
  })
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (item: User) => {
  const action = item.status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${item.realName || item.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.update({
      id: item.id,
      status: item.status === 0 ? 1 : 0
    })
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
    }
  }
}

// 删除用户（后端需要传用户ID）
const handleDelete = async (item: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${item.realName || item.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.delete(item.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file: File) => {
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

// 头像上传
const handleAvatarUpload = async (options: any) => {
  const { file } = options

  const uploadFormData = new FormData()
  uploadFormData.append('file', file)

  try {
    const uploadResponse = await fetch('/api/resource/avatar', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`
      },
      body: uploadFormData
    })
    const result = await uploadResponse.json()
    if (result.success && result.data) {
      formData.avatarUrl = result.data
      formData.avatarHash = result.hash
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(result.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await userApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await userApi.create(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadUsers()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadUsers()
  loadDepartments()
  loadRoles()
})
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 移除原有的 filter-card 样式，使用新的 search-wrapper */
.search-wrapper {
  background-color: #fff;
  padding: 18px 20px;
  border-radius: 8px; /* 更圆润的边角 */
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); /* 轻微的悬浮阴影 */
}

.search-form {
  display: flex;
  justify-content: space-between; /* 左右两端对齐 */
  align-items: center;
  width: 100%;
}

.search-inputs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px; /* 控件之间的间距 */
}

/* 覆盖 Element Plus 默认的 form-item margin，让布局更紧凑 */
.search-wrapper :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

/* 按钮区样式 */
.search-actions {
  display: flex;
  gap: 10px;
  margin-left: 20px; /* 防止屏幕缩小时贴得太近 */
}

/* 响应式处理：屏幕小的时候自动换行 */
@media screen and (max-width: 1200px) {
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-actions {
    margin-left: 0;
    margin-top: 16px;
    width: 100%;
    justify-content: flex-end; /* 按钮靠右 */
  }
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-right {
  display: flex;
  gap: 12px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* 头像上传样式 */
.avatar-upload-wrapper {
  width: fit-content;
}

.avatar-uploader :deep(.el-upload) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  cursor: pointer;
  border: 2px dashed #d9d9d;
  border-radius: 8px;
  padding: 24px;
  transition: all 0.3s;
  background-color: #fafafa;
  width: 120px;
  height: 120px;
}

.avatar-uploader-icon .el-icon {
  margin-bottom: 8px;
}

.avatar-uploader-icon .upload-tip {
  font-size: 12px;
  color: #8c939d;
}

.avatar-uploader-icon:hover {
  color: #409eff;
  border-color: #409eff;
  background-color: #f0f7ff;
}

.avatar-uploader-icon:hover .upload-tip {
  color: #409eff;
}
</style>

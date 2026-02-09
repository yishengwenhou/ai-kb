<template>
  <div class="department-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>部门管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建部门
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 左侧部门树 -->
      <el-card class="tree-card">
        <template #header>
          <div class="card-header">
            <span>组织架构</span>
            <el-button link type="primary" @click="loadTree">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </template>
        <el-tree
          ref="treeRef"
          :data="deptTree"
          :props="{ label: 'deptName', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ node.label }}</span>
            </div>
          </template>
        </el-tree>
      </el-card>

      <!-- 右侧部门详情/列表 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>{{ currentDept ? '部门详情' : '部门列表' }}</span>
            <el-input
              v-if="!currentDept"
              v-model="searchKeyword"
              placeholder="搜索部门"
              style="width: 200px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </template>

        <!-- 选中部门的详情 -->
        <div v-if="currentDept" class="dept-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="部门名称">
              {{ currentDept.deptName }}
            </el-descriptions-item>
            <el-descriptions-item label="上级部门">
              {{ getParentName(currentDept.parentId) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentDept.status === 0 ? 'success' : 'danger'">
                {{ currentDept.status === 0 ? '正常' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="负责人">
              {{ getLeaderName(currentDept) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ currentDept.createTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-actions">
            <el-button type="primary" @click="handleEdit(currentDept)">
              编辑
            </el-button>
            <el-button type="success" @click="handleAddChild(currentDept)">
              添加子部门
            </el-button>
            <el-button type="danger" @click="handleDelete(currentDept)">
              删除
            </el-button>
            <el-button @click="currentDept = null">
              返回列表
            </el-button>
          </div>
        </div>

        <!-- 部门列表 -->
        <div v-else>
          <el-table :data="deptList" style="width: 100%" v-loading="loading">
            <el-table-column prop="deptName" label="部门名称" width="200" />
            <el-table-column prop="parentName" label="上级部门" width="200">
              <template #default="{ row }">
                {{ getParentName(row.parentId) }}
              </template>
            </el-table-column>
            <el-table-column label="负责人" width="120">
              <template #default="{ row }">
                {{ getLeaderName(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
                  {{ row.status === 0 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button link type="success" size="small" @click="handleAddChild(row)">
                  添加子部门
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
              @size-change="loadDeptList"
              @current-change="loadDeptList"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑部门' : '新建部门'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id' }"
            placeholder="请选择上级部门（不选则为根部门）"
            clearable
            check-strictly
          />
        </el-form-item>

        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>

        <el-form-item label="负责人" prop="leader">
          <el-select
            v-model="formData.leader"
            placeholder="请选择负责人"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.realName || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  OfficeBuilding
} from '@element-plus/icons-vue'
import { departmentApi } from '@/api/department'
import { userApi } from '@/api/user'
import type { Department, User } from '@/types'

const treeRef = ref()
const searchKeyword = ref('')
const deptTree = ref<Department[]>([])
const deptList = ref<Department[]>([])
const currentDept = ref<Department | null>(null)
const loading = ref(false)
const userList = ref<User[]>([])
const usersLoading = ref(false)

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

const formData = reactive<Partial<Department>>({
  parentId: null,
  deptName: '',
  status: 0
})

const formRules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ]
}

// 加载部门树
const loadTree = async () => {
  try {
    const response = await departmentApi.getTree()
    deptTree.value = response || []
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

// 加载部门列表
const loadDeptList = async () => {
  loading.value = true
  try {
    const response = await departmentApi.getPage({
      keyword: searchKeyword.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    // deptList.value = response.data!.records
    // pagination.total = response.data!.total
    deptList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('加载部门列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载用户列表（用于选择负责人）
const loadUsers = async () => {
  usersLoading.value = true
  try {
    const response = await userApi.getList({
      pageNum: 1,
      pageSize: 1000
    })
    userList.value = response.records
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    usersLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadDeptList()
}

// 节点点击
const handleNodeClick = (data: Department) => {
  currentDept.value = data
}

// 获取上级部门名称
const getParentName = (parentId: number | null) => {
  if (!parentId) return '-'
  const findParent = (list: Department[], id: number): string => {
    for (const item of list) {
      if (item.id === id) return item.deptName
      if (item.children) {
        const found = findParent(item.children, id)
        if (found !== '-') return found
      }
    }
    return '-'
  }
  return findParent(deptTree.value, parentId)
}

// 获取负责人姓名
const getLeaderName = (dept: Department) => {
  if (typeof dept.leader === 'object' && dept.leader) {
    // 如果 leader 是 User 对象，使用 realName
    return (dept.leader as any).realName || '-'
  }
  return dept.leaderName || '-'
}

// 新建
const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    parentId: null,
    deptName: '',
    leader: undefined,
    status: 0
  })
  dialogVisible.value = true
}

// 添加子部门
const handleAddChild = (parent: Department) => {
  isEdit.value = false
  Object.assign(formData, {
    parentId: parent.id,
    deptName: '',
    leader: undefined,
    status: 0
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (item: Department) => {
  isEdit.value = true
  
  // 深拷贝一份数据，避免修改原列表数据
  const data = { ...item }
  
  // 关键修复：如果 leader 是对象（User），提取其 ID 赋值给 leader 字段
  if (data.leader && typeof data.leader === 'object') {
    // 这里使用 any 强转是因为 Typescript 类型定义可能是 number | object
    data.leader = (data.leader as any).id
  }
  
  Object.assign(formData, data)
  dialogVisible.value = true
}
// 删除
const handleDelete = async (item: Department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${item.deptName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await departmentApi.delete(item.id)
    ElMessage.success('删除成功')
    currentDept.value = null
    loadTree()
    loadDeptList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
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
        await departmentApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await departmentApi.create(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadTree()
      loadDeptList()
      if (currentDept.value) {
        currentDept.value = null
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadTree()
  loadDeptList()
  loadUsers()
})
</script>

<style scoped>
.department-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.content-container {
  display: flex;
  gap: 20px;
}

.tree-card {
  width: 280px;
  height: fit-content;
}

.detail-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.dept-detail {
  padding: 20px 0;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>

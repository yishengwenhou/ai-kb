<template>
  <div class="knowledge-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>知识库管理</h2>
        <!-- 空间切换标签 -->
        <el-tabs v-model="currentScope" @tab-change="handleScopeChange" class="space-tabs">
          <el-tab-pane label="个人空间" name="mine"></el-tab-pane>
          <el-tab-pane label="部门空间" name="dept"></el-tab-pane>
          <el-tab-pane label="公共空间" name="public"></el-tab-pane>
        </el-tabs>
      </div>
      <div class="header-right">
        <div class="filter-search">
          <el-icon><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索知识库名称"
            @keyup.enter="handleSearch"
          />
          <el-icon v-if="searchKeyword" class="clear-btn" @click="handleClear"><CircleClose /></el-icon>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建知识库
        </el-button>
      </div>
    </div>

    <!-- 知识库列表 -->
    <div class="knowledge-list">
      <div class="kb-grid">
        <div
          v-for="item in knowledgeList"
          :key="item.id"
          class="kb-card"
          :class="getCardClass(item)"
          @click="handleView(item.id)"
        >
          <!-- 封面区域 -->
          <div class="card-cover" :style="getCoverStyle(item)">
            <div class="kb-content">
              <div class="kb-title">{{ item.name }}</div>
              <div class="kb-desc">{{ item.introduction || item.description || '暂无简介' }}</div>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)" class="cover-actions">
              <el-icon class="more-btn" :size="18" @click.stop><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 底部标签 -->
          <div class="card-footer">
            <el-tag :type="getOwnerTypeType(item.ownerType)" size="small">
              {{ getOwnerTypeLabel(item.ownerType) }}
            </el-tag>
            <el-tag :type="getVisibilityType(item.visibility)" size="small">
              {{ getVisibilityLabel(item.visibility) }}
            </el-tag>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusLabel(item.status) }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑知识库' : '新建知识库'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="知识库名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入知识库名称" />
        </el-form-item>

        <el-form-item label="简介" prop="introduction">
          <el-input
            v-model="formData.introduction"
            type="textarea"
            :rows="2"
            placeholder="请输入知识库简介（将显示在卡片上）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入知识库详细描述"
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverUrl">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :http-request="handleCoverUpload"
          >
            <div class="cover-preview" v-if="formData.coverUrl" :style="{ backgroundImage: `url(${formData.coverUrl})` }">
              <div class="cover-mask">
                <el-icon><Plus /></el-icon>
                <span>更换封面</span>
              </div>
            </div>
            <div class="cover-placeholder" v-else>
              <el-icon :size="24"><Picture /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="访问权限" prop="visibility">
          <el-select v-model="formData.visibility" placeholder="请选择访问权限">
            <el-option label="私有" :value="0" />
            <el-option label="公开" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  MoreFilled,
  Edit,
  Delete,
  Picture,
  CircleClose
} from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api/knowledge'
import type { KnowledgeBase, SpaceType } from '@/types'

const router = useRouter()

// 数据
const currentScope = ref<SpaceType>('mine')
const searchKeyword = ref('')
const knowledgeList = ref<KnowledgeBase[]>([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive<Partial<KnowledgeBase>>({
  name: '',
  introduction: '',
  description: '',
  visibility: 0,
  ownerType: 10,
  ownerId: 0,
  status: 0,
  coverUrl: '',
  remark: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' }
  ],
  visibility: [
    { required: true, message: '请选择访问权限', trigger: 'change' }
  ]
}

// 空间切换
const handleScopeChange = () => {
  pagination.pageNum = 1
  loadList()
}

// 加载知识库列表
const loadList = async () => {
  try {
    const response = await knowledgeApi.getList({
      scope: currentScope.value,
      keyword: searchKeyword.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    knowledgeList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}

// 清空搜索
const handleClear = () => {
  searchKeyword.value = ''
  pagination.pageNum = 1
  loadList()
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadList()
}

// 查看知识库
const handleView = (id: number) => {
  router.push({ name: 'KnowledgeDetail', params: { id } })
}

// 新建
const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    name: '',
    introduction: '',
    description: '',
    visibility: 0,
    ownerType: currentScope.value === 'mine' ? 10 : currentScope.value === 'dept' ? 20 : 30,
    ownerId: 0,
    status: 0,
    coverUrl: '',
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (item: KnowledgeBase) => {
  isEdit.value = true
  Object.assign(formData, item)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (item: KnowledgeBase) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识库"${item.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await knowledgeApi.delete(item.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 命令处理
const handleCommand = (command: string, item: KnowledgeBase) => {
  if (command === 'edit') {
    handleEdit(item)
  } else if (command === 'delete') {
    handleDelete(item)
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
        await knowledgeApi.update(formData.id!, formData)
        ElMessage.success('更新成功')
      } else {
        await knowledgeApi.create(currentScope.value, formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadList()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 工具函数
const getVisibilityType = (visibility: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success'
  }
  return map[visibility] || 'info'
}

const getVisibilityLabel = (visibility: number) => {
  const map: Record<number, string> = {
    0: '私有',
    1: '公开'
  }
  return map[visibility] || '未知'
}

const getOwnerTypeLabel = (ownerType: number) => {
  const map: Record<number, string> = {
    10: '个人',
    20: '部门',
    30: '公共'
  }
  return map[ownerType] || '未知'
}

const getOwnerTypeType = (ownerType: number) => {
  const map: Record<number, any> = {
    10: 'primary',
    20: 'warning',
    30: 'success'
  }
  return map[ownerType] || 'info'
}

const getStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'success',
    1: 'warning',
    2: 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '就绪',
    1: '处理中',
    2: '已归档'
  }
  return map[status] || '未知'
}

// 生成卡片样式类
const getCardClass = (item: KnowledgeBase) => {
  if (item.coverUrl) {
    return 'with-cover'
  }
  // 根据知识库名称生成固定的样式类
  const index = item.name ? item.name.charCodeAt(0) % 5 : 0
  const classes = ['card-pink', 'card-purple', 'card-blue', 'card-green', 'card-orange']
  return classes[index]
}

// 生成封面样式
const getCoverStyle = (item: KnowledgeBase) => {
  if (item.coverUrl) {
    return {
      backgroundImage: `url(${item.coverUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }

  // 如果没有封面，使用随机渐变背景
  const gradients = [
    'var(--kb-gradient-pink)',
    'var(--kb-gradient-purple)',
    'var(--kb-gradient-blue)',
    'var(--kb-gradient-green)',
    'var(--kb-gradient-orange)'
  ]

  const index = item.name ? item.name.charCodeAt(0) % gradients.length : 0
  return {
    backgroundImage: gradients[index]
  }
}

// 封面上传相关
const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleCoverUpload = async (options: any) => {
  const { file } = options

  const formData = new FormData()
  formData.append('file', file)

  try {
    const uploadResponse = await fetch('http://localhost:8080/api/document/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: formData
    })
    const result = await uploadResponse.json()
    if (result.success && result.data) {
      formData.coverUrl = result.data.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败')
    }
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.knowledge-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #666;
}

.space-tabs {
  margin-left: 16px;
}

.space-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.space-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 搜索栏 */
.filter-search {
  border: 1px solid var(--border-color);
  padding: 6px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  width: 200px;
}

.filter-search input {
  border: none;
  outline: none;
  margin-left: 8px;
  font-size: 13px;
  width: 100%;
}

.filter-search .el-icon {
  color: #999;
}

.clear-btn {
  cursor: pointer;
  color: #999;
}

.clear-btn:hover {
  color: var(--text-sub);
}

/* 知识库列表 */
.knowledge-list {
  flex: 1;
  overflow-y: auto;
}

/* 卡片网格 */
.kb-grid {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

/* 知识库卡片 - 云文档风格 */
.kb-card {
  width: 240px;
  height: 320px;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
}

.kb-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-hover-shadow);
}

/* 封面区域 */
.card-cover {
  height: 260px;
  background-size: cover;
  background-position: center;
  position: relative;
  color: white;
}

/* 给图片加暗遮罩让文字看清楚 */
.kb-card.with-cover .card-cover::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px 12px 0 0;
}

.kb-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.kb-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
}

.kb-desc {
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.6;
}

/* 封面上的操作按钮 */
.cover-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
}

.kb-card:hover .cover-actions {
  opacity: 1;
}

.more-btn {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  padding: 6px;
  transition: all 0.2s;
}

.more-btn:hover {
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
}

/* 底部标签 */
.card-footer {
  padding: 12px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  border-top: 1px solid var(--border-color);
  background: #fff;
}

.card-footer :deep(.el-tag) {
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  line-height: 18px;
  border-radius: 4px;
}

/* 分页 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding-bottom: 20px;
}

/* 封面上传组件样式 */
.cover-uploader :deep(.el-upload) {
  width: 100%;
  height: 160px;
  border: 1px dashed var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: var(--primary-blue);
}

.cover-preview {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.cover-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-mask {
  opacity: 1;
}

.cover-mask span {
  margin-top: 8px;
  font-size: 14px;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.cover-placeholder .el-icon {
  color: #ccc;
  margin-bottom: 8px;
}

.cover-placeholder > span:not(.tip) {
  font-size: 14px;
}
</style>

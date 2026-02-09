<template>
  <div class="document-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>文档管理</h2>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文档"
          style="width: 200px"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Upload" @click="showUploadDialog = true">
          上传文档
        </el-button>
      </div>
    </div>

    <!-- 文档列表 -->
    <el-card>
      <el-table :data="documents" style="width: 100%" v-loading="loading">
        <el-table-column prop="fileName" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon><Document /></el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="kbName" label="所属知识库" width="150" />
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="charCount" label="字符数" width="100">
          <template #default="{ row }">
            {{ row.charCount?.toLocaleString() || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handlePreview(row)">
              预览
            </el-button>
            <el-button link type="primary" size="small" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button link type="primary" size="small" @click="handleRetry(row)">
              重试
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
          @size-change="loadDocuments"
          @current-change="loadDocuments"
        />
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="知识库">
          <el-select
            v-model="uploadForm.kbId"
            placeholder="请选择知识库"
            style="width: 100%"
          >
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、TXT、Markdown 等格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="文档预览" width="80%" fullscreen>
      <div class="preview-container" v-if="previewContent">
        <pre class="preview-content">{{ previewContent }}</pre>
      </div>
      <div v-else class="preview-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Upload,
  Document,
  UploadFilled,
  Loading
} from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api/knowledge'
import { documentApi } from '@/api/document'
import type { KnowledgeBase, Document as DocType } from '@/types'

const searchKeyword = ref('')
const documents = ref<DocType[]>([])
const knowledgeBases = ref<KnowledgeBase[]>([])
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const showUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const uploading = ref(false)
const uploadRef = ref()
const previewContent = ref('')
const selectedFile = ref<File | null>(null)

const uploadForm = reactive({
  kbId: undefined as number | undefined
})

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    // 获取所有知识库的文档
    const kbPromises = knowledgeBases.value.map(kb =>
      knowledgeApi.getDocuments(kb.id, {
        keyword: searchKeyword.value,
        pageNum: 1,
        pageSize: 1000
      })
    )
    const results = await Promise.all(kbPromises)
    const allDocs = results.flatMap(r => r.data!.records)

    // 客户端分页
    const start = (pagination.pageNum - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    documents.value = allDocs.slice(start, end)
    pagination.total = allDocs.length
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载知识库列表
const loadKnowledgeBases = async () => {
  try {
    const response = await knowledgeApi.getList({
      pageNum: 1,
      pageSize: 1000
    })
    knowledgeBases.value = response.records
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadDocuments()
}

// 文件选择
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 上传文件
const handleUpload = async () => {
  if (!uploadForm.kbId) {
    ElMessage.warning('请选择知识库')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    await documentApi.upload(uploadForm.kbId, selectedFile.value)
    ElMessage.success('上传成功')
    showUploadDialog.value = false
    selectedFile.value = null
    uploadForm.kbId = undefined
    uploadRef.value?.clearFiles()
    loadDocuments()
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

// 预览文档
const handlePreview = async (doc: DocType) => {
  showPreviewDialog.value = true
  previewContent.value = ''
  try {
    const response = await documentApi.getContent(doc.id)
    previewContent.value = response.data || '暂无内容'
  } catch (error) {
    console.error('预览失败:', error)
    previewContent.value = '加载失败'
  }
}

// 下载文档
const handleDownload = async (doc: DocType) => {
  try {
    await documentApi.download(doc.id)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
  }
}

// 重试解析
const handleRetry = async (doc: DocType) => {
  try {
    await documentApi.retryParse(doc.id)
    ElMessage.success('已提交重试任务')
    loadDocuments()
  } catch (error) {
    console.error('重试失败:', error)
  }
}

// 删除文档
const handleDelete = async (doc: DocType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${doc.fileName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await documentApi.delete(doc.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 工具函数
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const getStatusType = (status: number) => {
  const map: Record<number, any> = { 0: 'success', 1: 'warning', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '已完成', 1: '处理中', 2: '失败' }
  return map[status] || '未知'
}

onMounted(() => {
  loadKnowledgeBases().then(() => {
    loadDocuments()
  })
})
</script>

<style scoped>
.document-view {
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

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.preview-container {
  max-height: 70vh;
  overflow: auto;
}

.preview-content {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}

.preview-loading p {
  margin-top: 16px;
}
</style>

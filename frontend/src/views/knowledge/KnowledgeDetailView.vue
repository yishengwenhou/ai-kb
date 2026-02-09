<template>
  <div class="kb-detail-view">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="app-brand">
          <el-icon>
            <Menu />
          </el-icon>
          <span>AI 知识库</span>
        </div>

        <div class="kb-info">
          <div class="kb-icon">知</div>
          <span class="kb-name">{{ knowledgeInfo?.name || '加载中...' }}</span>
        </div>

        <div class="search-box">
          <el-icon>
            <Search />
          </el-icon>
          <input v-model="searchKeyword" type="text" placeholder="搜索文档" @keyup.enter="handleSearch" />
        </div>
      </div>

      <div class="tree-actions">
        <span>目录</span>
        <el-dropdown trigger="click"
          @command="(cmd) => handleCreateNode(cmd, { id: 0, fileName: '根目录', isFolder: 1 } as any)">
          <div class="action-btn">
            <el-icon>
              <Plus />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="doc"><el-icon style="color: #3370ff">
                  <Document />
                </el-icon>新建文档</el-dropdown-item>
              <el-dropdown-item command="folder"><el-icon style="color: #ffb800">
                  <Folder />
                </el-icon>新建文件夹</el-dropdown-item>
              <el-dropdown-item command="upload" divided><el-icon>
                  <Upload />
                </el-icon>上传及导入</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="nav-tree">
        <template v-for="item in documentTree" :key="item.id">
          <NavTreeItem :item="item" :selected-doc-id="selectedDocId" @doc-click="handleDocClick"
            @doc-command="handleDocCommand" @create-child-folder="handleCreateChildFolder"
            @create-node="handleCreateNode" />
        </template>
      </div>

      <div class="sidebar-footer">
        <div class="footer-icon" @click="showUploadDialog = true" title="上传文档">
          <el-icon>
            <Upload />
          </el-icon>
        </div>
        <div class="footer-icon" @click="handleShowSettings" title="知识库设置">
          <el-icon>
            <Setting />
          </el-icon>
        </div>
        <el-dropdown trigger="click" class="footer-icon" title="更多">
          <el-icon>
            <MoreFilled />
          </el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="info">知识库信息</el-dropdown-item>
              <el-dropdown-item command="share" divided>分享知识库</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="nav-left">
          <div class="breadcrumb">
            <span class="breadcrumb-item" @click="goHome">{{ knowledgeInfo?.name || '知识库' }}</span>
            <el-icon v-if="currentParentId !== 0">
              <ArrowRight />
            </el-icon>
            <span v-if="currentParentId !== 0" class="breadcrumb-item" @click="goBack">{{ currentPath }}</span>
          </div>
        </div>

        <div class="header-actions">
          <el-button type="primary" @click="handleShowShare">
            <el-icon>
              <Share />
            </el-icon>
            分享
          </el-button>
          <el-button circle @click="showUploadDialog = true">
            <el-icon>
              <Upload />
            </el-icon>
          </el-button>
        </div>
      </header>

      <!-- 文档内容区 -->
      <div class="doc-container">
        <!-- 首页 Banner -->
        <div v-if="!selectedDocId && knowledgeInfo" class="doc-banner">
          <div class="banner-content">
            <h1 class="kb-title">{{ knowledgeInfo.name }}</h1>
            <p class="kb-intro">{{ knowledgeInfo.introduction || '暂无简介' }}</p>
          </div>
        </div>

        <!-- 文档列表 -->
        <div v-if="!selectedDocId" class="doc-list">
          <div v-for="doc in currentDocuments" :key="doc.id" class="doc-item" @click="handleDocClick(doc)">
            <el-icon class="doc-icon">
              <Folder v-if="doc.isFolder" />
              <Document v-else />
            </el-icon>
            <div class="doc-info">
              <div class="doc-name">{{ doc.fileName }}</div>
              <div class="doc-meta">
                <span v-if="!doc.isFolder">{{ formatFileSize(doc.fileSize) }}</span>
                <span>{{ formatDate(doc.updateTime) }}</span>
              </div>
            </div>
            <el-tag v-if="!doc.isFolder" :type="getDocStatusType(doc.status)" size="small">
              {{ getDocStatusLabel(doc.status) }}
            </el-tag>
          </div>

          <div v-if="currentDocuments.length === 0" class="empty-state">
            <el-icon>
              <FolderOpened />
            </el-icon>
            <p>暂无文档，点击上传按钮添加文档</p>
          </div>
        </div>

        <!-- 文档详情/预览 -->
        <div v-else class="doc-detail">
          <div class="doc-content" v-loading="loading">
            <h1 class="doc-title">{{ currentDoc?.fileName }}</h1>
            <div class="doc-meta-line">
              <span>{{ formatDate(currentDoc?.updateTime) }}</span>
              <el-tag v-if="!currentDoc?.isFolder" :type="getDocStatusType(currentDoc?.status)" size="small">
                {{ getDocStatusLabel(currentDoc?.status) }}
              </el-tag>
            </div>
            <div v-if="currentDoc?.content" class="doc-body">
              {{ currentDoc.content }}
            </div>
            <div v-else class="doc-empty">
              <el-icon>
                <Document />
              </el-icon>
              <p>此文档内容正在处理中...</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 悬浮工具按钮 -->
      <div class="floating-tools">
        <div class="float-btn" @click="showUploadDialog = true" title="上传文档">
          <el-icon>
            <Upload />
          </el-icon>
        </div>
      </div>
    </main>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="handleFileChange" :on-exceed="handleExceed"
        drag>
        <el-icon class="el-icon--upload">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、TXT、Markdown 等格式
          </div>
        </template>
      </el-upload>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建文件夹对话框 -->
    <el-dialog v-model="showCreateFolderDialog" title="新建文件夹" width="400px">
      <el-form label-width="80px">
        <el-form-item label="文件夹名称">
          <el-input v-model="createFolderForm.title" placeholder="请输入文件夹名称" maxlength="50" show-word-limit
            @keyup.enter="confirmCreateFolder" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateFolderDialog = false">取消</el-button>
        <el-button type="primary" :loading="creatingFolder" @click="confirmCreateFolder">
          创建
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, defineComponent, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Menu,
  Search,
  Folder,
  Document,
  MoreFilled,
  Plus,
  Upload,
  Setting,
  Edit,
  Delete,
  Share,
  ArrowRight,
  FolderOpened,
  UploadFilled,
  FolderAdd
} from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api/knowledge'
import { documentApi } from '@/api/document'
import type { KnowledgeBase, Document as DocType } from '@/types'

const router = useRouter()
const route = useRoute()

const knowledgeId = Number(route.params.id)
const knowledgeInfo = ref<KnowledgeBase>()
const documents = ref<DocType[]>([])
const documentTree = ref<DocType[]>([])
const searchKeyword = ref('')
const selectedDocId = ref<number | null>(null)
const currentDoc = ref<DocType | null>(null)
const loading = ref(false)
const uploading = ref(false)

// 当前路径（面包屑显示）
const currentPath = ref('首页')
// 当前父文件夹ID（0 表示根目录）
const currentParentId = ref(0)
// 路径堆栈，用于面包屑导航
const pathStack = ref<{ id: number; name: string }[]>([])

// 返回上一级
const goBack = () => {
  if (pathStack.value.length > 0) {
    const prev = pathStack.value.pop()!
    if (prev.id === 0) {
      currentParentId.value = 0
      currentPath.value = '首页'
    } else {
      currentParentId.value = prev.id
      currentPath.value = prev.name
    }
    selectedDocId.value = null
    currentDoc.value = null
    loadDocuments(currentParentId.value)
  }
}

// 返回根目录
const goHome = () => {
  pathStack.value = []
  currentParentId.value = 0
  currentPath.value = '首页'
  selectedDocId.value = null
  currentDoc.value = null
  loadDocuments(0)
}

// 当前文件夹下的文档
const currentDocuments = computed(() => {
  return documents.value
})

// 对话框状态
const showUploadDialog = ref(false)
const showCreateFolderDialog = ref(false)
const uploadRef = ref()
const selectedFile = ref<File | null>(null)

// 新建文件夹表单
const createFolderForm = reactive({
  parentId: 0,
  title: ''
})
const creatingFolder = ref(false)

// 加载知识库信息
const loadKnowledgeInfo = async () => {
  try {
    const response = await knowledgeApi.getById(knowledgeId)
    knowledgeInfo.value = response.data
  } catch (error) {
    console.error('加载知识库信息失败:', error)
  }
}

// 加载文档列表
const loadDocuments = async (parentId: number = 0) => {
  loading.value = true
  try {
    const response = await documentApi.list(knowledgeId, parentId)
    // 处理后端返回的数据，将 type="folder" 转换为 isFolder=1
    documents.value = (response || []).map((doc: DocType) => ({
      ...doc,
      isFolder: doc.type === 'folder' ? 1 : 0,
      fileName: doc.title || doc.fileName // 优先使用 title，兼容旧接口
    }))
    documentTree.value = buildTree(documents.value)
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 构建文档树
const buildTree = (docs: DocType[]): DocType[] => {
  const map = new Map<number, DocType>()
  const roots: DocType[] = []

  docs.forEach(doc => {
    map.set(doc.id, { ...doc, children: [] })
  })

  docs.forEach(doc => {
    const item = map.get(doc.id)!
    if (doc.parentId && map.has(doc.parentId)) {
      const parent = map.get(doc.parentId)!
      if (!parent.children) parent.children = []
      parent.children.push(item)
    } else {
      roots.push(item)
    }
  })

  return roots
}

// 搜索
const handleSearch = () => {
  loadDocuments()
}

// 点击文档
const handleDocClick = async (doc: DocType) => {
  if (doc.isFolder) {
    // 文件夹处理 - 保存当前路径到堆栈，然后加载文件夹内容
    pathStack.value.push({ id: currentParentId.value, name: currentPath.value })
    currentParentId.value = doc.id
    currentPath.value = doc.fileName
    selectedDocId.value = null
    currentDoc.value = null
    await loadDocuments(doc.id)
  } else {
    // 文档处理 - 加载详情
    selectedDocId.value = doc.id
    currentPath.value = doc.title || doc.fileName
    currentDoc.value = doc

    // 加载文档详情
    loading.value = true
    try {
      const detail = await documentApi.getDetail(doc.id)
      currentDoc.value = {
        ...doc,
        ...detail,
        isFolder: detail.type === 'folder' ? 1 : 0,
        fileName: detail.title || doc.fileName // 优先使用 detail.title
      }
    } catch (error) {
      console.error('加载文档详情失败:', error)
    } finally {
      loading.value = false
    }
  }
}

// 文档操作
const handleDocCommand = async (command: string, doc: DocType) => {
  if (command === 'rename') {
    ElMessage.info('重命名功能开发中')
  } else if (command === 'delete') {
    ElMessage.info('删除功能开发中')
  } else if (command === 'addFolder') {
    // 在当前文件夹下新建子文件夹
    handleCreateChildFolder(doc)
  }
}

// 新建根文件夹
const handleCreateFolder = () => {
  createFolderForm.parentId = 0
  createFolderForm.title = ''
  showCreateFolderDialog.value = true
}

// 在指定文件夹下新建子文件夹
const handleCreateChildFolder = (parentFolder: DocType) => {
  createFolderForm.parentId = parentFolder.id
  createFolderForm.title = ''
  showCreateFolderDialog.value = true
}

// 确认新建文件夹
const confirmCreateFolder = async () => {
  if (!createFolderForm.title.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  creatingFolder.value = true
  try {
    await documentApi.createNode(knowledgeId, {
      parentId: createFolderForm.parentId,
      title: createFolderForm.title,
      type: 'folder'
    })
    ElMessage.success('文件夹创建成功')
    showCreateFolderDialog.value = false
    createFolderForm.title = ''
    // 刷新文档列表
    loadDocuments(currentParentId.value)
  } catch (error) {
    console.error('创建文件夹失败:', error)
    ElMessage.error('创建文件夹失败')
  } finally {
    creatingFolder.value = false
  }
}

// 上传文件
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    // 上传到当前所在的文件夹
    await documentApi.upload(knowledgeId, selectedFile.value, currentParentId.value)
    ElMessage.success('上传成功')
    showUploadDialog.value = false
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    loadDocuments(currentParentId.value)
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

// 处理新建节点的操作分发
const handleCreateNode = (type: string, parentNode: DocType) => {
  console.log(`在父节点 ${parentNode.fileName} 下创建 ${type}`);

  if (type === 'upload') {
    // 触发上传，并设置当前父ID
    currentParentId.value = parentNode.id; // 这里的 currentParentId 可能需要根据你的逻辑调整
    // 或者直接修改 handleUpload 逻辑让它接受 parentId
    handleUploadTrigger(parentNode.id);
  } else if (type === 'folder') {
    // 复用你现有的创建文件夹逻辑
    handleCreateChildFolder(parentNode);
  } else {
    // 新建文档/表格等
    handleCreateDoc(type, parentNode.id);
  }
}

// 新建文档的逻辑实现
const handleCreateDoc = async (type: string, parentId: number) => {
  try {
    // 1. 可以弹窗让用户输入名字，或者直接创建一个名为 "未命名文档" 的文件
    // 这里演示直接创建
    const defaultTitle = type === 'sheet' ? '未命名表格' : '未命名文档';

    await documentApi.createNode(knowledgeId, {
      parentId: parentId,
      title: defaultTitle,
      type: type // 传入 'doc' 或 'sheet'
    });

    ElMessage.success('创建成功');
    // 刷新列表并展开父节点
    await loadDocuments(parentId === 0 ? currentParentId.value : parentId);

  } catch (error) {
    console.error(error);
    ElMessage.error('创建失败');
  }
}

// 稍微改造一下上传逻辑，支持传入 parentId
const handleUploadTrigger = (parentId: number) => {
  // 保存临时的上传目标ID
  uploadTargetId.value = parentId;
  showUploadDialog.value = true;
}
// 需要定义一个 ref: const uploadTargetId = ref(0);
// 并在 handleUpload 方法中使用它代替 currentParentId.value

// 显示设置
const handleShowSettings = () => {
  ElMessage.info('设置功能开发中')
}

// 显示分享
const handleShowShare = () => {
  ElMessage.info('分享功能开发中')
}

// 工具函数
const formatFileSize = (bytes: number) => {
  if (!bytes) return '-'
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getDocStatusType = (status?: number) => {
  const map: Record<number, any> = { 0: 'success', 1: 'warning', 2: 'danger' }
  return map[status ?? 0] || 'info'
}

const getDocStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 0: '已完成', 1: '处理中', 2: '失败' }
  return map[status ?? 0] || '未知'
}

// 目录树节点组件（递归渲染）
// 目录树节点组件（递归渲染）
const NavTreeItem = defineComponent({
  name: 'NavTreeItem',
  props: {
    item: {
      type: Object as () => DocType,
      required: true
    },
    selectedDocId: {
      type: Number as () => number | null,
      default: null
    },
    level: {
      type: Number,
      default: 0
    },
    expanded: {
      type: Boolean,
      default: false
    }
  },
  emits: ['docClick', 'docCommand', 'createNode', 'toggleExpand'],
  setup(props, { emit }) {
    // 【核心修复】宽松的容器判断逻辑
    // 只要类型不是 'file' (附件)，都视为容器，显示 + 号
    // 这样即使旧数据没有 type 字段，也会默认显示 + 号
    const showAddButton = computed(() => true)

    // 展开箭头逻辑：文件夹始终显示，文档只有在有子节点时才显示
    const showExpandIcon = computed(() => {
      if (props.item.isFolder) return true
      return props.item.children && props.item.children.length > 0
    })

    const isExpanded = ref(props.expanded || props.level === 0)

    const handleClick = (e: MouseEvent) => {
      emit('docClick', props.item)
    }

    const handleExpandClick = (e: MouseEvent) => {
      e.stopPropagation()
      isExpanded.value = !isExpanded.value
      emit('toggleExpand', props.item.id, isExpanded.value)
    }

    const handleCommand = (cmd: string) => {
      emit('docCommand', cmd, props.item)
    }

    const handleAddCommand = (command: string) => {
      emit('createNode', command, props.item)
    }

    return () => h('div', { class: ['tree-node-wrapper'] }, [
      h('div', {
        class: ['nav-item', { active: props.selectedDocId === props.item.id }],
        style: { paddingLeft: `${props.level * 16 + 8}px` },
        onClick: handleClick
      }, [
        // ... 展开箭头 (保持不变) ...
        showExpandIcon.value ? h('el-icon', {
          class: ['expand-icon', { expanded: isExpanded.value }],
          onClick: handleExpandClick
        }, () => h(ArrowRight)) : h('span', { class: 'expand-placeholder' }),

        // ... 类型图标 (保持不变) ...
        h('el-icon', { class: ['type-icon'] }, () => props.item.isFolder ? h(Folder) : h(Document)),

        // ... 标题 (保持不变) ...
        h('span', { class: 'doc-name' }, props.item.fileName),

        // 2. 【修改】操作按钮区域
        // 这里是关键：无论什么类型，只要鼠标悬停，这个 div 就会显示
        h('div', { class: 'item-actions' }, [

          // --- [+] 加号按钮 ---
          h('el-dropdown', {
            trigger: 'click',
            placement: 'bottom-start', // 下拉菜单位置
            onCommand: handleAddCommand,
            onClick: (e: MouseEvent) => e.stopPropagation()
          }, {
            default: () => h('div', {
              class: 'action-btn add-btn', // 添加特定类名方便控制样式
              title: '新建'
            }, [h('el-icon', {}, () => h(Plus))]),
            dropdown: () => h('el-dropdown-menu', {}, [
              h('el-dropdown-item', { command: 'doc' }, [
                h('el-icon', { style: 'color: #3370ff' }, () => h(Document)), '新建文档'
              ]),
              h('el-dropdown-item', { command: 'folder' }, [
                h('el-icon', { style: 'color: #ffb800' }, () => h(Folder)), '新建文件夹'
              ]),
              // 根据需要决定是否允许在子节点上传
              h('el-dropdown-item', { command: 'upload', divided: true }, [
                h('el-icon', {}, () => h(Upload)), '上传文件'
              ])
            ])
          }),

          // --- [...] 更多按钮 ---
          h('el-dropdown', {
            trigger: 'click',
            placement: 'bottom-start',
            onCommand: handleCommand,
            onClick: (e: MouseEvent) => e.stopPropagation()
          }, {
            default: () => h('div', {
              class: 'action-btn more-btn',
              title: '更多'
            }, [h('el-icon', {}, () => h(MoreFilled))]),
            dropdown: () => h('el-dropdown-menu', {}, () => [
              h('el-dropdown-item', { command: 'rename' }, () => [h('el-icon', {}, () => h(Edit)), '重命名']),
              h('el-dropdown-item', { command: 'delete', divided: true }, () => [h('el-icon', {}, () => h(Delete)), '删除'])
            ])
          })
        ])
      ]),

      // 子节点递归渲染
      isExpanded.value && props.item.children && props.item.children.length > 0
        ? props.item.children.map((child: DocType) => h(NavTreeItem, {
          key: child.id,
          item: child,
          selectedDocId: props.selectedDocId,
          level: props.level + 1,
          expanded: false,
          onDocClick: (item: DocType) => emit('docClick', item),
          onDocCommand: (cmd: string, item: DocType) => emit('docCommand', cmd, item),
          onCreateNode: (type: string, parent: DocType) => emit('createNode', type, parent), // 务必透传此事件
          onToggleExpand: (id: number, expanded: boolean) => emit('toggleExpand', id, expanded)
        }))
        : null
    ])
  }
})

onMounted(() => {
  loadKnowledgeInfo()
  loadDocuments()
})
</script>

<style scoped>
.kb-detail-view {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #fff;
}

/* =============== 左侧侧边栏 =============== */
.sidebar {
  width: 260px;
  background-color: #f5f6f7;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #dee0e3;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
}

.app-brand {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
  margin-bottom: 12px;
  cursor: pointer;
}

.app-brand .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

/* 占位符，保证没有箭头的文档标题也能对齐 */
.expand-placeholder {
  width: 16px;
  height: 16px;
  margin-right: 2px;
  display: inline-block;
  flex-shrink: 0;
  /* 防止被挤压 */
}

/* 确保加号按钮有尺寸 */
.add-child-btn {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0;
  /* 默认隐藏 */
  transition: all 0.2s;
  color: #646a73;
}

/* 树节点行样式 */
.nav-item {
  display: flex;
  align-items: center;
  height: 36px; /* 稍微调高一点，方便点击 */
  padding-right: 8px; /* 右侧留点空隙 */
  margin-bottom: 2px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #1f2329;
  transition: background-color 0.1s;
  position: relative; /* 为绝对定位做准备（如果需要） */
}

.nav-item:hover {
  background-color: rgba(31, 35, 41, 0.08); /* 悬停背景色加深一点点 */
}

/* 激活状态 */
.nav-item.active {
  background-color: #e1efff; /* 飞书蓝背景 */
  color: #3370ff;
}

.add-child-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #3370ff;
}

/* 确保操作区域布局正确 */
.item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  /* 推到最右边 */
}

/* 调整操作区样式，确保按钮可见 */
.item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  /* 默认隐藏 */
  transition: opacity 0.2s;
}

.nav-item:hover .item-actions {
  opacity: 1;
}

.kb-info {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
}

.kb-icon {
  width: 20px;
  height: 20px;
  border: 1px solid #d0d3d6;
  color: #d83931;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  font-size: 12px;
}

.kb-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box input {
  width: 100%;
  height: 32px;
  background-color: rgba(31, 35, 41, 0.05);
  border: 1px solid transparent;
  border-radius: 6px;
  padding-left: 30px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.search-box input:focus {
  background-color: #fff;
  border-color: #3370ff;
  box-shadow: 0 0 0 2px rgba(51, 112, 255, 0.2);
}

.search-box .el-icon {
  position: absolute;
  left: 8px;
  color: #8f959e;
  font-size: 14px;
}

.tree-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  font-size: 12px;
  color: #646a73;
}

.action-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: #646a73; /* 图标默认灰色 */
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: rgba(0, 0, 0, 0.08); /* 按钮悬停背景 */
  color: #1f2329; /* 按钮悬停颜色变深 */
}

.nav-tree {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 32px;
  padding: 0 8px;
  margin-bottom: 2px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #1f2329;
  transition: background-color 0.1s;
}

.nav-item:hover {
  background-color: rgba(31, 35, 41, 0.05);
}

.nav-item.active .action-btn {
  color: #3370ff; /* 选中行时，按钮也是蓝色基调 */
}

.nav-item.active .action-btn:hover {
  background-color: rgba(51, 112, 255, 0.1);
}

.expand-icon {
  width: 16px;
  height: 16px;
  margin-right: 2px;
  font-size: 12px;
  transition: transform 0.2s;
  color: #8f959e;
  cursor: pointer;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.type-icon {
  margin-right: 6px;
  font-size: 16px;
  color: #646a73;
}

.nav-item.active .type-icon {
  color: #3370ff;
}

.doc-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 4px;
}

.doc-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

/* 悬停时显示操作按钮 */
.nav-item:hover .doc-actions,
.nav-item:hover .add-child-btn,
.nav-item:hover .item-actions {
  opacity: 1;
}

.doc-actions .el-icon,
.add-child-btn {
  font-size: 14px;
  color: #646a73;
}

.add-child-btn {
  opacity: 0;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s;
}

.add-child-btn:hover {
  background-color: rgba(51, 112, 255, 0.1);
  color: #3370ff;
}

.sidebar-footer {
  height: 48px;
  border-top: 1px solid #dee0e3;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 10px;
}

.footer-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: #646a73;
  cursor: pointer;
}

.footer-icon:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

/* =============== 右侧主内容区 =============== */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  position: relative;
  overflow: hidden;
}

/* 顶部导航栏 */
.top-nav {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid transparent;
}

.nav-left {
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  font-size: 13px;
  color: #646a73;
  display: flex;
  align-items: center;
}

.breadcrumb .el-icon {
  font-size: 12px;
  margin: 0 4px;
}

.breadcrumb-item {
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: #3370ff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 文档内容区 */
.doc-container {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 首页 Banner */
.doc-banner {
  width: 100%;
  height: 240px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.banner-content {
  text-align: center;
  color: white;
}

.kb-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
}

.kb-intro {
  font-size: 16px;
  opacity: 0.9;
  max-width: 600px;
  line-height: 1.6;
}

/* 文档列表 */
.doc-list {
  padding: 24px 48px;
}

.doc-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.doc-item:hover {
  background-color: #f5f6f7;
  border-color: #e1e4e8;
}

.doc-icon {
  margin-right: 12px;
  font-size: 24px;
  color: #646a73;
}

.doc-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.doc-info .doc-name {
  font-size: 14px;
  color: #1f2329;
}

.doc-meta {
  font-size: 12px;
  color: #8f959e;
  display: flex;
  gap: 12px;
}

/* 确保加号按钮能够响应点击 */
.add-child-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.add-child-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

/* 下拉菜单项的样式优化 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #8f959e;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 文档详情 */
.doc-detail {
  padding: 48px;
  overflow-y: auto;
}

.doc-content {
  max-width: 800px;
  margin: 0 auto;
}

.doc-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 24px;
  color: #1f2329;
}

.doc-meta-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #dee0e3;
  font-size: 13px;
  color: #8f959e;
}

.doc-body {
  font-size: 15px;
  line-height: 1.8;
  color: #646a73;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.doc-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #8f959e;
}

.doc-empty .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 占位符，保证没有箭头的文档标题也能对齐 */
.expand-placeholder {
  width: 16px;
  height: 16px;
  margin-right: 2px;
  display: inline-block;
}

/* 调整图标颜色，区分文档和文件夹 */
.type-icon {
  margin-right: 6px;
  font-size: 16px;
  color: #646a73;
}

/* 给加号按钮一个微弱的背景交互 */
.add-child-btn {
  opacity: 0;
  /* 默认隐藏，hover行时显示 */
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.nav-item:hover .add-child-btn {
  opacity: 1;
}

.add-child-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #3370ff;
}

/* 悬浮工具按钮 */
.floating-tools {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.float-btn {
  width: 36px;
  height: 36px;
  background: #fff;
  border: 1px solid #dee0e3;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  color: #646a73;
  transition: all 0.2s;
}

.float-btn:hover {
  color: #3370ff;
  border-color: #3370ff;
}

/* 滚动条样式 */
:deep(.nav-tree::-webkit-scrollbar),
:deep(.doc-container::-webkit-scrollbar),
:deep(.doc-detail::-webkit-scrollbar) {
  width: 6px;
}

:deep(.nav-tree::-webkit-scrollbar-thumb),
:deep(.doc-container::-webkit-scrollbar-thumb),
:deep(.doc-detail::-webkit-scrollbar-thumb) {
  background-color: #dee0e3;
  border-radius: 3px;
}

:deep(.nav-tree::-webkit-scrollbar-thumb:hover),
:deep(.doc-container::-webkit-scrollbar-thumb:hover),
:deep(.doc-detail::-webkit-scrollbar-thumb:hover) {
  background-color: #b4b8c1;
}
</style>

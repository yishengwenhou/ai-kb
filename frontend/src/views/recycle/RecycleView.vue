<template>
  <div class="recycle-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>回收站</h2>
        <p class="subtitle">已删除的项目保留30天后自动永久删除</p>
      </div>
      <div class="header-right">
        <el-select
          v-model="filterType"
          placeholder="类型筛选"
          style="width: 120px"
          @change="loadList"
        >
          <el-option label="全部" value="all" />
          <el-option label="文档" value="document" />
          <el-option label="知识库" value="knowledgeBase" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索"
          style="width: 200px"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="danger" :icon="Delete" @click="handleClear">
          清空回收站
        </el-button>
      </div>
    </div>

    <!-- 回收站列表 -->
    <el-card>
      <!-- 批量操作栏 -->
      <div v-if="selectedItems.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedItems.length }} 项</span>
        <el-button type="primary" :icon="RefreshLeft" @click="handleBatchRestore">
          批量恢复
        </el-button>
        <el-button type="danger" :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button @click="selectedItems = []">取消选择</el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="recycleList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'document' ? 'primary' : 'success'" size="small">
              {{ row.type === 'document' ? '文档' : '知识库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="200">
          <template #default="{ row }">
            <div class="item-name">
              <el-icon v-if="row.type === 'document'"><Document /></el-icon>
              <el-icon v-else><FolderOpened /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属" width="150">
          <template #default="{ row }">
            {{ row.type === 'document' ? row.kbName : row.deptName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deletedByName" label="删除人" width="100" />
        <el-table-column prop="deleteTime" label="删除时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              :icon="RefreshLeft"
              @click="handleRestore(row)"
            >
              恢复
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              永久删除
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
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Delete,
  RefreshLeft,
  Document,
  FolderOpened
} from '@element-plus/icons-vue'
import { recycleApi } from '@/api/recycle'
import type { RecycleBinItem } from '@/types'

const tableRef = ref()
const searchKeyword = ref('')
const filterType = ref('all')
const recycleList = ref<RecycleBinItem[]>([])
const selectedItems = ref<RecycleBinItem[]>([])
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载回收站列表
const loadList = async () => {
  loading.value = true
  try {
    const response = await recycleApi.getList({
      type: filterType.value,
      keyword: searchKeyword.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    recycleList.value = response.data!.records
    pagination.total = response.data!.total
  } catch (error) {
    console.error('加载回收站列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadList()
}

// 选择变化
const handleSelectionChange = (items: RecycleBinItem[]) => {
  selectedItems.value = items
}

// 恢复
const handleRestore = async (item: RecycleBinItem) => {
  try {
    if (item.type === 'document') {
      await recycleApi.restoreDocument(item.id)
    } else {
      await recycleApi.restoreKnowledgeBase(item.id)
    }
    ElMessage.success('恢复成功')
    loadList()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
    console.error('恢复失败:', error)
  }
}

// 批量恢复
const handleBatchRestore = async () => {
  try {
    // 按类型分组
    const docs = selectedItems.value.filter(i => i.type === 'document')
    const kbs = selectedItems.value.filter(i => i.type === 'knowledgeBase')

    let count = 0
    if (docs.length > 0) {
      count += await recycleApi.batchRestore(
        docs.map(i => i.id),
        'document'
      )
    }
    if (kbs.length > 0) {
      count += await recycleApi.batchRestore(
        kbs.map(i => i.id),
        'knowledgeBase'
      )
    }

    ElMessage.success(`成功恢复 ${count} 项`)
    selectedItems.value = []
    tableRef.value?.clearSelection()
    loadList()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
    console.error('批量恢复失败:', error)
  }
}

// 永久删除
const handleDelete = async (item: RecycleBinItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除"${item.name}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    if (item.type === 'document') {
      await recycleApi.permanentDeleteDocument(item.id)
    } else {
      await recycleApi.permanentDeleteKnowledgeBase(item.id)
    }
    ElMessage.success('已永久删除')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量永久删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除选中的 ${selectedItems.value.length} 项吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 按类型分组
    const docs = selectedItems.value.filter(i => i.type === 'document')
    const kbs = selectedItems.value.filter(i => i.type === 'knowledgeBase')

    let count = 0
    if (docs.length > 0) {
      count += await recycleApi.batchPermanentDelete(
        docs.map(i => i.id),
        'document'
      )
    }
    if (kbs.length > 0) {
      count += await recycleApi.batchPermanentDelete(
        kbs.map(i => i.id),
        'knowledgeBase'
      )
    }

    ElMessage.success(`已永久删除 ${count} 项`)
    selectedItems.value = []
    tableRef.value?.clearSelection()
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 清空回收站
const handleClear = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空回收站吗？此操作将永久删除所有项目，不可恢复！',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await recycleApi.clear()
    ElMessage.success('回收站已清空')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清空回收站失败:', error)
    }
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.recycle-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #999;
}

.header-right {
  display: flex;
  gap: 12px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.selected-count {
  font-weight: bold;
  color: #409eff;
}

.item-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>

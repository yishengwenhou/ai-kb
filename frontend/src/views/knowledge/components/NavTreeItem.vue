<template>
  <div class="tree-node-wrapper">
    <div 
      class="nav-item" 
      :class="{ active: selectedDocId === item.id }"
      :style="{ paddingLeft: `${level * 18 + 4}px` }" 
      @click="handleClick"
    >
      
      <span 
        class="expand-icon-wrapper" 
        @click.stop="handleExpandClick"
      >
        <el-icon 
          v-if="showExpandIcon" 
          class="expand-icon" 
          :class="{ expanded: isExpanded }"
        >
          <CaretRight /> 
        </el-icon>
      </span>

      <el-icon class="type-icon" :class="getIconColorClass(item)">
        <Folder v-if="item.isFolder" />
        <DocumentIcon v-else />
      </el-icon>

      <span class="doc-name" :title="item.fileName">{{ item.fileName }}</span>

      <div class="item-actions">
        <el-dropdown 
          v-if="showAddButton" 
          trigger="click" 
          placement="bottom-start" 
          @command="handleAddCommand"
          @click.stop
        >
          <div class="action-btn" title="新建">
            <el-icon><Plus /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="feishu-dropdown">
              <el-dropdown-item command="doc">
                <el-icon class="icon-blue"><DocumentIcon /></el-icon>新建文档
              </el-dropdown-item>
              <el-dropdown-item command="folder">
                <el-icon class="icon-yellow"><Folder /></el-icon>新建文件夹
              </el-dropdown-item>
              <el-dropdown-item command="upload" divided>
                <el-icon><Upload /></el-icon>上传文件
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown 
          trigger="click" 
          placement="bottom-start" 
          @command="handleCommand" 
          @click.stop
        >
          <div class="action-btn" title="更多">
            <el-icon><MoreFilled /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="feishu-dropdown">
              <el-dropdown-item command="rename"><el-icon><Edit /></el-icon>重命名</el-dropdown-item>
              <el-dropdown-item command="delete" class="text-red"><el-icon><Delete /></el-icon>删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <template v-if="isExpanded && item.children && item.children.length > 0">
      <NavTreeItem 
        v-for="child in item.children" 
        :key="child.id" 
        :item="child" 
        :selected-doc-id="selectedDocId"
        :level="level + 1" 
        :expanded="false" 
        @doc-click="(i) => emit('docClick', i)"
        @doc-command="(c, i) => emit('docCommand', c, i)" 
        @create-node="(t, p) => emit('createNode', t, p)"
        @toggle-expand="(id, e) => emit('toggleExpand', id, e)" 
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Folder,
  Document as DocumentIcon,
  CaretRight, // 换成 CaretRight 更像飞书
  Plus,
  Upload,
  MoreFilled,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import type { Document as DocType } from '@/types'

defineOptions({ name: 'NavTreeItem' })

const props = defineProps<{
  item: DocType
  selectedDocId: number | null
  level: number
  expanded: boolean
}>()

const emit = defineEmits<{
  (e: 'docClick', item: DocType): void
  (e: 'docCommand', cmd: string, item: DocType): void
  (e: 'createNode', type: string, parent: DocType): void
  (e: 'toggleExpand', id: number, expanded: boolean): void
}>()

// 只有文件夹显示加号
const showAddButton = computed(() => {
  const type = props.item.type
  return props.item.isFolder || type === 'folder' || type === 'doc' || type === 'sheet'
})

// 展开箭头逻辑：精准判断
const showExpandIcon = computed(() => {
  // 1. 如果前端已经加载了子节点数组，且长度大于0，必须显示
  if (props.item.children && props.item.children.length > 0) {
    return true
  }
  
  // 2. 如果还没加载子节点（懒加载状态），依赖后端返回的 hasChildren 标记
  if (props.item.hasChildren) {
    return true
  }
  
  // 3. 其他情况（包括空文件夹、普通文件）都不显示箭头
  return false
})

const isExpanded = computed(() => !!props.item.expanded)

// 获取图标颜色类
const getIconColorClass = (item: DocType) => {
  if (item.isFolder || item.type === 'folder') return 'icon-yellow' // 文件夹是黄色
  if (item.type === 'sheet') return 'icon-green'  // 表格是绿色
  return 'icon-blue' // 文档是蓝色
}

const handleClick = () => {
  emit('docClick', props.item)
}

const handleExpandClick = () => {
  emit('toggleExpand', props.item.id, !isExpanded.value)
}

const handleCommand = (cmd: string | number | object) => {
  emit('docCommand', cmd as string, props.item)
}

const handleAddCommand = (cmd: string | number | object) => {
  emit('createNode', cmd as string, props.item)
}
</script>

<style scoped>
/* ================== 核心行样式 ================== */
.nav-item {
  display: flex;
  align-items: center;
  height: 30px; /* 飞书的行高通常较小，更紧凑 */
  margin-bottom: 2px;
  border-radius: 6px; /* 圆角矩形背景 */
  cursor: pointer;
  font-size: 14px;
  color: #1f2329; /* 飞书标准黑 */
  transition: all 0.1s;
  padding-right: 8px;
  position: relative; /* 为绝对定位做准备 */
}

/* 悬停效果 */
.nav-item:hover {
  background-color: rgba(31, 35, 41, 0.05); /* 极浅的灰色背景 */
}

/* 选中状态 */
.nav-item.active {
  background-color: #E1EFFF; /* 飞书标准选中蓝背景 */
  color: #3370ff; /* 飞书标准蓝文字 */
}

/* ================== 图标区域 ================== */
/* 展开箭头容器：固定宽度保证对齐 */
.expand-icon-wrapper {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 2px;
  border-radius: 4px;
}
.expand-icon-wrapper:hover {
  background-color: rgba(0,0,0,0.05); /* 箭头单独悬停效果 */
}

/* 箭头本身 */
.expand-icon {
  font-size: 12px;
  color: #8f959e; /* 浅灰色箭头 */
  transition: transform 0.2s cubic-bezier(0.2, 0, 0, 1);
}
.expand-icon.expanded {
  transform: rotate(90deg);
}

/* 类型图标颜色 */
.type-icon {
  margin-right: 8px;
  font-size: 16px;
}
.icon-yellow { color: #FFB800; } /* 文件夹黄 */
.icon-blue { color: #3370FF; }   /* 文档蓝 */
.icon-green { color: #00B666; }  /* 表格绿 */

/* ================== 文字区域 ================== */
.doc-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 30px;
  user-select: none; /* 防止双击选中文字 */
}

/* ================== 操作按钮区域 (核心魔法) ================== */
.item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0; /* 默认隐藏 */
  transform: translateX(10px); /* 稍微位移增加动效 */
  transition: all 0.2s;
}

/* 只有当鼠标悬停在行上时，才显示按钮 */
.nav-item:hover .item-actions {
  opacity: 1;
  transform: translateX(0);
}
/* 选中状态下如果想常驻显示，可以加上这句 */
/* .nav-item.active .item-actions { opacity: 1; transform: translateX(0); } */

.action-btn {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: #646a73;
}
.action-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
  color: #1f2329;
}
.nav-item.active .action-btn {
  color: #3370ff; /* 选中行时按钮也变蓝 */
}
.nav-item.active .action-btn:hover {
  background-color: rgba(51, 112, 255, 0.15);
}

/* ================== 辅助样式 ================== */
/* 递归容器 */
.tree-node-wrapper {
  width: 100%;
}
</style>

<style>
/* 全局覆盖 Element Plus Dropdown 样式，使其更像飞书菜单 */
.feishu-dropdown .el-dropdown-menu__item {
  border-radius: 4px;
  margin: 2px 4px;
  padding: 8px 12px;
}
.feishu-dropdown .el-icon {
  margin-right: 8px;
}
.text-red {
  color: #F54A45;
}
.text-red:hover {
  background-color: #FEF0F0;
}
</style>
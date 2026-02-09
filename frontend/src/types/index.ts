// =============== 通用类型 ===============
export interface ApiResponse<T = any> {
  data?: T
  success: boolean
  message?: string
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// =============== 用户相关 ===============
export interface User {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  avatarUrl?: string
  avatarHash?: string
  status: number // 0-正常, 1-禁用
  deptId?: number
  deptName?: string
  gender?: number
  createTime?: string
  updateTime?: string
}

export interface Role {
  id: number
  roleName: string
  roleKey: string
  sort?: number
  status: number // 0-正常, 1-停用
  createTime?: string
  updateTime?: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  realName: string
}

export interface TokenInfo {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  username?: string
}

// =============== 部门相关 ===============
export interface Department {
  id: number
  parentId: number | null
  deptName: string
  sort: number
  leader?: number | { id: number; realName?: string; username?: string }
  leaderName?: string
  status: number // 0-正常, 1-停用
  createTime?: string
  updateTime?: string
  children?: Department[]
}

// 后端返回的 DepartmentVo（包含完整的 User 对象）
export interface DepartmentVo extends Omit<Department, 'leader'> {
  leader?: { id: number; realName?: string; username?: string }
}

export interface DepartmentForm {
  id?: number
  parentId: number | null
  deptName: string
  sort: number
  leader?: number
  status: number
}

// =============== 知识库相关 ===============
export interface KnowledgeBase {
  id: number
  name: string
  introduction?: string // 简介字段
  description?: string
  visibility: number // 0-私有, 1-公开
  ownerType: number // 10-个人, 20-部门, 30-公共
  ownerId: number
  status: number // 0-就绪, 1-处理中, 2-已归档
  vectorCollectionName?: string
  coverUrl?: string // 封面图片URL
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface KnowledgeBaseForm {
  id?: number
  name: string
  introduction?: string // 简介字段
  description?: string
  visibility: number
  ownerType?: number
  ownerId?: number
  status?: number
  coverUrl?: string // 封面图片URL
  remark?: string
}

// 空间类型
export type SpaceType = 'mine' | 'dept' | 'public'

// =============== 文档相关 ===============
export interface Document {
  id: number
  kbId: number
  kbName?: string
  // --- 核心 ID ---
  parentId: number

  // 【新增】路径，用于面包屑导航 (如: "0/1/5/")
  treePath?: string

  // --- 展示信息 ---
  fileName?: string // 兼容旧接口
  title?: string // 节点标题
  icon?: string
  description?: string

  // 【新增】文档封面图
  coverUrl?: string

  // --- 逻辑控制 ---
  type?: string // 'folder' | 'file' | 'doc' | 'sheet' | 'mind'
  sort?: number
  hasChildren?: boolean

  // 【新增】辅助字段，方便前端判断 (后端判断 type.equals("folder"))
  isFolder?: number // 0-文件, 1-目录（兼容旧接口）
  isFolderFlag?: boolean

  // --- 内容与统计 (详情页用) ---
  // 【新增】核心正文 (列表页为null，详情页有值)
  content?: string
  // 【新增】字数统计
  charCount?: number

  // --- 文件特有属性 (仅 type=file 时有值) ---
  fileUrl?: string
  fileSize?: number
  fileExt?: string
  fileType?: string // 兼容旧接口
  filePath?: string // 兼容旧接口
  fileHash?: string // 兼容旧接口

  // --- 状态与审计 ---
  status?: number // 处理状态
  createTime?: string
  updateTime?: string

  // --- 用户信息 ---
  createName?: string
  updateName?: string

  // 兼容：子节点（用于树形结构）
  children?: Document[]
}

export interface DocumentTreeNode extends Document {
  children?: DocumentTreeNode[]
}

// =============== 回收站相关 ===============
export interface RecycleBinItem {
  id: number
  type: 'document' | 'knowledgeBase'
  name: string
  kbId?: number
  kbName?: string
  deptId?: number
  deptName?: string
  deleteTime: string
  deletedBy?: number
  deletedByName?: string
  fileType?: string
  fileSize?: number
  description?: string
}

// =============== 路由菜单项 ===============
export interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  children?: MenuItem[]
}

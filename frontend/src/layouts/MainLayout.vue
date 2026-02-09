<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '260px'" class="sidebar">
      <!-- Logo -->
      <div class="logo">
        <el-icon v-if="isCollapse" :size="24"><Reading /></el-icon>
        <div v-else class="logo-text">
          <el-icon :size="20"><Reading /></el-icon>
          <span>企业AI知识库</span>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div v-if="!isCollapse" class="search-bar">
        <el-icon><Search /></el-icon>
        <input type="text" placeholder="搜索" />
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item
            v-if="!route.meta?.hideInMenu"
            :index="route.path"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>

      <!-- 分隔线 -->
      <div v-if="!isCollapse" class="nav-divider"></div>

      <!-- 快捷操作 -->
      <div v-if="!isCollapse" class="quick-actions">
        <div class="quick-action-item">
          <el-icon><Plus /></el-icon>
          <span>新建知识库</span>
        </div>
      </div>

      <!-- 用户信息区域 -->
      <div class="user-section">
        <el-dropdown v-if="!isCollapse" trigger="click" @command="handleUserCommand" class="user-info-expanded">
          <div class="dropdown-trigger">
            <el-avatar :size="32" :src="userStore.avatarUrl || undefined">
              <el-icon v-if="!userStore.avatarUrl"><UserFilled /></el-icon>
            </el-avatar>
            <div class="user-details">
              <div class="username">{{ userStore.userName }}</div>
              <div class="user-email">{{ userStore.email || userStore.userInfo?.username }}</div>
            </div>
            <el-icon class="dropdown-icon" :size="14"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>
                修改个人信息
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown v-else trigger="click" @command="handleUserCommand" class="user-info-collapsed">
          <el-avatar :size="28" :src="userStore.avatarUrl || undefined">
            <el-icon v-if="!userStore.avatarUrl"><UserFilled /></el-icon>
          </el-avatar>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ userStore.userName }}
              </el-dropdown-item>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>
                修改个人信息
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <!-- 主体内容 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="18"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="{ path: item.path }"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Reading,
  FolderOpened,
  OfficeBuilding,
  User,
  DeleteFilled,
  UserFilled,
  ArrowDown,
  Fold,
  Expand,
  SwitchButton,
  Search,
  Plus
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/knowledge/') && path !== '/knowledge') {
    return '/knowledge'
  }
  return path
})

// 菜单路由
const menuRoutes = computed(() => {
  const mainRoute = router.getRoutes().find(r => r.path === '/')
  return mainRoute?.children?.filter(r => !r.meta?.hideInMenu) || []
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({
    path: r.path,
    title: r.meta?.title as string
  }))
})

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 用户下拉菜单操作
const handleUserCommand = (command: string) => {
  if (command === 'profile') {
    router.push({ name: 'Personal' })
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.clearAuth()
      router.push({ name: 'Login' })
    }).catch(() => {})
  }
}
</script>

<style scoped>
/* =============== 布局容器 =============== */
.main-layout {
  height: 100vh;
}

/* =============== 侧边栏 =============== */
.sidebar {
  background-color: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
  border-right: 1px solid var(--border-color);
  flex-shrink: 0;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  margin-bottom: 16px;
  height: 40px;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
}

/* 搜索栏 */
.search-bar {
  background: var(--search-bg);
  border-radius: 6px;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  color: var(--text-sub);
  font-size: 14px;
}

.search-bar input {
  border: none;
  background: transparent;
  margin-left: 8px;
  outline: none;
  width: 100%;
  font-size: 14px;
}

/* 菜单 */
.menu {
  border-right: none;
  background-color: transparent;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  color: var(--text-main);
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 2px;
  height: auto;
  line-height: 1.5;
  font-size: 14px;
}

.menu :deep(.el-menu-item:hover) {
  background-color: var(--hover-bg) !important;
}

.menu :deep(.el-menu-item.is-active) {
  background-color: var(--active-bg) !important;
  color: var(--primary-blue) !important;
}

.menu :deep(.el-menu-item .el-icon) {
  color: var(--text-sub);
  margin-right: 10px;
  font-size: 18px;
}

.menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--primary-blue);
}

/* 折叠状态菜单 */
.menu :deep(.el-menu-item.is-collapse) {
  justify-content: center;
  padding: 8px;
}

.menu :deep(.el-menu-item.is-collapse .el-icon) {
  margin-right: 0;
}

/* 分隔线 */
.nav-divider {
  height: 1px;
  background-color: var(--border-color-light);
  margin: 10px 0;
}

/* 快捷操作 */
.quick-actions {
  padding: 8px 0;
}

.quick-action-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-main);
  font-size: 14px;
  transition: background-color 0.2s;
}

.quick-action-item:hover {
  background-color: var(--hover-bg);
}

.quick-action-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
  color: var(--text-sub);
}

/* =============== 用户信息区域 =============== */
.user-section {
  border-top: 1px solid var(--border-color-light);
  padding: 12px;
  background-color: transparent;
  flex-shrink: 0;
}

.user-info-expanded {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.user-details {
  flex: 1;
  overflow: hidden;
}

.username {
  font-size: 14px;
  color: var(--text-main);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  font-size: 12px;
  color: var(--text-sub);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  color: var(--text-sub);
  cursor: pointer;
  transition: color 0.3s;
}

.dropdown-icon:hover {
  color: var(--text-main);
}

.user-info-collapsed {
  display: flex;
  justify-content: center;
  cursor: pointer;
}

/* =============== 主内容区 =============== */
.main-container {
  display: flex;
  flex-direction: column;
  background-color: var(--main-bg);
}

/* 顶部导航 */
.header {
  display: flex;
  align-items: center;
  height: 56px;
  border-bottom: 1px solid var(--border-color);
  background-color: #fff;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  transition: color 0.3s;
  color: var(--text-sub);
}

.collapse-btn:hover {
  color: var(--primary-blue);
}

:deep(.el-breadcrumb__item) {
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  color: var(--text-sub);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-main);
}

/* 页面内容 */
.main-content {
  padding: 24px 32px;
  overflow-y: auto;
  background-color: var(--main-bg);
}

/* =============== 页面切换动画 =============== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

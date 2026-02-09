import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/knowledge',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/KnowledgeView.vue'),
        meta: { title: '知识库管理', icon: 'FolderOpened' }
      },
      {
        path: 'knowledge/:id',
        name: 'KnowledgeDetail',
        component: () => import('@/views/knowledge/KnowledgeDetailView.vue'),
        meta: { title: '知识库详情', hideInMenu: true }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/department/DepartmentView.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserView.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'personal',
        name: 'Personal',
        component: () => import('@/views/personal/PersonalCenter.vue'),
        meta: { title: '个人中心', hideInMenu: true }
      },
      {
        path: 'recycle',
        name: 'Recycle',
        component: () => import('@/views/recycle/RecycleView.vue'),
        meta: { title: '回收站', icon: 'DeleteFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 企业AI知识库` : '企业AI知识库'

  if (requiresAuth && !userStore.isLoggedIn) {
    // 需要认证但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.isLoggedIn) {
    // 已登录但访问登录页，跳转到首页
    next({ name: 'Knowledge' })
  } else if (requiresAuth && userStore.isLoggedIn && !userStore.userInfo) {
    // 已登录但没有用户信息，尝试获取用户信息
    try {
      await userStore.fetchUserInfo()
      next()
    } catch (error) {
      // 获取用户信息失败，跳转到登录页
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

export default router

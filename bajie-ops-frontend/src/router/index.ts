import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView/LoginView.vue'
import Main from '@/components/main/Main.vue'
import { useCachedLoginModelStore } from '@/stores/cachedLoginModel'
import { CacheTool } from '@/utils/cache-util'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      name: 'login',
      path: '/login',
      component: LoginView
    },
    {
      name: 'Main',
      path: '/',
      component: Main,
      redirect: { name: 'students' },
      children: [
        {
          name: 'students',
          path: 'students',
          component: () => import('@/views/StudentManagementView/StudentManagementView.vue'),
          meta: { title: '学生管理' }
        },
        {
          name: 'courses',
          path: 'courses',
          component: () => import('@/views/CourseManagementView/CourseManagementView.vue'),
          meta: { title: '课程管理' }
        },
        {
          name: 'classes',
          path: 'classes',
          component: () => import('@/views/ClassManagementView/ClassManagementView.vue'),
          meta: { title: '班级管理' }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = useCachedLoginModelStore().token || CacheTool.getLoginInfo()?.token
  if (!token && to.name !== 'login') return { name: 'login' }
  if (token && to.name === 'login') return { name: 'students' }
  return true
})

export default router

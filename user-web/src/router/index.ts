import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/resumes',
    children: [
      {
        path: 'resumes/upload',
        name: 'ResumeUpload',
        component: () => import('@/views/resumes/ResumeUpload.vue'),
        meta: { title: '上传简历' },
      },
      {
        path: 'resumes',
        name: 'ResumeList',
        component: () => import('@/views/resumes/ResumeList.vue'),
        meta: { title: '简历列表' },
      },
      {
        path: 'resumes/:id',
        name: 'ResumeDetail',
        component: () => import('@/views/resumes/ResumeDetail.vue'),
        meta: { title: '简历详情' },
      },
      {
        path: 'interview',
        name: 'InterviewEntry',
        component: () => import('@/views/interview/InterviewEntry.vue'),
        meta: { title: '开始面试' },
      },
      {
        path: 'interview/session/:id',
        name: 'InterviewSession',
        component: () => import('@/views/interview/InterviewSession.vue'),
        meta: { title: '答题中' },
      },
      {
        path: 'interview/history',
        name: 'InterviewHistory',
        component: () => import('@/views/interview/InterviewHistory.vue'),
        meta: { title: '面试历史' },
      },
      {
        path: 'interview/history/:id',
        name: 'InterviewDetail',
        component: () => import('@/views/interview/InterviewDetail.vue'),
        meta: { title: '面试详情' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.public) return true
  if (!getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router

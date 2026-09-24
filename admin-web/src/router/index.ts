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
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/candidates',
    children: [
      {
        path: 'candidates',
        name: 'CandidateList',
        component: () => import('@/views/candidates/CandidateList.vue'),
        meta: { title: '候选人列表' },
      },
      {
        path: 'candidates/create',
        name: 'CandidateCreate',
        component: () => import('@/views/candidates/CandidateForm.vue'),
        meta: { title: '创建候选人' },
      },
      {
        path: 'candidates/:id/edit',
        name: 'CandidateEdit',
        component: () => import('@/views/candidates/CandidateForm.vue'),
        meta: { title: '编辑候选人' },
      },
      {
        path: 'interview-records',
        name: 'InterviewRecords',
        component: () => import('@/views/records/InterviewRecordList.vue'),
        meta: { title: '面试记录' },
      },
      {
        path: 'interview-records/:id',
        name: 'InterviewRecordDetail',
        component: () => import('@/views/records/InterviewRecordDetail.vue'),
        meta: { title: '面试记录详情' },
      },
      {
        path: 'jobs',
        name: 'JobList',
        component: () => import('@/views/jobs/JobList.vue'),
        meta: { title: '职位列表' },
      },
      {
        path: 'jobs/create',
        name: 'JobCreate',
        component: () => import('@/views/jobs/JobForm.vue'),
        meta: { title: '创建职位' },
      },
      {
        path: 'jobs/:id/edit',
        name: 'JobEdit',
        component: () => import('@/views/jobs/JobForm.vue'),
        meta: { title: '编辑职位' },
      },
      {
        path: 'interviewers',
        name: 'InterviewerList',
        component: () => import('@/views/interviewers/InterviewerList.vue'),
        meta: { title: '面试官设置' },
      },
      {
        path: 'questions',
        name: 'QuestionList',
        component: () => import('@/views/questions/QuestionList.vue'),
        meta: { title: '面试题库' },
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

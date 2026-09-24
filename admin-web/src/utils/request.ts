import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearToken, getToken } from '@/utils/auth'
import type { Result } from '@/types'

/** 业务码：401 未授权；9002 Token 失效 */
const AUTH_EXPIRED_CODES = new Set([401, 9002])

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function redirectToLogin() {
  clearToken()
  if (router.currentRoute.value.path !== '/login') {
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

request.interceptors.response.use(
  (response) => {
    const payload = response.data as Result<unknown>
    if (payload && typeof payload.code === 'number' && payload.code !== 0 && payload.code !== 200) {
      ElMessage.error(payload.message || '请求失败')
      if (AUTH_EXPIRED_CODES.has(payload.code)) {
        redirectToLogin()
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data as Result<unknown> | undefined
    const msg = body?.message || error.message || '网络错误'
    if (status === 401 || (body && AUTH_EXPIRED_CODES.has(body.code))) {
      redirectToLogin()
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

export function unwrap<T>(data: Result<T> | T): T {
  if (data && typeof data === 'object' && 'code' in (data as object) && 'data' in (data as object)) {
    return (data as Result<T>).data
  }
  return data as T
}

export default request

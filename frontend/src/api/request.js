import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

// Axios 实例: 统一配置 baseURL / 超时 / 请求拦截(JWT) / 响应拦截(错误处理)
const service = axios.create({
  baseURL: '/api',         // 经 Vite 代理或 Nginx 转发到后端
  timeout: 15000
})

// 请求拦截: 注入 Authorization 头
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截: 统一处理业务码与 HTTP 错误
service.interceptors.response.use(
  response => {
    const res = response.data
    // 文件流等非标准响应直接返回
    if (response.config.responseType === 'blob') return response
    if (res.code === 0 || res.code === undefined) {
      return res
    }
    // 业务错误: 提示并拒绝
    ElMessage.error(res.message || '请求失败')
    // 未登录或登录过期
    if (res.code === 10002) {
      const userStore = useUserStore()
      userStore.logout()
      location.href = '/login'
    }
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error('登录已过期, 请重新登录')
      location.href = '/login'
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service

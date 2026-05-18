import axios from 'axios'
import { ElMessage } from 'element-plus'

const getTokenKey = (userType) => {
  const type = Number(userType)
  switch (type) {
    case 1: return 'student_token'
    case 2: return 'manager_token'
    case 3: return 'admin_token'
    case 4: return 'maintenance_token'
    default: return 'token'
  }
}

const getAuthToken = () => {
  const userType = sessionStorage.getItem('userType') || ''
  const tokenKey = getTokenKey(userType)
  return sessionStorage.getItem(tokenKey) || ''
}

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = getAuthToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200 || res.code === 0) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userType = sessionStorage.getItem('userType') || ''
        const tokenKey = getTokenKey(userType)
        const userInfoKey = {
          1: 'student_userInfo',
          2: 'manager_userInfo',
          3: 'admin_userInfo',
          4: 'maintenance_userInfo'
        }[Number(userType)] || 'userInfo'
        
        sessionStorage.removeItem(tokenKey)
        sessionStorage.removeItem(userInfoKey)
        sessionStorage.removeItem('userType')
        
        window.location.href = '/login'
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service

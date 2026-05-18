import { defineStore } from 'pinia'
import { ref } from 'vue'

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

const getUserInfoKey = (userType) => {
  const type = Number(userType)
  switch (type) {
    case 1: return 'student_userInfo'
    case 2: return 'manager_userInfo'
    case 3: return 'admin_userInfo'
    case 4: return 'maintenance_userInfo'
    default: return 'userInfo'
  }
}

export const useUserStore = defineStore('user', () => {
  const userType = ref(sessionStorage.getItem('userType') || '')
  
  const token = ref('')
  
  const userInfo = ref({})
  
  function init() {
    const storedUserType = sessionStorage.getItem('userType') || ''
    userType.value = storedUserType
    
    const tokenKey = getTokenKey(storedUserType)
    const userInfoKey = getUserInfoKey(storedUserType)
    token.value = sessionStorage.getItem(tokenKey) || ''
    userInfo.value = JSON.parse(sessionStorage.getItem(userInfoKey) || '{}')
  }
  
  init()

  function setToken(newToken, type) {
    const targetType = type || userType.value
    const tokenKey = getTokenKey(targetType)
    token.value = newToken
    sessionStorage.setItem(tokenKey, newToken)
    
    if (type) {
      userType.value = String(type)
      sessionStorage.setItem('userType', String(type))
    }
  }

  function setUserInfo(info) {
    const targetType = info.userType || userType.value
    
    userType.value = String(targetType)
    sessionStorage.setItem('userType', String(targetType))
    
    const userInfoKey = getUserInfoKey(targetType)
    userInfo.value = info
    sessionStorage.setItem(userInfoKey, JSON.stringify(info))
    
    const tokenKey = getTokenKey(targetType)
    token.value = sessionStorage.getItem(tokenKey) || ''
  }

  function logout() {
    const tokenKey = getTokenKey(userType.value)
    const userInfoKey = getUserInfoKey(userType.value)
    token.value = ''
    userInfo.value = {}
    userType.value = ''
    sessionStorage.removeItem(tokenKey)
    sessionStorage.removeItem(userInfoKey)
    sessionStorage.removeItem('userType')
  }

  return {
    token,
    userInfo,
    userType,
    setToken,
    setUserInfo,
    logout
  }
})

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '../api/auth'

// 用户状态: token / 用户信息 / 登录登出
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)

  async function login(loginForm) {
    const res = await authApi.login(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  async function adminLogin(loginForm) {
    const res = await authApi.adminLogin(loginForm)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('localCart')
  }

  return { token, userInfo, isLogin, isAdmin, login, adminLogin, logout }
})

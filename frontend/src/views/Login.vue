<template>
  <div class="page">
    <Header />
    <div class="login-box">
      <el-card>
        <h2 class="title">用户登录</h2>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password @keyup.enter="onLogin" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="onLogin">登 录</el-button>
        </el-form>
        <div class="links">
          <router-link to="/register">没有账号? 去注册</router-link>
          <router-link to="/admin/login">管理员入口</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLocalCartStore } from '../stores/cart'
import cartApi from '../api/cart'
import Header from '../components/Header.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const localCart = useLocalCartStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    // 登录后合并本地购物车到后端 Redis
    if (localCart.items.length > 0) {
      for (const item of localCart.items) {
        await cartApi.add({ productId: item.productId, quantity: item.quantity })
      }
      localCart.clear()
      ElMessage.success('本地购物车已同步')
    }
    router.push(route.query.redirect || '/home')
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.login-box { display: flex; justify-content: center; padding: 60px 20px; }
.title { text-align: center; margin-bottom: 24px; color: #303133; }
.links { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; }
.links a { color: #409eff; }
</style>

<template>
  <div class="page">
    <Header />
    <div class="container main-content">
      <h2 class="title">个人中心</h2>
      <el-card v-if="user">
        <div class="profile">
          <el-avatar :size="80" :src="user.avatar">{{ (user.nickname || user.username || '').charAt(0) }}</el-avatar>
          <div class="info">
            <p class="name">{{ user.nickname || user.username }}</p>
            <p class="muted">{{ user.username }} · {{ user.phone || '未绑定手机' }}</p>
            <p class="muted">{{ user.email || '未绑定邮箱' }}</p>
          </div>
        </div>
      </el-card>

      <div class="entry-grid">
        <el-card class="entry" @click="$router.push('/orders')">
          <el-icon :size="32"><Document /></el-icon>
          <span>我的订单</span>
        </el-card>
        <el-card class="entry" @click="$router.push('/address')">
          <el-icon :size="32"><Location /></el-icon>
          <span>收货地址</span>
        </el-card>
        <el-card class="entry" @click="$router.push('/cart')">
          <el-icon :size="32"><ShoppingBag /></el-icon>
          <span>购物车</span>
        </el-card>
        <el-card class="entry" @click="logout">
          <el-icon :size="32"><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Location, ShoppingBag, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import authApi from '../api/auth'
import Header from '../components/Header.vue'

const router = useRouter()
const userStore = useUserStore()
const user = ref(null)

async function loadUser() {
  const res = await authApi.info()
  user.value = res.data
}

function logout() {
  userStore.logout()
  router.push('/home')
}

onMounted(loadUser)
</script>

<style scoped>
.page { min-height: 100vh; }
.title { margin-bottom: 20px; }
.profile { display: flex; gap: 20px; align-items: center; }
.name { font-size: 18px; font-weight: 600; }
.muted { color: #909399; font-size: 13px; }
.entry-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 20px; }
.entry { text-align: center; cursor: pointer; transition: transform .2s; }
.entry:hover { transform: translateY(-2px); }
.entry span { display: block; margin-top: 8px; color: #606266; }
</style>

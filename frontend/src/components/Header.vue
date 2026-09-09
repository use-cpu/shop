<template>
  <header class="header">
    <div class="container header-inner">
      <div class="logo" @click="$router.push('/home')">
        <el-icon :size="24"><ShoppingCart /></el-icon>
        <span>购物商城</span>
      </div>

      <nav class="nav">
        <router-link to="/home">首页</router-link>
        <router-link to="/product/list">全部商品</router-link>
        <router-link to="/orders" v-if="userStore.isLogin">我的订单</router-link>
        <router-link to="/profile" v-if="userStore.isLogin">个人中心</router-link>
      </nav>

      <div class="actions">
        <router-link to="/cart" class="cart-btn" v-if="userStore.isLogin">
          <el-badge :value="cartCount" :hidden="cartCount === 0">
            <el-icon :size="22"><ShoppingBag /></el-icon>
          </el-badge>
        </router-link>
        <router-link to="/login" class="cart-btn" v-else>
          <el-badge :value="cartCount" :hidden="cartCount === 0">
            <el-icon :size="22"><ShoppingBag /></el-icon>
          </el-badge>
        </router-link>
        <template v-if="userStore.isLogin">
          <el-dropdown @command="handleCommand">
            <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="address">收货地址</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" link @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" link @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useLocalCartStore } from '../stores/cart'
import cartApi from '../api/cart'

const router = useRouter()
const userStore = useUserStore()
const localCart = useLocalCartStore()
const remoteCount = ref(0)

// 已登录显示 Redis 购物车数量; 未登录显示本地购物车数量
const cartCount = computed(() =>
  userStore.isLogin ? remoteCount.value : localCart.count
)

async function loadCartCount() {
  if (!userStore.isLogin) return
  try {
    const res = await cartApi.list()
    remoteCount.value = res.data?.totalCount || 0
  } catch (e) { /* 忽略 */ }
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/home')
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'orders') {
    router.push('/orders')
  } else if (cmd === 'address') {
    router.push('/address')
  }
}

onMounted(() => {
  loadCartCount()
})

defineExpose({ loadCartCount })
</script>

<style scoped>
.header { height: 60px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.06); position: sticky; top: 0; z-index: 100; }
.header-inner { display: flex; align-items: center; height: 100%; }
.logo { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: 700; color: #409eff; cursor: pointer; }
.nav { display: flex; gap: 24px; margin-left: 48px; }
.nav a { color: #606266; font-size: 14px; }
.nav a.router-link-active { color: #409eff; font-weight: 600; }
.actions { margin-left: auto; display: flex; align-items: center; gap: 16px; }
.cart-btn { display: flex; align-items: center; color: #606266; }
.user-name { cursor: pointer; color: #409eff; }
</style>

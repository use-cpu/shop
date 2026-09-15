<template>
  <header class="header">
    <div class="container header-inner">
      <div class="logo" @click="$router.push('/home')">
        <el-icon :size="24"><ShoppingCart /></el-icon>
        <span>购物商城</span>
      </div>

      <nav class="nav">
        <router-link to="/product/list">全部商品</router-link>
        <router-link to="/orders" v-if="userStore.isLogin">我的订单</router-link>
        <router-link to="/profile" v-if="userStore.isLogin">个人中心</router-link>
      </nav>

      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          :prefix-icon="Search"
          clearable
          @keyup.enter="onSearch"
        />
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </div>

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
      <button class="menu-toggle" @click="mobileOpen = !mobileOpen" aria-label="菜单">
        <el-icon :size="20"><Menu /></el-icon>
      </button>
    </div>
    <!-- 移动端下拉导航 -->
    <nav class="mobile-nav" v-show="mobileOpen" @click="mobileOpen = false">
      <router-link to="/product/list">全部商品</router-link>
      <router-link to="/orders" v-if="userStore.isLogin">我的订单</router-link>
      <router-link to="/profile" v-if="userStore.isLogin">个人中心</router-link>
      <router-link to="/login" v-if="!userStore.isLogin">登录 / 注册</router-link>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, ShoppingBag, Menu, Search } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useLocalCartStore } from '../stores/cart'
import cartApi from '../api/cart'

const router = useRouter()
const userStore = useUserStore()
const localCart = useLocalCartStore()
const remoteCount = ref(0)
const mobileOpen = ref(false)
const keyword = ref('')

function onSearch() {
  router.push({ path: '/product/list', query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {} })
}

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
    localCart.clear()
    remoteCount.value = 0
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
.header { height: 60px; background: rgba(255,255,255,.92); backdrop-filter: blur(10px); box-shadow: 0 2px 10px rgba(0,0,0,.06); position: sticky; top: 0; z-index: 100; }
.header-inner { display: flex; align-items: center; height: 100%; }
.logo { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: 700; color: #409eff; cursor: pointer; transition: opacity .15s; user-select: none; }
.logo:hover { opacity: .8; }

/* 桌面导航: 悬浮下划线动画 */
.nav { display: flex; gap: 28px; margin-left: 48px; }
.nav a {
  position: relative; color: #606266; font-size: 15px; padding: 6px 2px;
  transition: color .2s;
}
.nav a::after {
  content: ''; position: absolute; left: 0; bottom: -2px; width: 100%; height: 2px;
  background: #409eff; border-radius: 2px;
  transform: scaleX(0); transform-origin: center; transition: transform .22s ease;
}
.nav a:hover { color: #409eff; }
.nav a:hover::after, .nav a.router-link-active::after { transform: scaleX(1); }
.nav a.router-link-active { color: #409eff; font-weight: 600; }

.search-box {
  flex: 1; max-width: 420px; margin-left: 36px;
  display: flex; gap: 8px;
}
.search-box .el-input { flex: 1; }

.actions { margin-left: auto; display: flex; align-items: center; gap: 16px; }
.cart-btn { display: flex; align-items: center; color: #606266; transition: color .2s, transform .15s; }
.cart-btn:hover { color: #409eff; transform: scale(1.12); }
.user-name { cursor: pointer; color: #409eff; font-size: 14px; }

/* 汉堡按钮: 仅窄屏显示 */
.menu-toggle {
  display: none; margin-left: auto; background: none; border: none; cursor: pointer;
  color: #606266; padding: 6px; border-radius: 6px;
}
.menu-toggle:hover { background: #f0f2f5; color: #409eff; }

/* 移动端下拉导航 */
.mobile-nav {
  display: none; flex-direction: column; background: #fff;
  border-top: 1px solid #ebeef5; padding: 8px 20px 12px;
}
.mobile-nav a { padding: 12px 4px; color: #303133; font-size: 15px; border-bottom: 1px solid #f5f7fa; }
.mobile-nav a.router-link-exact-active { color: #409eff; font-weight: 600; }

@media (max-width: 768px) {
  .nav { display: none; }
  .search-box { display: none; }
  .actions { gap: 10px; margin-left: auto; }
  .logo span { font-size: 17px; }
  .menu-toggle { display: inline-flex; margin-left: 4px; }
  .actions .user-name { max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .mobile-nav { display: flex; }
}
</style>

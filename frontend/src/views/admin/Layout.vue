<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">商城管理后台</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon><span>概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Document /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <span>欢迎, {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
        <el-button link type="primary" @click="logout">退出</el-button>
      </el-header>
      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DataLine, Goods, Document, User } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; color: #fff; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 16px; font-weight: 600; background: #2b3a4d; }
.aside :deep(.el-menu) { background: transparent; border: none; }
.aside :deep(.el-menu-item) { color: #bfcbd9; }
.aside :deep(.el-menu-item.is-active) { color: #409eff; background: #1f2d3d; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.main { background: #f0f2f5; }
</style>

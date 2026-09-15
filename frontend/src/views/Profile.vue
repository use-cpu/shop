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
          <el-button type="primary" link @click="openEditDialog">编辑资料</el-button>
        </div>
      </el-card>

      <!-- 编辑资料弹窗 -->
      <el-dialog v-model="editVisible" title="编辑个人资料" width="420px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
        </template>
      </el-dialog>

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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Location, ShoppingBag, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import authApi from '../api/auth'
import Header from '../components/Header.vue'

const router = useRouter()
const userStore = useUserStore()
const user = ref(null)

// ---------- 编辑资料 ----------
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ nickname: '', phone: '', email: '' })

function openEditDialog() {
  // 用当前用户信息填充表单
  Object.assign(editForm, {
    nickname: user.value?.nickname || '',
    phone: user.value?.phone || '',
    email: user.value?.email || ''
  })
  editVisible.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    await authApi.updateProfile(editForm)
    ElMessage.success('资料更新成功')
    editVisible.value = false
    await loadUser()
  } finally {
    saving.value = false
  }
}

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
.profile .info { flex: 1; }
.name { font-size: 18px; font-weight: 600; }
.muted { color: #909399; font-size: 13px; }
.entry-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 20px; }
.entry { text-align: center; cursor: pointer; transition: transform .2s; }
.entry:hover { transform: translateY(-2px); }
.entry span { display: block; margin-top: 8px; color: #606266; }
</style>

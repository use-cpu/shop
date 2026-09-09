<template>
  <div class="page">
    <div class="login-box">
      <el-card>
        <h2 class="title">管理员登录</h2>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="管理员账号" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password @keyup.enter="onLogin" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="onLogin">登 录</el-button>
        </el-form>
        <div class="tip">默认账号: admin / 密码: admin123</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.adminLogin(form)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #2c3e50; display: flex; align-items: center; justify-content: center; }
.login-box { width: 400px; }
.title { text-align: center; margin-bottom: 24px; color: #303133; }
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 12px; }
</style>

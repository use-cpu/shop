<template>
  <div class="page">
    <Header />
    <div class="reg-box">
      <el-card>
        <h2 class="title">用户注册</h2>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="3-20位" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="6-32位" show-password />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="选填" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="选填" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="选填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="onRegister">注 册</el-button>
            <el-button @click="$router.push('/login')">返回登录</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '../api/auth'
import Header from '../components/Header.vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度6-32位', trigger: 'blur' }
  ]
}

async function onRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功, 请登录')
    router.push('/login')
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fa; }
.reg-box { display: flex; justify-content: center; padding: 40px 20px; }
.title { text-align: center; margin-bottom: 24px; color: #303133; }
</style>

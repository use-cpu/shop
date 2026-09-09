<template>
  <div class="page">
    <Header />
    <div class="container main-content">
      <div class="head">
        <h2>收货地址</h2>
        <el-button type="primary" @click="openDialog()">新增地址</el-button>
      </div>

      <el-card v-for="a in list" :key="a.id" class="addr-card">
        <div class="addr">
          <div class="main">
            <span class="name">{{ a.receiverName }}</span>
            <span class="phone">{{ a.receiverPhone }}</span>
            <el-tag v-if="a.isDefault" type="warning" size="small">默认</el-tag>
          </div>
          <p class="text">{{ formatAddr(a) }}</p>
          <div class="ops">
            <el-button v-if="a.isDefault !== 1" link @click="setDefault(a.id)">设为默认</el-button>
            <el-button link @click="openDialog(a)">编辑</el-button>
            <el-button link type="danger" @click="remove(a.id)">删除</el-button>
          </div>
        </div>
      </el-card>
      <el-empty v-if="!list.length" description="暂无收货地址" />

      <!-- 新增/编辑弹窗 -->
      <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="500px">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="form.receiverName" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model="form.receiverPhone" />
          </el-form-item>
          <el-form-item label="省">
            <el-input v-model="form.province" placeholder="如: 浙江省" />
          </el-form-item>
          <el-form-item label="市">
            <el-input v-model="form.city" placeholder="如: 杭州市" />
          </el-form-item>
          <el-form-item label="区/县">
            <el-input v-model="form.district" placeholder="如: 西湖区" />
          </el-form-item>
          <el-form-item label="详细地址" prop="detail">
            <el-input v-model="form.detail" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import addressApi from '../api/address'
import Header from '../components/Header.vue'

const list = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function loadData() {
  const res = await addressApi.list()
  list.value = res.data
}

function formatAddr(a) {
  return [a.province, a.city, a.district, a.detail].filter(Boolean).join('')
}

function openDialog(a) {
  if (a) {
    Object.assign(form, a)
  } else {
    Object.assign(form, { id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
  }
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) await addressApi.update(form)
    else await addressApi.add(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function setDefault(id) {
  await addressApi.setDefault(id)
  loadData()
}

async function remove(id) {
  await ElMessageBox.confirm('确定删除该地址吗?', '提示', { type: 'warning' })
  await addressApi.remove(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.addr-card { margin-bottom: 12px; }
.addr .main { display: flex; align-items: center; gap: 12px; }
.name { font-weight: 600; font-size: 16px; }
.phone { color: #606266; }
.text { color: #909399; margin: 8px 0; }
.ops { display: flex; gap: 8px; }
</style>

<template>
  <div class="page">
    <Header />
    <div class="container main-content" v-loading="loading">
      <h2 class="title">确认订单</h2>

      <!-- 收货地址 -->
      <el-card class="block">
        <template #header>收货地址</template>
        <el-radio-group v-model="addressId" v-if="addresses.length">
          <el-radio v-for="a in addresses" :key="a.id" :label="a.id" class="addr">
            <div class="addr-info">
              <span class="addr-name">{{ a.receiverName }}</span>
              <span class="addr-phone">{{ a.receiverPhone }}</span>
              <span class="addr-text">{{ formatAddr(a) }}</span>
              <el-tag v-if="a.isDefault" type="warning" size="small">默认</el-tag>
            </div>
          </el-radio>
        </el-radio-group>
        <el-empty v-else description="未配置收货地址" :image-size="60">
          <el-button type="primary" @click="$router.push('/address')">去添加地址</el-button>
        </el-empty>
      </el-card>

      <!-- 商品清单 -->
      <el-card class="block">
        <template #header>商品清单</template>
        <el-table :data="cart.items" v-if="cart.items?.length">
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="prod">
                <el-image :src="row.mainImage" class="prod-img" fit="cover" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="120" />
          <el-table-column label="数量" prop="quantity" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }"><span class="price">¥{{ row.subtotal }}</span></template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 提交 -->
      <div class="submit-bar">
        <div>
          <span>共 {{ cart.totalCount || 0 }} 件商品, 合计:</span>
          <span class="total-price">¥{{ cart.selectedAmount || 0 }}</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" :disabled="!addressId" @click="submit">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import addressApi from '../api/address'
import cartApi from '../api/cart'
import orderApi from '../api/order'
import Header from '../components/Header.vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const addresses = ref([])
const addressId = ref(null)
const cart = reactive({ items: [], totalCount: 0, selectedAmount: 0 })

async function loadData() {
  loading.value = true
  try {
    const [addrRes, cartRes] = await Promise.all([addressApi.list(), cartApi.list()])
    addresses.value = addrRes.data
    Object.assign(cart, cartRes.data)
    // 默认选中默认地址
    const def = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
    if (def) addressId.value = def.id
  } finally {
    loading.value = false
  }
}

function formatAddr(a) {
  return [a.province, a.city, a.district, a.detail].filter(Boolean).join('')
}

async function submit() {
  submitting.value = true
  try {
    const res = await orderApi.create({ addressId: addressId.value })
    ElMessage.success('订单创建成功')
    router.replace(`/order/${res.data.orderNo}`)
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.title { margin-bottom: 20px; }
.block { margin-bottom: 20px; }
.addr { display: block; margin-bottom: 12px; }
.addr-info { display: inline-flex; align-items: center; gap: 12px; }
.addr-name { font-weight: 600; }
.addr-phone { color: #606266; }
.addr-text { color: #909399; }
.prod { display: flex; align-items: center; gap: 8px; }
.prod-img { width: 50px; height: 50px; border-radius: 4px; }
.submit-bar { display: flex; justify-content: flex-end; align-items: center; gap: 20px; background: #fff; padding: 16px 20px; border-radius: 8px; }
.total-price { color: #f56c6c; font-size: 22px; font-weight: 700; margin-left: 8px; }
</style>

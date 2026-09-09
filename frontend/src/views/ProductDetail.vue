<template>
  <div class="page" v-loading="loading">
    <Header />
    <div class="container main-content" v-if="product">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/product/list' }">全部商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail">
        <div class="gallery">
          <el-image :src="currentImage" class="main-img" fit="contain" />
          <div class="thumbs" v-if="images.length > 1">
            <el-image v-for="(img, i) in images" :key="i" :src="img" class="thumb" fit="cover" @click="currentImage = img" />
          </div>
        </div>

        <div class="info">
          <h1 class="name">{{ product.name }}</h1>
          <p class="subtitle">{{ product.subtitle }}</p>
          <div class="price-card">
            <span class="label">价格</span>
            <span class="price">¥{{ product.price }}</span>
            <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
          </div>
          <div class="meta">
            <span>销量: {{ product.sales || 0 }}</span>
            <span>库存: {{ product.stock || 0 }}</span>
            <span>分类: {{ product.categoryName }}</span>
          </div>
          <div class="qty">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(1, product.stock || 1)" />
          </div>
          <div class="actions">
            <el-button type="warning" size="large" @click="buyNow" :disabled="product.status !== 1 || product.stock <= 0">
              立即购买
            </el-button>
            <el-button type="primary" size="large" :icon="ShoppingBag" @click="addToCart" :disabled="product.status !== 1 || product.stock <= 0">
              加入购物车
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />
      <h3 class="desc-title">商品详情</h3>
      <div class="desc" v-html="product.detail || '暂无详细描述'"></div>
    </div>

    <!-- 立即购买: 收货地址选择对话框 -->
    <el-dialog v-model="showBuyNowDialog" title="选择收货地址" width="520px">
      <el-radio-group v-model="selectedAddressId" class="addr-list">
        <el-radio
          v-for="a in addresses"
          :key="a.id"
          :label="a.id"
          class="addr-item"
        >
          <span class="addr-text">{{ formatAddress(a) }}</span>
          <el-tag v-if="a.isDefault === 1" size="small" type="success" class="addr-tag">默认</el-tag>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showBuyNowDialog = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="confirmBuyNow">立即购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ShoppingBag } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLocalCartStore } from '../stores/cart'
import productApi from '../api/product'
import cartApi from '../api/cart'
import orderApi from '../api/order'
import addressApi from '../api/address'
import Header from '../components/Header.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const localCart = useLocalCartStore()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const currentImage = ref('')
// 立即购买: 收货地址选择
const addresses = ref([])
const showBuyNowDialog = ref(false)
const selectedAddressId = ref(null)
const submitting = ref(false)

const images = computed(() => {
  const arr = []
  if (product.value?.mainImage) arr.push(product.value.mainImage)
  if (product.value?.images?.length) arr.push(...product.value.images)
  return arr
})

watch(product, (p) => {
  currentImage.value = p?.mainImage || ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  if (!userStore.isLogin) {
    // 未登录: 存入本地购物车, 登录后自动合并
    localCart.add(product.value.id, quantity.value)
    ElMessage.success('已加入购物车, 登录后将自动同步')
    return
  }
  await cartApi.add({ productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  // 立即购买: 先拉取收货地址, 让用户选择, 再调用 buy-now 接口创建订单
  let res
  try {
    res = await addressApi.list()
  } catch (e) {
    return
  }
  addresses.value = res.data || []
  if (addresses.value.length === 0) {
    // 无地址: 引导前往个人中心添加
    try {
      await ElMessageBox.confirm('您还未添加收货地址, 是否前往添加?', '提示', {
        confirmButtonText: '前往添加',
        cancelButtonText: '取消',
        type: 'warning'
      })
      router.push('/profile')
    } catch (e) { /* 用户取消 */ }
    return
  }
  // 默认选中默认地址, 否则第一个
  const def = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
  selectedAddressId.value = def.id
  showBuyNowDialog.value = true
}

async function confirmBuyNow() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const res = await orderApi.buyNow({
      productId: product.value.id,
      quantity: quantity.value,
      addressId: selectedAddressId.value
    })
    ElMessage.success('订单创建成功')
    showBuyNowDialog.value = false
    router.push(`/order/${res.data.orderNo}`)
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

function formatAddress(a) {
  return `${a.receiverName} ${a.receiverPhone} ${a.province}${a.city}${a.district}${a.detail}`
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.detail { display: flex; gap: 40px; margin-top: 20px; }
.gallery { width: 480px; }
.main-img { width: 480px; height: 480px; background: #fff; border-radius: 8px; }
.thumbs { display: flex; gap: 8px; margin-top: 12px; }
.thumb { width: 64px; height: 64px; border-radius: 4px; cursor: pointer; border: 1px solid #ebeef5; }
.info { flex: 1; }
.name { font-size: 24px; margin-bottom: 8px; }
.subtitle { color: #909399; margin-bottom: 20px; }
.price-card { background: #fff8f0; padding: 16px 20px; border-radius: 8px; margin-bottom: 20px; }
.price-card .label { color: #909399; margin-right: 12px; }
.price { color: #f56c6c; font-size: 28px; font-weight: 700; }
.price-original { color: #909399; text-decoration: line-through; margin-left: 8px; }
.meta { display: flex; gap: 24px; color: #909399; font-size: 14px; margin-bottom: 24px; }
.qty { margin-bottom: 24px; }
.qty .label { margin-right: 12px; color: #606266; }
.actions { display: flex; gap: 12px; }
.desc-title { margin: 20px 0; }
.desc { background: #fff; padding: 20px; border-radius: 8px; line-height: 1.8; min-height: 100px; }
.addr-list { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.addr-item { display: flex; align-items: center; width: 100%; height: auto; padding: 10px 12px; border: 1px solid #ebeef5; border-radius: 6px; margin-right: 0; }
.addr-item.is-checked { border-color: #f56c6c; background: #fff8f0; }
.addr-text { flex: 1; color: #303133; font-size: 14px; line-height: 1.6; word-break: break-all; }
.addr-tag { margin-left: 8px; }
</style>

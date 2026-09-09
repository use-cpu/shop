<template>
  <div class="page" v-loading="loading">
    <Header />
    <div class="container main-content" v-if="order">
      <el-page-header @back="$router.back()" content="订单详情" />

      <el-card class="block">
        <template #header>订单状态</template>
        <el-steps :active="stepActive" align-center>
          <el-step title="待支付" />
          <el-step title="已支付" />
          <el-step title="已发货" />
          <el-step title="已完成" />
        </el-steps>
        <div class="status-tip" v-if="order.status === 0">
          <el-alert type="warning" :closable="false" title="请尽快完成支付, 超过 30 分钟订单将自动关闭" show-icon />
        </div>
      </el-card>

      <el-card class="block">
        <template #header>收货信息</template>
        <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
        <p class="muted">{{ order.receiverAddress }}</p>
      </el-card>

      <el-card class="block">
        <template #header>商品清单</template>
        <el-table :data="order.orderItems">
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <div class="prod">
                <el-image :src="row.productImage" class="prod-img" fit="cover" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="120" />
          <el-table-column label="数量" prop="quantity" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="block">
        <template #header>订单信息</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ order.statusText }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ order.finishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实付金额"><span class="price">¥{{ order.totalAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="商品数量">{{ order.totalQuantity }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="ops">
        <el-button v-if="order.status === 0" type="primary" @click="pay">立即支付(模拟)</el-button>
        <el-button v-if="order.status === 0" @click="cancel">取消订单</el-button>
        <el-button v-if="order.status === 2" type="success" @click="confirm">确认收货</el-button>
        <el-button @click="rebuy">再次购买</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '../api/order'
import cartApi from '../api/cart'
import Header from '../components/Header.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)

const stepActive = computed(() => {
  const s = order.value?.status
  if (s === 4) return 0        // 已关闭, 停留在第一步
  return s === null ? 0 : s
})

async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.orderNo)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

async function pay() {
  await orderApi.pay(order.value.orderNo)
  ElMessage.success('支付成功(模拟)')
  loadData()
}

async function cancel() {
  await ElMessageBox.confirm('确定取消该订单吗?', '提示', { type: 'warning' })
  await orderApi.cancel(order.value.orderNo)
  ElMessage.success('已取消')
  loadData()
}

async function confirm() {
  await orderApi.confirm(order.value.orderNo)
  ElMessage.success('已确认收货')
  loadData()
}

// 再次购买: 将订单中的商品批量加入购物车
async function rebuy() {
  const items = order.value.orderItems || []
  for (const item of items) {
    await cartApi.add({ productId: item.productId, quantity: item.quantity })
  }
  ElMessage.success('已加入购物车')
  router.push('/cart')
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.block { margin-top: 20px; }
.muted { color: #909399; }
.prod { display: flex; align-items: center; gap: 8px; }
.prod-img { width: 50px; height: 50px; border-radius: 4px; }
.status-tip { margin-top: 16px; }
.ops { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>

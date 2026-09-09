<template>
  <div class="page">
    <Header />
    <div class="container main-content">
      <h2 class="title">我的购物车</h2>
      <div v-loading="loading">
        <el-table :data="cart.items" v-if="cart.items?.length" @selection-change="onSelectionChange" ref="tableRef" row-key="productId">
          <el-table-column type="selection" width="48" :reserve-selection="true" />
          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <div class="prod">
                <el-image :src="row.mainImage" class="prod-img" fit="cover" />
                <div>
                  <p class="prod-name">{{ row.productName }}</p>
                  <p class="prod-price">¥{{ row.price }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" @change="changeQty(row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.subtotal }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" link @click="remove(row.productId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else description="购物车还是空的" />
      </div>

      <div class="footer" v-if="cart.items?.length">
        <div class="left">
          <el-checkbox v-model="allChecked" @change="toggleAll">全选</el-checkbox>
          <span class="selected-count">已选 {{ selectedCount }} 件</span>
        </div>
        <div class="right">
          <span class="total-label">合计:</span>
          <span class="total-price">¥{{ cart.selectedAmount || 0 }}</span>
          <el-button type="primary" size="large" :disabled="selectedCount === 0" @click="checkout">去结算</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import cartApi from '../api/cart'
import Header from '../components/Header.vue'

const router = useRouter()
const loading = ref(false)
const tableRef = ref()
const cart = reactive({ items: [], totalCount: 0, selectedCount: 0, selectedAmount: 0 })
// 防止 selection-change 回调引发循环: 加载期间忽略回调
const silent = ref(false)

const selectedCount = computed(() => {
  return (cart.items || []).filter(i => i.selected).reduce((s, i) => s + i.quantity, 0)
})

const allChecked = computed({
  get: () => cart.items?.length > 0 && cart.items.every(i => i.selected),
  set: () => {}
})

async function loadData() {
  loading.value = true
  silent.value = true
  try {
    const res = await cartApi.list()
    Object.assign(cart, res.data)
    // 等 DOM 渲染完成后再恢复复选框, 避免触发 change 回调
    await nextTick()
    cart.items?.forEach(i => {
      tableRef.value?.toggleRowSelection(i, !!i.selected)
    })
  } finally {
    loading.value = false
    silent.value = false
  }
}

// 用户点击复选框触发, 同步状态到后端
async function onSelectionChange(rows) {
  if (silent.value) return
  const selectedSet = new Set(rows.map(r => r.productId))
  // 找出状态变化的项, 仅同步差异
  const tasks = []
  for (const item of cart.items) {
    const newSel = selectedSet.has(item.productId)
    if (item.selected !== newSel) {
      item.selected = newSel
      tasks.push(cartApi.toggleSelect({ productId: item.productId, selected: newSel }))
    }
  }
  await Promise.all(tasks)
  // 重新聚合金额
  recalc()
}

function recalc() {
  // 本地重算选中金额, 避免重复请求 list
  const sel = cart.items.filter(i => i.selected)
  cart.selectedAmount = sel.reduce((s, i) => s + Number(i.subtotal), 0)
}

async function toggleAll(val) {
  silent.value = true
  try {
    await cartApi.toggleSelectAll(val)
    cart.items.forEach(i => { i.selected = val })
    cart.items.forEach(i => tableRef.value?.toggleRowSelection(i, val))
    recalc()
  } finally {
    silent.value = false
  }
}

async function changeQty(row) {
  await cartApi.updateQuantity({ productId: row.productId, quantity: row.quantity })
  // 本地重算小计与总额
  row.subtotal = (row.price * row.quantity).toFixed(2)
  recalc()
}

async function remove(productId) {
  await ElMessageBox.confirm('确定删除该商品吗?', '提示', { type: 'warning' })
  await cartApi.remove([productId])
  ElMessage.success('已删除')
  loadData()
}

function checkout() {
  router.push('/checkout')
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.title { margin-bottom: 20px; }
.prod { display: flex; gap: 12px; align-items: center; }
.prod-img { width: 60px; height: 60px; border-radius: 4px; }
.prod-name { font-size: 14px; }
.prod-price { color: #f56c6c; font-size: 13px; }
.footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: #fff; padding: 16px 20px; border-radius: 8px; }
.left { display: flex; align-items: center; gap: 16px; }
.selected-count { color: #606266; font-size: 14px; }
.right { display: flex; align-items: center; gap: 12px; }
.total-price { color: #f56c6c; font-size: 22px; font-weight: 700; margin-right: 12px; }
</style>

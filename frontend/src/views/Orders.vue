<template>
  <div class="page">
    <Header />
    <div class="container main-content">
      <h2 class="title">我的订单</h2>

      <el-tabs v-model="activeStatus" @tab-change="loadData">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="已支付" name="1" />
        <el-tab-pane label="已发货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已关闭" name="4" />
      </el-tabs>

      <div v-loading="loading">
        <el-card v-for="o in list" :key="o.orderNo" class="order-card">
          <div class="order-head">
            <span>订单号: {{ o.orderNo }}</span>
            <span>下单时间: {{ o.createTime }}</span>
            <el-tag :type="statusType(o.status)" size="small">{{ o.statusText }}</el-tag>
          </div>
          <el-table :data="o.orderItems" size="small">
            <el-table-column label="商品" min-width="240">
              <template #default="{ row }">
                <div class="prod">
                  <el-image :src="row.productImage" class="prod-img" fit="cover" />
                  <span>{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" prop="price" width="100" />
            <el-table-column label="数量" prop="quantity" width="80" />
          </el-table>
          <div class="order-foot">
            <span class="amount">实付: <b class="price">¥{{ o.totalAmount }}</b></span>
            <div class="ops">
              <el-button size="small" @click="$router.push(`/order/${o.orderNo}`)">详情</el-button>
              <el-button v-if="o.status === 0" type="primary" size="small" @click="pay(o.orderNo)">去支付</el-button>
              <el-button v-if="o.status === 0" size="small" @click="cancel(o.orderNo)">取消</el-button>
              <el-button v-if="o.status === 2" type="success" size="small" @click="confirm(o.orderNo)">确认收货</el-button>
              <el-button v-if="o.status === 3 || o.status === 4" type="danger" plain size="small" @click="remove(o.orderNo)">删除</el-button>
            </div>
          </div>
        </el-card>
        <el-empty v-if="!list.length" description="暂无订单" />
      </div>

      <div class="pager" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '../api/order'
import Header from '../components/Header.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const activeStatus = ref('')

function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.myOrders({
      status: activeStatus.value === '' ? null : activeStatus.value,
      pageNum: pageNum.value,
      pageSize
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function pay(orderNo) {
  await orderApi.pay(orderNo)
  ElMessage.success('支付成功(模拟)')
  loadData()
}

async function cancel(orderNo) {
  await ElMessageBox.confirm('确定取消该订单吗?', '提示', { type: 'warning' })
  await orderApi.cancel(orderNo)
  ElMessage.success('已取消')
  loadData()
}

async function confirm(orderNo) {
  await orderApi.confirm(orderNo)
  ElMessage.success('已确认收货')
  loadData()
}

async function remove(orderNo) {
  await ElMessageBox.confirm('删除后订单记录将不可恢复, 确定删除吗?', '提示', { type: 'warning' })
  await orderApi.delete(orderNo)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.title { margin-bottom: 20px; }
.order-card { margin-bottom: 16px; }
.order-head { display: flex; gap: 24px; align-items: center; margin-bottom: 12px; font-size: 14px; color: #606266; }
.order-head .el-tag { margin-left: auto; }
.prod { display: flex; align-items: center; gap: 8px; }
.prod-img { width: 40px; height: 40px; border-radius: 4px; }
.order-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.amount { color: #606266; }
.pager { margin-top: 24px; display: flex; justify-content: center; }
</style>

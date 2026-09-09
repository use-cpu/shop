<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width:160px" @change="search">
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已发货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已关闭" :value="4" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="订单号" prop="orderNo" width="200" />
      <el-table-column label="用户ID" prop="userId" width="90" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="数量" prop="totalQuantity" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收货人" prop="receiverName" width="100" />
      <el-table-column label="电话" prop="receiverPhone" width="130" />
      <el-table-column label="下单时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link @click="$router.push(`/order/${row.orderNo}`)">详情</el-button>
          <el-button v-if="row.status === 1" link type="primary" @click="ship(row.orderNo)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '../../api/order'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ status: null, pageNum: 1, pageSize: 10 })

function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.adminList(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  loadData()
}

async function ship(orderNo) {
  await ElMessageBox.confirm('确认发货该订单吗?', '提示', { type: 'warning' })
  await orderApi.ship(orderNo)
  ElMessage.success('已发货')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; }
.pager { margin-top: 16px; display: flex; justify-content: center; }
</style>

<template>
  <div>
    <h2>概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat">
          <p class="stat-label">商品总数</p>
          <p class="stat-value">{{ stats.productCount }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat">
          <p class="stat-label">订单总数</p>
          <p class="stat-value">{{ stats.orderCount }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat">
          <p class="stat-label">待支付订单</p>
          <p class="stat-value">{{ stats.pendingPay }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat">
          <p class="stat-label">用户总数</p>
          <p class="stat-value">{{ stats.userCount }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="tip-card">
      <template #header>系统说明</template>
      <ul class="tips">
        <li>订单状态机: 待支付 → 已支付 → 已发货 → 已完成; 30 分钟未支付自动关闭</li>
        <li>库存防超卖: Redis 原子扣减 + DB 乐观锁双重保障</li>
        <li>个性化推荐: 基于用户浏览/加购行为的偏好分类推荐</li>
        <li>购物车: Redis Hash 存储, 登录自动合并离线购物车</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import productApi from '../../api/product'
import orderApi from '../../api/order'
import adminApi from '../../api/admin'

const stats = reactive({ productCount: 0, orderCount: 0, pendingPay: 0, userCount: 0 })

async function loadStats() {
  // 商品: 查第一页拿总数
  const p = await productApi.adminList({ pageNum: 1, pageSize: 1 })
  stats.productCount = p.data.total
  // 订单: 全部
  const o = await orderApi.adminList({ pageNum: 1, pageSize: 1 })
  stats.orderCount = o.data.total
  // 待支付
  const op = await orderApi.adminList({ pageNum: 1, pageSize: 1, status: 0 })
  stats.pendingPay = op.data.total
  // 用户
  const u = await adminApi.userList({ pageNum: 1, pageSize: 1 })
  stats.userCount = u.data.total
}

onMounted(loadStats)
</script>

<style scoped>
.stat { text-align: center; }
.stat-label { color: #909399; font-size: 14px; }
.stat-value { font-size: 28px; font-weight: 700; color: #409eff; margin-top: 8px; }
.tip-card { margin-top: 20px; }
.tips li { margin-bottom: 8px; color: #606266; line-height: 1.6; }
</style>

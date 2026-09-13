<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <div class="cover-wrap">
      <el-image :src="product.mainImage" class="cover" fit="cover" lazy>
        <template #placeholder><div class="skeleton cover-ph"></div></template>
        <template #error><div class="img-fallback">暂无图片</div></template>
      </el-image>
      <div class="soldout" v-if="product.stock <= 0">已售罄</div>
    </div>
    <div class="info">
      <p class="name">{{ product.name }}</p>
      <p class="subtitle" v-if="product.subtitle">{{ product.subtitle }}</p>
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
      </div>
      <div class="meta">
        <span><el-icon><TrendCharts /></el-icon> 已售 {{ product.sales || 0 }}</span>
        <span :class="{ 'out': product.stock <= 0 }">库存 {{ product.stock || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ product: { type: Object, required: true } })
</script>

<style scoped>
.product-card {
  background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer;
  transition: box-shadow .25s ease, transform .25s ease;
  border: 1px solid rgba(0,0,0,.03);
}
.product-card:hover {
  box-shadow: 0 10px 24px rgba(0,0,0,.12);
  transform: translateY(-4px);
}
.cover-wrap { position: relative; width: 100%; aspect-ratio: 1; overflow: hidden; background: #f5f7fa; }
.cover { width: 100%; height: 100%; transition: transform .4s ease; }
.product-card:hover .cover { transform: scale(1.07); }
.cover-ph { width: 100%; height: 100%; border-radius: 0; }
.img-fallback { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #909399; font-size: 13px; }
.soldout {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,.45); color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 4px;
}
.info { padding: 12px 14px 14px; }
.name {
  font-size: 14px; font-weight: 600; color: #303133;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.subtitle { font-size: 12px; color: #909399; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { margin-top: 10px; display: flex; align-items: baseline; }
.price { font-size: 18px; }
.meta { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; margin-top: 10px; }
.meta span { display: inline-flex; align-items: center; gap: 3px; }
.meta .out { color: #f56c6c; }
</style>

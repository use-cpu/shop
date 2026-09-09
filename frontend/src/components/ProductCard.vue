<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <el-image :src="product.mainImage" class="cover" fit="cover" lazy>
      <template #error><div class="img-fallback">暂无图片</div></template>
    </el-image>
    <div class="info">
      <p class="name">{{ product.name }}</p>
      <p class="subtitle" v-if="product.subtitle">{{ product.subtitle }}</p>
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
      </div>
      <div class="meta">
        <span>销量 {{ product.sales || 0 }}</span>
        <span>库存 {{ product.stock || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ product: { type: Object, required: true } })
</script>

<style scoped>
.product-card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: box-shadow .2s, transform .2s; }
.product-card:hover { box-shadow: 0 6px 16px rgba(0,0,0,.12); transform: translateY(-2px); }
.cover { width: 100%; aspect-ratio: 1; }
.img-fallback { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; font-size: 13px; }
.info { padding: 12px; }
.name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.subtitle { font-size: 12px; color: #909399; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { margin-top: 8px; }
.meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-top: 8px; }
</style>

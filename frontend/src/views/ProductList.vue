<template>
  <div class="page">
    <Header />
    <div class="container main-content">
      <!-- 搜索与筛选 -->
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索商品名称/副标题" clearable style="width:300px" @keyup.enter="search" @clear="search">
          <template #append>
            <el-button :icon="Search" @click="search" />
          </template>
        </el-input>
        <el-select v-model="query.sort" placeholder="排序" style="width:140px" @change="search">
          <el-option label="默认" value="default" />
          <el-option label="新品优先" value="new_desc" />
          <el-option label="销量优先" value="sales_desc" />
          <el-option label="价格升序" value="price_asc" />
          <el-option label="价格降序" value="price_desc" />
        </el-select>
      </div>

      <!-- 分类筛选 -->
      <div class="cats" v-if="categories.length">
        <span class="cat-label">分类:</span>
        <el-tag :type="query.categoryId ? 'info' : 'primary'" effect="plain" class="cat-tag" @click="selectCategory(null)">全部</el-tag>
        <template v-for="c in categories" :key="c.id">
          <el-tag :type="query.categoryId === c.id ? 'primary' : 'info'" effect="plain" class="cat-tag" @click="selectCategory(c.id)">{{ c.name }}</el-tag>
          <el-tag v-for="sub in c.children" :key="sub.id" :type="query.categoryId === sub.id ? 'primary' : 'info'" effect="plain" class="cat-tag" @click="selectCategory(sub.id)">{{ sub.name }}</el-tag>
        </template>
      </div>

      <!-- 商品网格 -->
      <div v-loading="loading && list.length" element-loading-background="rgba(245,247,250,.6)">
        <!-- 首次加载骨架屏 -->
        <div class="grid" v-if="loading && !list.length">
          <div class="skeleton-card" v-for="n in 8" :key="n">
            <div class="skeleton sk-img"></div>
            <div class="skeleton sk-line"></div>
            <div class="skeleton sk-line short"></div>
          </div>
        </div>
        <template v-else>
          <div class="grid" v-if="list.length">
            <ProductCard v-for="p in list" :key="p.id" :product="p" />
          </div>
          <el-empty v-else description="暂无商品" />
        </template>
      </div>

      <div class="pager" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" v-model:current-page="query.pageNum" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import productApi from '../api/product'
import categoryApi from '../api/category'
import Header from '../components/Header.vue'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  sort: route.query.sort || 'default',
  pageNum: 1,
  pageSize: 12
})

// 从首页轮播等入口跳转时, 路由参数变化需同步筛选条件(组件被复用时不会重新挂载)
watch(() => route.query, (q) => {
  query.keyword = q.keyword || ''
  query.categoryId = q.categoryId ? Number(q.categoryId) : null
  query.sort = q.sort || 'default'
  query.pageNum = 1
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await productApi.list(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  const res = await categoryApi.tree()
  categories.value = res.data
}

function search() {
  query.pageNum = 1
  loadData()
}

function selectCategory(id) {
  query.categoryId = id
  search()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.page { min-height: 100vh; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.cats { background: #fff; padding: 16px; border-radius: 10px; margin-bottom: 20px; display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.cat-label { color: #909399; font-size: 14px; margin-right: 4px; }
.cat-tag { cursor: pointer; transition: transform .12s; }
.cat-tag:hover { transform: translateY(-1px); }
.pager { margin-top: 24px; display: flex; justify-content: center; }

/* 骨架卡片 */
.skeleton-card { background: #fff; border-radius: 10px; padding: 0 0 14px; overflow: hidden; }
.sk-img { width: 100%; aspect-ratio: 1; border-radius: 0; }
.sk-line { height: 14px; margin: 14px 14px 0; }
.sk-line.short { width: 50%; }

@media (max-width: 768px) {
  .toolbar :deep(.el-input) { width: 100% !important; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar :deep(.el-select) { width: 100% !important; }
}
</style>

<template>
  <div class="page">
    <Header ref="headerRef" />
    <!-- 顶部 Banner 轮播 -->
    <el-carousel height="360px" :interval="5000" arrow="always" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-slide" :style="{ background: b.bg }">
          <div class="container banner-inner">
            <div class="slogan">
              <h1>{{ b.title }}</h1>
              <p>{{ b.desc }}</p>
              <el-button type="primary" size="large" @click="$router.push(b.link)">立即查看</el-button>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="container main-content">
      <!-- 分类入口 -->
      <section v-if="data.categories?.length" class="section">
        <h2 class="section-title">商品分类</h2>
        <div class="cat-grid">
          <div v-for="c in data.categories" :key="c.id" class="cat-item" @click="goCategory(c.id)">
            <el-icon :size="28"><Goods /></el-icon>
            <span>{{ c.name }}</span>
          </div>
        </div>
      </section>

      <!-- 个性化推荐 -->
      <section v-if="data.personalized?.length" class="section">
        <h2 class="section-title">
          {{ userStore.isLogin ? '为你推荐' : '热门精选' }}
          <span class="sub" v-if="userStore.isLogin">基于你的浏览与加购行为</span>
        </h2>
        <div class="grid">
          <ProductCard v-for="p in data.personalized" :key="p.id" :product="p" />
        </div>
      </section>

      <!-- 热销榜 -->
      <section v-if="data.hot?.length" class="section">
        <h2 class="section-title">热销榜单</h2>
        <div class="grid">
          <ProductCard v-for="p in data.hot" :key="p.id" :product="p" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import recommendApi from '../api/recommend'
import Header from '../components/Header.vue'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const userStore = useUserStore()
const data = reactive({ personalized: [], hot: [], categories: [] })

// 首页 Banner 轮播配置
const banners = [
  { id: 1, title: '欢迎来到购物商城', desc: '个性化推荐 · 品质好物 · 极速配送', link: '/product/list', bg: 'linear-gradient(135deg, #409eff, #66b1ff)' },
  { id: 2, title: '热销榜单', desc: '精选高销量好物, 错过不再来', link: '/product/list?sort=sales_desc', bg: 'linear-gradient(135deg, #f56c6c, #f89898)' },
  { id: 3, title: '为你推荐', desc: '基于浏览与加购行为, 越用越懂你', link: '/product/list', bg: 'linear-gradient(135deg, #67c23a, #95d475)' }
]

async function loadData() {
  const res = await recommendApi.home()
  Object.assign(data, res.data)
}

function goCategory(id) {
  router.push({ path: '/product/list', query: { categoryId: id } })
}

onMounted(loadData)
</script>

<style scoped>
.page { min-height: 100vh; }
.banner { width: 100%; }
.banner-slide { width: 100%; height: 100%; color: #fff; }
.banner-inner { padding: 80px 0; }
.slogan h1 { font-size: 36px; margin-bottom: 12px; }
.slogan p { font-size: 16px; margin-bottom: 24px; opacity: .9; }
.section { margin-bottom: 40px; }
.section-title { font-size: 22px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
.section-title .sub { font-size: 13px; color: #909399; font-weight: normal; }
.cat-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 16px; }
.cat-item { background: #fff; border-radius: 8px; padding: 20px 0; text-align: center; cursor: pointer; transition: transform .2s; display: flex; flex-direction: column; align-items: center; gap: 8px; color: #606266; }
.cat-item:hover { transform: translateY(-2px); color: #409eff; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
</style>

<template>
  <div class="page">
    <Header ref="headerRef" />
    <!-- 顶部 Banner 轮播 -->
    <el-carousel height="420px" :interval="5000" arrow="always" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-slide" :style="{ backgroundImage: `url('${b.image}')` }">
          <div class="banner-mask" :class="'mask-' + b.theme"></div>
          <div class="container banner-inner">
            <div class="slogan">
              <span class="banner-tag">
                <i class="tag-dot"></i>{{ b.tag }}
              </span>
              <h1>{{ b.title }}</h1>
              <p class="banner-desc">{{ b.desc }}</p>
              <div class="banner-promo">{{ b.promo }}</div>
              <div class="banner-cta">
                <el-button class="cta-btn" size="large" round @click="$router.push(b.link)">
                  {{ b.cta }}
                  <el-icon class="cta-arrow"><ArrowRight /></el-icon>
                </el-button>
                <span class="banner-hint">
                  <el-icon><Promotion /></el-icon>正品保障 · 极速发货 · 七天无理由
                </span>
              </div>
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
import { Goods, ArrowRight, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import recommendApi from '../api/recommend'
import Header from '../components/Header.vue'
import ProductCard from '../components/ProductCard.vue'

const router = useRouter()
const userStore = useUserStore()
const data = reactive({ personalized: [], hot: [], categories: [] })

// 图片生成地址(电商营销背景图)
const img = (prompt) =>
  `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodeURIComponent(prompt)}&image_size=landscape_16_9`

// 首页 Banner 轮播配置
const banners = [
  {
    id: 1,
    theme: 'blue',
    tag: '数码焕新季',
    title: '新潮数码  抢先首发',
    desc: '手机 · 电脑 · 智能配件  全场直降',
    promo: '最高立减 ¥1000 · 12期免息',
    cta: '立即抢购',
    link: '/product/list?categoryId=1',
    image: img('Modern e-commerce tech sale banner, premium smartphones and slim laptop floating on deep blue gradient background with glowing neon light effects, futuristic gadgets, professional commercial product photography, high-end advertising, empty dark text space on left side')
  },
  {
    id: 2,
    theme: 'red',
    tag: '限时秒杀',
    title: '爆款好物  低价开抢',
    desc: '热销榜单 TOP 好物  手慢则无',
    promo: '低至 5 折 · 限时 24 小时',
    cta: '马上秒杀',
    link: '/product/list?sort=sales_desc',
    image: img('E-commerce mega shopping festival flash sale banner, vibrant red and orange gradient background, golden gift boxes shopping bags and discount coupons floating, festive celebration sparkles, dynamic commercial advertising, shopping carnival style, empty dark text space on left side')
  },
  {
    id: 3,
    theme: 'gold',
    tag: '品质生活',
    title: '居家服饰  焕新生活',
    desc: '家居 · 服饰 · 美食  精选上新',
    promo: '新人专享 满199减30',
    cta: '逛逛新品',
    link: '/product/list?sort=new_desc',
    image: img('Cozy lifestyle shopping banner, elegant modern living room home interior with warm golden lighting, fashion apparel and home decor products, soft beige cream and gold tones, premium retail advertisement, comfortable quality life, empty dark text space on left side')
  }
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
.banner-slide {
  width: 100%; height: 100%; color: #fff;
  background-size: cover; background-position: center; position: relative; overflow: hidden;
}
/* 左侧深色渐变遮罩, 保证文字可读 */
.banner-mask {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, rgba(0,0,0,.72) 0%, rgba(0,0,0,.45) 42%, rgba(0,0,0,.08) 100%);
}
.mask-red  { background: linear-gradient(90deg, rgba(120,10,10,.78) 0%, rgba(180,30,20,.42) 45%, rgba(0,0,0,.06) 100%); }
.mask-blue { background: linear-gradient(90deg, rgba(8,20,60,.80) 0%, rgba(20,60,140,.42) 45%, rgba(0,0,0,.06) 100%); }
.mask-gold { background: linear-gradient(90deg, rgba(60,38,8,.80) 0%, rgba(120,82,20,.42) 45%, rgba(0,0,0,.06) 100%); }
.banner-inner { position: relative; z-index: 2; height: 100%; display: flex; align-items: center; }
.slogan { max-width: 560px; }
.slogan > * { animation: bannerIn .7s ease both; }
.slogan .banner-tag { animation-delay: .05s; }
.slogan h1 { animation-delay: .15s; }
.slogan .banner-desc { animation-delay: .25s; }
.slogan .banner-promo { animation-delay: .35s; }
.slogan .banner-cta { animation-delay: .45s; }
@keyframes bannerIn {
  from { opacity: 0; transform: translateY(26px); }
  to   { opacity: 1; transform: translateY(0); }
}
.banner-tag {
  display: inline-flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,.16); backdrop-filter: blur(4px);
  border: 1px solid rgba(255,255,255,.35);
  padding: 6px 16px; border-radius: 999px;
  font-size: 14px; font-weight: 600; letter-spacing: 1px; margin-bottom: 18px;
}
.tag-dot { width: 8px; height: 8px; border-radius: 50%; background: #ffd040; box-shadow: 0 0 8px #ffd040; }
.slogan h1 { font-size: 46px; font-weight: 800; line-height: 1.2; margin-bottom: 14px; text-shadow: 0 2px 12px rgba(0,0,0,.35); }
.banner-desc { font-size: 18px; margin-bottom: 16px; opacity: .92; }
.banner-promo {
  display: inline-block; font-size: 22px; font-weight: 800; color: #ffe08a;
  text-shadow: 0 2px 10px rgba(0,0,0,.4); margin-bottom: 26px;
}
.banner-cta { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; }
.cta-btn {
  background: linear-gradient(135deg, #ff5b5b, #ff8a3d);
  border: none; color: #fff; font-size: 17px; font-weight: 700;
  padding: 14px 34px; box-shadow: 0 8px 22px rgba(255,90,60,.45);
  transition: transform .2s, box-shadow .2s;
}
.cta-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 28px rgba(255,90,60,.55); background: linear-gradient(135deg, #ff5b5b, #ff8a3d); color: #fff; }
.cta-arrow { margin-left: 4px; }
.banner-hint { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; opacity: .85; }
.section { margin-bottom: 40px; }
.section-title { font-size: 22px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
.section-title .sub { font-size: 13px; color: #909399; font-weight: normal; }
.cat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 16px; }
.cat-item { background: #fff; border-radius: 10px; padding: 20px 0; text-align: center; cursor: pointer; transition: transform .2s, box-shadow .2s, color .2s; display: flex; flex-direction: column; align-items: center; gap: 8px; color: #606266; }
.cat-item:hover { transform: translateY(-3px); color: #409eff; box-shadow: 0 8px 20px rgba(64,158,255,.12); }

/* 平板/手机: 压缩 banner 与字号 */
@media (max-width: 1024px) {
  .banner :deep(.el-carousel__container) { height: 320px !important; }
  .slogan h1 { font-size: 36px; }
}
@media (max-width: 768px) {
  .banner :deep(.el-carousel__container) { height: 240px !important; }
  .slogan { max-width: 100%; }
  .slogan h1 { font-size: 24px; }
  .banner-desc { font-size: 14px; margin-bottom: 10px; }
  .banner-promo { font-size: 16px; margin-bottom: 14px; }
  .banner-hint { display: none; }
  .cta-btn { padding: 10px 22px; font-size: 15px; }
  .section-title { font-size: 18px; }
}
</style>

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 路由表: 用户端 + 管理端
const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/product/list',
    name: 'ProductList',
    component: () => import('../views/ProductList.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue'),
    meta: { title: '购物车', requireAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('../views/Checkout.vue'),
    meta: { title: '确认订单', requireAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { title: '我的订单', requireAuth: true }
  },
  {
    path: '/order/:orderNo',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue'),
    meta: { title: '订单详情', requireAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requireAuth: true }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('../views/Address.vue'),
    meta: { title: '收货地址', requireAuth: true }
  },

  /* ============ 管理后台 ============ */
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '概览' } },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/ProductManage.vue'), meta: { title: '商品管理' } },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 全局前置守卫: 登录校验 + 管理员校验 + 标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 购物商城' : '购物商城'
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.requireAdmin && !userStore.isAdmin) {
    next({ path: '/admin/login' })
    return
  }
  next()
})

export default router

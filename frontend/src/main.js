import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles.css'

// 应用入口: 注册 Pinia / Vue Router / Element Plus / 图标
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
// 全局注册 Element Plus 图标
for (const [key, comp] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, comp)
}
app.mount('#app')

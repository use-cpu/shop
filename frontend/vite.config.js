import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// Vite 配置: 开发服务器代理 /api → 后端 8080, 生产构建输出到 dist
export default defineConfig({
  plugins: [
    vue(),
    // 自动导入 Vue 与 Vue Router 的 API(ref/ reactive 等)
    AutoImport({
      resolvers: [ElementPlusResolver()]
    }),
    // 自动注册 Element Plus 组件
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  server: {
    port: 5173,
    // 开发环境代理, 解决跨域; 生产环境由 Nginx 反向代理
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 上传的图片资源也代理到后端(后端 context-path 是 /api, 需 rewrite 补前缀)
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/api' + path
      }
    }
  },
  build: {
    outDir: 'dist',
    // 生产构建时压缩; 关闭 sourcemap 减小体积
    minify: 'esbuild',
    sourcemap: false,
    // 分包优化
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus', '@element-plus/icons-vue']
        }
      }
    }
  }
})

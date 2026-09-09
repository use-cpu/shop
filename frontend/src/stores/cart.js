import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

/**
 * 离线购物车状态(localStorage)
 * 未登录时商品存本地; 登录后由 Login 页调用 /cart/merge 合并到 Redis
 * key 结构: [{ productId, quantity, selected }]
 */
export const useLocalCartStore = defineStore('localCart', () => {
  const items = ref(JSON.parse(localStorage.getItem('localCart') || '[]'))

  const count = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))

  /** 同步到 localStorage */
  function persist() {
    localStorage.setItem('localCart', JSON.stringify(items.value))
  }

  /** 加入购物车(未登录) */
  function add(productId, quantity = 1) {
    const exist = items.value.find(i => i.productId === productId)
    if (exist) {
      exist.quantity += quantity
    } else {
      items.value.push({ productId, quantity, selected: true })
    }
    persist()
  }

  /** 清空(合并后调用) */
  function clear() {
    items.value = []
    localStorage.removeItem('localCart')
  }

  return { items, count, add, clear, persist }
})

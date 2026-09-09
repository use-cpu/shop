import request from './request'

// 购物车 API(Redis 存储)
export default {
  list: () => request.get('/cart'),
  add: (data) => request.post('/cart', data),
  updateQuantity: (data) => request.put('/cart/quantity', data),
  toggleSelect: (data) => request.put('/cart/select', data),
  toggleSelectAll: (selected) => request.put('/cart/select-all', null, { params: { selected } }),
  remove: (productIds) => request.delete('/cart', { data: productIds }),
  merge: (deviceToken) => request.post('/cart/merge', null, { params: { deviceToken } })
}

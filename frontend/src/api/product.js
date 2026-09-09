import request from './request'

// 商品 API
export default {
  list: (params) => request.get('/product/list', { params }),
  hot: () => request.get('/product/hot'),
  detail: (id) => request.get(`/product/detail/${id}`),
  // 管理后台
  adminList: (params) => request.get('/product/admin/list', { params }),
  add: (data) => request.post('/product', data),
  update: (data) => request.put('/product', data),
  toggleStatus: (id, status) => request.put(`/product/status/${id}/${status}`),
  updateStock: (data) => request.put('/product/stock', data),
  remove: (id) => request.delete(`/product/${id}`)
}

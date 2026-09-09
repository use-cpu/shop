import request from './request'

// 商品分类 API
export default {
  tree: () => request.get('/category/tree'),
  add: (data) => request.post('/category', data),
  update: (data) => request.put('/category', data),
  remove: (id) => request.delete(`/category/${id}`)
}

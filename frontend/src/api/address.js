import request from './request'

// 收货地址 API
export default {
  list: () => request.get('/address'),
  add: (data) => request.post('/address', data),
  update: (data) => request.put('/address', data),
  remove: (id) => request.delete(`/address/${id}`),
  setDefault: (id) => request.put(`/address/default/${id}`)
}

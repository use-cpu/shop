import request from './request'

// 订单 API
export default {
  create: (data) => request.post('/order', data),
  buyNow: (data) => request.post('/order/buy-now', data),
  pay: (orderNo) => request.post(`/order/pay/${orderNo}`),
  cancel: (orderNo) => request.post(`/order/cancel/${orderNo}`),
  confirm: (orderNo) => request.post(`/order/confirm/${orderNo}`),
  delete: (orderNo) => request.delete(`/order/${orderNo}`),
  detail: (orderNo) => request.get(`/order/${orderNo}`),
  myOrders: (params) => request.get('/order/list', { params }),
  // 管理后台
  adminList: (params) => request.get('/order/admin/list', { params }),
  ship: (orderNo) => request.post(`/order/admin/ship/${orderNo}`)
}

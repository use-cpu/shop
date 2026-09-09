import request from './request'

// 用户管理 API(管理后台)
export default {
  userList: (params) => request.get('/admin/user/list', { params }),
  updateStatus: (data) => request.put('/admin/user/status', data)
}

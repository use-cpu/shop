import request from './request'

// 鉴权 API
export default {
  register: (data) => request.post('/auth/register', data),
  login: (data) => request.post('/auth/login', data),
  adminLogin: (data) => request.post('/auth/admin/login', data),
  info: () => request.get('/auth/info'),
  updateProfile: (data) => request.put('/auth/profile', data)
}

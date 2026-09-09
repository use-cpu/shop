import request from './request'

// 个性化推荐 API
export default {
  home: () => request.get('/recommend')
}

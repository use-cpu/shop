import request from './request'

// 文件上传 API
export default {
  // 上传图片, 返回可访问 URL
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

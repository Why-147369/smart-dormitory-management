import request from '@/utils/request'

export function getCurrentAdmin() {
  return request({
    url: '/admin/me',
    method: 'get'
  })
}

export function updateProfile(data) {
  return request({
    url: '/admin/profile',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/admin/password',
    method: 'put',
    data
  })
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

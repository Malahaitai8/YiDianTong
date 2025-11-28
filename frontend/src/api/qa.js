import request from './request'

export const getFaqList = (params) => {
  return request({
    url: '/api/admin/qa/faqs',
    method: 'get',
    params
  })
}

export const createFaq = (data) => {
  return request({
    url: '/api/admin/qa/faqs',
    method: 'post',
    data
  })
}

export const updateFaq = (index, data) => {
  return request({
    url: `/api/admin/qa/faqs/${index}`,
    method: 'put',
    data
  })
}

export const deleteFaq = (index) => {
  return request({
    url: `/api/admin/qa/faqs/${index}`,
    method: 'delete'
  })
}

export const importFaqs = (data) => {
  return request({
    url: '/api/admin/qa/faqs/import',
    method: 'post',
    data
  })
}

export const exportFaqs = () => {
  return request({
    url: '/api/admin/qa/faqs/export',
    method: 'get'
  })
}

export const getFaqStats = () => {
  return request({
    url: '/api/admin/qa/stats',
    method: 'get'
  })
}

export const updateFaqStats = (stats) => {
  return request({
    url: '/api/admin/qa/stats',
    method: 'put',
    data: stats
  })
}


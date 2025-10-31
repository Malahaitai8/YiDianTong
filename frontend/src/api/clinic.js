import request from './request'

// 查询所有门诊
export const getClinicList = () => {
  return request({
    url: '/clinic/selectAll',
    method: 'get'
  })
}

// 根据ID查询门诊
export const getClinicById = (id) => {
  return request({
    url: `/clinic/selectById/${id}`,
    method: 'get'
  })
}

// 根据名称查询门诊
export const getClinicByName = (name) => {
  return request({
    url: `/clinic/selectByName/${name}`,
    method: 'get'
  })
}

// 根据科室ID查询门诊列表（前端过滤）
export const getClinicsByDepartmentId = async (departmentId) => {
  try {
    const response = await getClinicList()
    const allClinics = response.data || []
    // 前端过滤出指定科室的门诊
    const filteredClinics = allClinics.filter(clinic => clinic.departmentId === departmentId)
    return {
      ...response,
      data: filteredClinics
    }
  } catch (error) {
    throw error
  }
}

// 创建门诊
export const createClinic = (data) => {
  return request({
    url: '/clinic',
    method: 'post',
    data
  })
}

// 更新门诊信息
export const updateClinic = (id, data) => {
  return request({
    url: `/clinic/${id}`,
    method: 'put',
    data
  })
}

// 删除门诊
export const deleteClinic = (id) => {
  return request({
    url: `/clinic/${id}`,
    method: 'delete'
  })
}
import request from './request'

// 查询所有科室
export const getDepartmentList = () => {
  return request({
    url: '/department/selectAll',
    method: 'get'
  })
}

// 根据ID查询科室
export const getDepartmentById = (id) => {
  return request({
    url: `/department/selectById/${id}`,
    method: 'get'
  })
}

// 创建科室
export const createDepartment = (data) => {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

// 更新科室信息
export const updateDepartment = (id, data) => {
  return request({
    url: `/department/${id}`,
    method: 'put',
    data
  })
}

// 删除科室
export const deleteDepartment = (id) => {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}

// 批量创建科室
export const batchCreateDepartments = (departments) => {
  return request({
    url: '/department/batch',
    method: 'post',
    data: {
      departments
    }
  })
}
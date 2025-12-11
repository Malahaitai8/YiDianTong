import request from './request'

// 系统配置管理API

/**
 * 获取所有系统配置
 */
export const getAllConfigs = () => {
  return request({
    url: '/api/systemConfig',
    method: 'get'
  })
}

/**
 * 根据ID获取系统配置
 * @param {number} id - 配置ID
 */
export const getConfigById = (id) => {
  return request({
    url: `/api/systemConfig/${id}`,
    method: 'get'
  })
}

/**
 * 根据key获取系统配置
 * @param {string} key - 配置键
 */
export const getConfigByKey = (key) => {
  return request({
    url: `/api/systemConfig/key/${key}`,
    method: 'get'
  })
}

/**
 * 创建系统配置
 * @param {Object} data - 配置数据
 * @param {string} data.key - 配置键
 * @param {string} data.value - 配置值
 * @param {string} data.description - 配置描述
 */
export const createConfig = (data) => {
  return request({
    url: '/api/systemConfig',
    method: 'post',
    data
  })
}

/**
 * 更新系统配置值
 * @param {string} key - 配置键
 * @param {string} value - 新的配置值
 */
export const updateConfigValue = (key, value) => {
  return request({
    url: `/api/systemConfig/key/${key}`,
    method: 'put',
    data: { value }
  })
}

// 挂号费相关配置
export const FEE_CONFIG_KEYS = {
  NORMAL_FEE: 'FEE_NORMAL',      // 普通号挂号费
  EXPERT_FEE: 'FEE_EXPERT',      // 专家号挂号费
  VIP_FEE: 'FEE_VIP',            // 特需号挂号费
  STUDENT_DISCOUNT: 'STUDENT_REIMBURSEMENT_RATE',  // 学生报销比例
  TEACHER_DISCOUNT: 'TEACHER_REIMBURSEMENT_RATE'   // 教师报销比例
}

/**
 * 获取挂号费配置
 */
export const getFeeConfigs = async () => {
  const configs = {}
  for (const [name, key] of Object.entries(FEE_CONFIG_KEYS)) {
    try {
      const response = await getConfigByKey(key)
      configs[name] = response.data?.value || '0'
    } catch (error) {
      console.warn(`获取配置 ${key} 失败:`, error)
      configs[name] = '0'
    }
  }
  return configs
}

/**
 * 更新挂号费配置
 * @param {Object} feeConfigs - 费用配置对象
 */
export const updateFeeConfigs = async (feeConfigs) => {
  const promises = []
  for (const [name, value] of Object.entries(feeConfigs)) {
    const key = FEE_CONFIG_KEYS[name]
    if (key) {
      promises.push(updateConfigValue(key, value))
    }
  }
  return Promise.all(promises)
}

// 导出systemConfigApi对象，包含挂号费相关的便捷方法
export const systemConfigApi = {
  // 获取挂号费配置
  getFeeConfig: async () => {
    try {
      const configs = await getFeeConfigs()
      return {
        normalFee: parseFloat(configs.NORMAL_FEE || 0),
        expertFee: parseFloat(configs.EXPERT_FEE || 0),
        vipFee: parseFloat(configs.VIP_FEE || 0),
        studentReimbursement: Math.round(parseFloat(configs.STUDENT_DISCOUNT || 0) * 100),
        teacherReimbursement: Math.round(parseFloat(configs.TEACHER_DISCOUNT || 0) * 100)
      }
    } catch (error) {
      console.error('获取挂号费配置失败:', error)
      throw error
    }
  },

  // 更新挂号费配置
  updateFeeConfig: async (feeConfig) => {
    try {
      const configData = {
        NORMAL_FEE: feeConfig.normalFee.toString(),
        EXPERT_FEE: feeConfig.expertFee.toString(),
        VIP_FEE: feeConfig.vipFee.toString(),
        STUDENT_DISCOUNT: (feeConfig.studentReimbursement / 100).toString(),
        TEACHER_DISCOUNT: (feeConfig.teacherReimbursement / 100).toString()
      }
      await updateFeeConfigs(configData)
      return { success: true }
    } catch (error) {
      console.error('更新挂号费配置失败:', error)
      throw error
    }
  },

  // 其他系统配置方法
  getAllConfigs,
  getConfigById,
  getConfigByKey,
  createConfig,
  updateConfigValue
}
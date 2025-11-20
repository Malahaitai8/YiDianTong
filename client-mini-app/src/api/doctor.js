import request from '@/utils/request.js'

/**
 * 获取所有医生列表
 * @param {Object} params - 查询参数
 * @param {number} params.departmentId - 科室ID（可选）
 * @param {string} params.title - 职称（可选）
 * @returns {Promise} 返回医生列表
 */
export function getDoctorList(params = {}) {
    return request({
        url: '/doctor/selectAll',  // 修改为后端实际路径
        method: 'GET',
        params
    })
}

/**
 * 根据医生ID获取医生详情
 * @param {number} id - 医生ID
 * @returns {Promise} 返回医生详情
 */
export function getDoctorById(id) {
    return request({
        url: `/doctor/selectById/${id}`,  // 修改为后端实际路径
        method: 'GET'
    })
}

/**
 * 搜索医生
 * @param {string} keyword - 搜索关键词
 * @returns {Promise} 返回匹配的医生列表
 */
export function searchDoctors(keyword) {
    // 后端暂未实现搜索接口，先获取全部然后前端过滤
    return request({
        url: '/doctor/selectAll',
        method: 'GET'
    }).then(data => {
        // 前端过滤
        if (!keyword) return data;
        const lowerKeyword = keyword.toLowerCase();
        return data.filter(doctor => 
            (doctor.name && doctor.name.toLowerCase().includes(lowerKeyword)) ||
            (doctor.title && doctor.title.toLowerCase().includes(lowerKeyword)) ||
            (doctor.specialty && doctor.specialty.toLowerCase().includes(lowerKeyword))
        );
    })
}

/**
 * 获取医生的排班信息
 * @param {number} doctorId - 医生ID
 * @param {string} startDate - 开始日期 (YYYY-MM-DD)
 * @param {string} endDate - 结束日期 (YYYY-MM-DD)
 * @returns {Promise} 返回医生排班信息
 */
export function getDoctorSchedules(doctorId, startDate, endDate) {
    // 调用后端患者端接口：GET /doctor/{id}/schedules
    return request({
        url: `/doctor/${doctorId}/schedules`,
        method: 'GET',
        data: {
            startDate,
            endDate
        }
    }).then(data => {
        // 后端返回的数据结构是 { schedules: [], total: x }
        const rawList = (data && data.schedules) || [];

        if (!Array.isArray(rawList)) {
            console.error("Schedules data is not an array:", rawList);
            return [];
        }

        // 将后端返回的字段映射到前端需要的字段
        return rawList.map(schedule => {
            const availableSlots = schedule.availableSlots || 0;
            return {
                id: schedule.id,
                doctorId: doctorId, // 后端返回的列表中没有doctorId，直接使用传入的
                date: formatToYMD(schedule.scheduleDate), // scheduleDate -> date
                period: normalizePeriod(schedule.timeSlot), // timeSlot -> period
                startTime: getDefaultStartTime(schedule.timeSlot),
                endTime: getDefaultEndTime(schedule.timeSlot),
                status: getScheduleStatus({ availableSlots }),
                totalSlots: schedule.totalSlots || 0,
                availableSlots: availableSlots
            };
        });
    });
}

// 将任意可解析日期规范化为 YYYY-MM-DD
function formatToYMD(input) {
    if (!input) return '';
    try {
        // 支持后端返回 'YYYY-MM-DD' 或 'YYYY-MM-DDTHH:mm:ss' 或时间戳
        const d = new Date(input);
        if (isNaN(d.getTime())) {
            // 兜底：若是字符串，尝试仅取前10位
            if (typeof input === 'string' && input.length >= 10) {
                return input.slice(0, 10);
            }
            return '';
        }
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    } catch (e) {
        return '';
    }
}

// 规范化时段
function normalizePeriod(slot) {
    if (!slot) return 'morning';
    const s = String(slot).toUpperCase();
    if (s.includes('MORNING') || s.includes('AM') || s.includes('上午')) return 'morning';
    if (s.includes('AFTERNOON') || s.includes('PM') || s.includes('下午')) return 'afternoon';
    if (s.includes('EVENING') || s.includes('NIGHT') || s.includes('晚上')) return 'evening';
    return String(slot).toLowerCase();
}

// 辅助函数：根据时间段返回默认开始时间
function getDefaultStartTime(timeSlot) {
    if (timeSlot === 'MORNING') return '08:00';
    if (timeSlot === 'AFTERNOON') return '14:00';
    if (timeSlot === 'EVENING') return '18:00';
    return '08:00';
}

// 辅助函数：根据时间段返回默认结束时间
function getDefaultEndTime(timeSlot) {
    if (timeSlot === 'MORNING') return '12:00';
    if (timeSlot === 'AFTERNOON') return '18:00';
    if (timeSlot === 'EVENING') return '21:00';
    return '12:00';
}

// 辅助函数：根据号源计算排班状态
function getScheduleStatus(schedule) {
    if (!schedule.availableSlots || schedule.availableSlots <= 0) {
        return 'full'; // 约满
    }
    if (schedule.availableSlots > 0) {
        return 'available'; // 可约
    }
    return 'unavailable'; // 停诊
}


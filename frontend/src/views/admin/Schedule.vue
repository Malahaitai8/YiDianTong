<template>
  <div class="schedule-management">
    <!-- 顶部信息卡片：独占一行 -->
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">排班管理</h1>
        <p class="header-subtitle">医生和科室排班统计—管理平台</p>
      </div>
      </div>

    <!-- 统计信息卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today">
              <el-icon><Calendar /></el-icon>
    </div>
            <div class="stat-info">
              <div class="stat-number">{{ todaySchedules }}</div>
              <div class="stat-label">今日排班</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ weekSchedules }}</div>
              <div class="stat-label">本周排班</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 页面主体：左右分栏 -->
    <div class="content-grid">
    <!-- 左侧筛选与操作区 -->
    <div class="filter-sidebar">
      <el-card class="filter-card">
        <!-- 筛选内容区域 -->
        <div class="filter-content">
        <!-- 核心筛选器 -->
        <div class="filter-section">
          <div class="filter-section-header">
            <h4>核心筛选</h4>
            <el-button size="small" @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
          
          <!-- 科室选择器 -->
          <div class="filter-item">
            <label>科室</label>
            <el-select
              v-model="filters.departmentId"
              placeholder="全部科室"
              clearable
              @change="handleDepartmentChange"
              style="width: 100%"
            >
              <el-option label="全部科室" value="" />
              <el-option
                v-for="dept in departmentList"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </div>

          <!-- 医生选择器 -->
          <div class="filter-item">
            <label>医生</label>
            <el-select
              v-model="filters.doctorId"
              :placeholder="filteredDoctorList.length === 0 && filters.departmentId ? '该科室暂无医生' : '全部医生'"
              clearable
              filterable
              style="width: 100%"
              :disabled="false"
              @change="handleDoctorChange"
            >
              <el-option label="全部医生" value="" />
              <el-option
                v-for="doctor in filteredDoctorList"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              />
            </el-select>
          </div>

          <!-- 日期范围选择器 -->
          <div class="filter-item">
            <label>日期范围</label>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
              @change="handleDateRangeChange"
            />
          </div>

          <!-- 时间段选择器 -->
          <div class="filter-item">
            <label>时间段</label>
            <el-select
              v-model="filters.timeSlot"
              placeholder="全部时间段"
              clearable
              style="width: 100%"
              @change="handleTimeSlotChange"
            >
              <el-option label="全部时间段" value="" />
              <el-option label="上午" value="morning" />
              <el-option label="下午" value="afternoon" />
            </el-select>
          </div>

        </div>

        <!-- 视图切换器 -->
        <div class="filter-section">
          <h4>视图模式</h4>
          <el-radio-group v-model="currentView" @change="handleViewChange" class="view-toggle">
            <el-radio-button label="calendar">
              <el-icon><Calendar /></el-icon>
              日历视图
            </el-radio-button>
            <el-radio-button label="list">
              <el-icon><List /></el-icon>
              列表视图
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 批量操作区 -->
        <div class="filter-section">
          <h4>批量操作</h4>
          <div class="batch-actions">
            <el-button type="primary" @click="showCreateDialog" style="width: 100%">
              <el-icon><Plus /></el-icon>
              新增排班
            </el-button>
            <el-button 
              type="warning" 
              @click="exportSchedules" 
              style="width: 100%"
              :loading="exporting"
            >
              <el-icon><Download /></el-icon>
              导出排班表
            </el-button>
            <el-button 
              type="info" 
              @click="showSettingsDialog" 
              style="width: 100%"
            >
              <el-icon><Setting /></el-icon>
              号源设置管理
            </el-button>
          </div>
        </div>

            </div>

      </el-card>
    </div>

    <!-- 右侧主展示区 -->
    <div class="main-content">
      <!-- 日历视图 -->
      <div v-if="currentView === 'calendar'" class="calendar-view">
        <el-card>
          <template #header>
            <div class="calendar-header">
              <h3>排班日历</h3>
              <div class="calendar-controls">
                <el-radio-group v-model="calendarMode" @change="handleCalendarModeChange">
                  <el-radio-button label="week">周视图</el-radio-button>
                  <el-radio-button label="month">月视图</el-radio-button>
                </el-radio-group>
                <div class="date-navigation">
                  <el-button @click="navigateDate(-1)">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                  <span class="current-period">{{ currentPeriodText }}</span>
                  <el-button @click="navigateDate(1)">
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </template>
          <div class="calendar-legend">
            <div class="legend-group">
              <div class="legend-chip expert"><span class="chip-left"></span><span class="chip-text">专家</span></div>
              <div class="legend-chip vip"><span class="chip-left"></span><span class="chip-text">特需</span></div>
            </div>
            <div class="legend-group right">
              <div class="legend-item"><span class="legend-dot morning"></span><span class="legend-text">上午</span></div>
              <div class="legend-item"><span class="legend-dot afternoon"></span><span class="legend-text">下午</span></div>
            </div>
          </div>

          <!-- 周视图 -->
          <div v-if="calendarMode === 'week'" class="calendar-grid-view">
            <div class="calendar-header-row">
              <div v-for="day in ['一', '二', '三', '四', '五', '六', '日']" :key="day" class="day-header-cell">
                {{ day }}
              </div>
            </div>
            <div class="calendar-body-grid">
              <div 
                v-for="date in weekDates" 
                :key="date.dateStr" 
                class="calendar-day-cell"
                :class="{ 
                  'today': date.isToday,
                  'in-date-range': isDateInRange(date.dateStr)
                }"
                @click="handleDayCellClick(date)"
              >
                <div class="day-number">{{ date.day }}</div>
                <div class="day-schedules">
                  <template v-for="timeSlot in ['morning', 'afternoon']" :key="timeSlot">
                    <template v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot]?.length">
                      <!-- 上午排班 -->
                      <template v-if="timeSlot === 'morning'">
                        <div 
                          v-for="schedule in getGroupedSchedulesForDate(date.dateStr)[timeSlot].slice(0, 5)" 
                      :key="schedule.id"
                          class="schedule-badge"
                          :class="getScheduleBadgeClass(schedule)"
                      @click.stop="handleScheduleClick(schedule)"
                    >
                          <div class="badge-top">
                            <span class="doctor-name">{{ schedule.doctorName || '未知医生' }}</span>
                            <span v-if="schedule.slotType === 'expert'" class="badge-label badge-expert">专家</span>
                            <span v-else-if="schedule.slotType === 'vip'" class="badge-label badge-vip">特需</span>
                            <span v-else class="badge-label badge-normal">普通</span>
                          </div>
                          <div class="slots-actions">
                            <div class="slots-info-row">
                              <span class="slots-info">剩余：{{ schedule.availableSlots || 0 }}</span>
                              <span v-if="getWaitlistCount(schedule) > 0" class="waitlist-info">候补：{{ getWaitlistCount(schedule) }}</span>
                            </div>
                            <div class="slots-buttons-row">
                              <el-button class="add-slots-btn" type="warning" size="small" @click.stop="openAddSlotsDialog(schedule)">加号</el-button>
                              <el-button 
                                v-if="canPopWaitlist(schedule)"
                                class="pop-waitlist-btn" 
                                type="danger" 
                                plain
                                size="small" 
                                :loading="waitlistPopLoadingId === schedule.id"
                                @click.stop="handlePopWaitlistClick(schedule)"
                              >
                                弹出候补
                              </el-button>
                            </div>
                          </div>
                    </div>
                        <div 
                          v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot].length > 5"
                          class="schedule-badge badge-more"
                          @click.stop="openDrawer(date.dateStr, 'morning', getGroupedSchedulesForDate(date.dateStr)[timeSlot])"
                        >
                          +{{ getGroupedSchedulesForDate(date.dateStr)[timeSlot].length - 5 }}
                      </div>
                      </template>
                      <!-- 下午排班 -->
                      <template v-if="timeSlot === 'afternoon'">
                        <div 
                          v-for="schedule in getGroupedSchedulesForDate(date.dateStr)[timeSlot].slice(0, 5)" 
                      :key="schedule.id"
                          class="schedule-badge"
                          :class="getScheduleBadgeClass(schedule)"
                      @click.stop="handleScheduleClick(schedule)"
                    >
                          <div class="badge-top">
                            <span class="doctor-name">{{ schedule.doctorName || '未知医生' }}</span>
                            <span v-if="schedule.slotType === 'expert'" class="badge-label badge-expert">专家</span>
                            <span v-else-if="schedule.slotType === 'vip'" class="badge-label badge-vip">特需</span>
                            <span v-else class="badge-label badge-normal">普通</span>
                          </div>
                          <div class="slots-actions">
                            <div class="slots-info-row">
                              <span class="slots-info">剩余：{{ schedule.availableSlots || 0 }}</span>
                              <span v-if="getWaitlistCount(schedule) > 0" class="waitlist-info">候补：{{ getWaitlistCount(schedule) }}</span>
                            </div>
                            <div class="slots-buttons-row">
                              <el-button class="add-slots-btn" type="warning" size="small" @click.stop="openAddSlotsDialog(schedule)">加号</el-button>
                              <el-button 
                                v-if="canPopWaitlist(schedule)"
                                class="pop-waitlist-btn" 
                                type="danger" 
                                plain
                                size="small" 
                                :loading="waitlistPopLoadingId === schedule.id"
                                @click.stop="handlePopWaitlistClick(schedule)"
                              >
                                弹出候补
                              </el-button>
                            </div>
                          </div>
                    </div>
                        <div 
                          v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot].length > 5"
                          class="schedule-badge badge-more"
                          @click.stop="openDrawer(date.dateStr, 'afternoon', getGroupedSchedulesForDate(date.dateStr)[timeSlot])"
                        >
                          +{{ getGroupedSchedulesForDate(date.dateStr)[timeSlot].length - 5 }}
                        </div>
                      </template>
                    </template>
                  </template>
                  <div v-if="!getSchedulesForDate(date.dateStr).length" class="empty-schedule" @click.stop="handleDayCellClick(date)">
                    <div class="add-schedule-icon">
                        <el-icon><Plus /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 月视图 -->
          <div v-else class="calendar-grid-view">
            <div class="calendar-header-row">
              <div v-for="day in ['一', '二', '三', '四', '五', '六', '日']" :key="day" class="day-header-cell">
                  {{ day }}
                </div>
              </div>
            <div class="calendar-body-grid month-grid">
              <div 
                v-for="date in monthDates" 
                :key="date.dateStr" 
                class="calendar-day-cell"
                :class="{ 
                  'today': date.isToday, 
                  'other-month': !date.isCurrentMonth,
                  'in-date-range': isDateInRange(date.dateStr) && date.isCurrentMonth
                }"
                @click="handleDayCellClick(date)"
              >
                <div class="day-number" :class="{ 'other-month': !date.isCurrentMonth }">{{ date.day }}</div>
                <div class="day-schedules">
                  <template v-for="timeSlot in ['morning', 'afternoon']" :key="timeSlot">
                    <template v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot]?.length">
                      <!-- 上午排班 -->
                      <template v-if="timeSlot === 'morning'">
                        <div 
                          v-for="schedule in getGroupedSchedulesForDate(date.dateStr)[timeSlot].slice(0, 5)" 
                          :key="schedule.id"
                          class="schedule-badge"
                          :class="getScheduleBadgeClass(schedule)"
                          @click.stop="handleScheduleClick(schedule)"
                        >
                          <div class="badge-top">
                            <span class="doctor-name">{{ schedule.doctorName || '未知医生' }}</span>
                            <span v-if="schedule.slotType === 'expert'" class="badge-label badge-expert">专家</span>
                            <span v-else-if="schedule.slotType === 'vip'" class="badge-label badge-vip">特需</span>
                            <span v-else class="badge-label badge-normal">普通</span>
                          </div>
                          <div class="slots-actions">
                            <div class="slots-info-row">
                              <span class="slots-info">剩余：{{ schedule.availableSlots || 0 }}</span>
                              <span v-if="getWaitlistCount(schedule) > 0" class="waitlist-info">候补：{{ getWaitlistCount(schedule) }}</span>
                            </div>
                            <div class="slots-buttons-row">
                              <el-button class="add-slots-btn" type="warning" size="small" @click.stop="openAddSlotsDialog(schedule)">加号</el-button>
                              <el-button 
                                v-if="canPopWaitlist(schedule)"
                                class="pop-waitlist-btn" 
                                type="danger" 
                                plain
                                size="small" 
                                :loading="waitlistPopLoadingId === schedule.id"
                                @click.stop="handlePopWaitlistClick(schedule)"
                              >
                                弹出候补
                              </el-button>
                            </div>
                          </div>
                    </div>
                        <div 
                          v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot].length > 5"
                          class="schedule-badge badge-more"
                          @click.stop="openDrawer(date.dateStr, 'morning', getGroupedSchedulesForDate(date.dateStr)[timeSlot])"
                        >
                          +{{ getGroupedSchedulesForDate(date.dateStr)[timeSlot].length - 5 }}
                  </div>
                      </template>
                      <!-- 下午排班 -->
                      <template v-if="timeSlot === 'afternoon'">
                        <div 
                          v-for="schedule in getGroupedSchedulesForDate(date.dateStr)[timeSlot].slice(0, 5)" 
                          :key="schedule.id"
                          class="schedule-badge"
                          :class="getScheduleBadgeClass(schedule)"
                          @click.stop="handleScheduleClick(schedule)"
                        >
                          <div class="badge-top">
                            <span class="doctor-name">{{ schedule.doctorName || '未知医生' }}</span>
                            <span v-if="schedule.slotType === 'expert'" class="badge-label badge-expert">专家</span>
                            <span v-else-if="schedule.slotType === 'vip'" class="badge-label badge-vip">特需</span>
                            <span v-else class="badge-label badge-normal">普通</span>
                          </div>
                          <div class="slots-actions">
                            <div class="slots-info-row">
                              <span class="slots-info">剩余：{{ schedule.availableSlots || 0 }}</span>
                              <span v-if="getWaitlistCount(schedule) > 0" class="waitlist-info">候补：{{ getWaitlistCount(schedule) }}</span>
                            </div>
                            <div class="slots-buttons-row">
                              <el-button class="add-slots-btn" type="warning" size="small" @click.stop="openAddSlotsDialog(schedule)">加号</el-button>
                              <el-button 
                                v-if="canPopWaitlist(schedule)"
                                class="pop-waitlist-btn" 
                                type="danger" 
                                plain
                                size="small" 
                                :loading="waitlistPopLoadingId === schedule.id"
                                @click.stop="handlePopWaitlistClick(schedule)"
                              >
                                弹出候补
                              </el-button>
                            </div>
                          </div>
                </div>
                        <div 
                          v-if="getGroupedSchedulesForDate(date.dateStr)[timeSlot].length > 5"
                          class="schedule-badge badge-more"
                          @click.stop="openDrawer(date.dateStr, 'afternoon', getGroupedSchedulesForDate(date.dateStr)[timeSlot])"
                        >
                          +{{ getGroupedSchedulesForDate(date.dateStr)[timeSlot].length - 5 }}
              </div>
                      </template>
                    </template>
                  </template>
                  <div v-if="!getSchedulesForDate(date.dateStr).length && date.isCurrentMonth" class="empty-schedule" @click.stop="handleDayCellClick(date)">
                    <div class="add-schedule-icon">
                      <el-icon><Plus /></el-icon>
            </div>
          </div>
          </div>
              </div>
            </div>
          </div>

        </el-card>
      </div>

      <!-- 排班详情抽屉 -->
      <el-drawer
        v-model="drawerVisible"
        :title="`${drawerDate} ${drawerTimeSlot === 'morning' ? '上午' : '下午'} 排班详情`"
        direction="rtl"
        size="400px"
      >
        <div class="drawer-schedules">
          <div 
            v-for="schedule in drawerSchedules" 
            :key="schedule.id"
            class="drawer-schedule-item"
            :class="getScheduleBadgeClass(schedule)"
            @click="handleScheduleClick(schedule)"
          >
            <div class="schedule-item-header badge-top">
              <span class="doctor-name">{{ schedule.doctorName || '未知医生' }}</span>
              <span v-if="schedule.slotType === 'expert'" class="badge-label badge-expert">专家</span>
              <span v-else-if="schedule.slotType === 'vip'" class="badge-label badge-vip">特需</span>
              <span v-else class="badge-label badge-normal">普通</span>
            </div>
            <div class="schedule-item-info">
              <span>总号源：{{ schedule.totalSlots || 0 }}</span>
              <span>剩余：{{ schedule.availableSlots || 0 }}</span>
              <span v-if="getWaitlistCount(schedule) > 0" class="waitlist-info">候补：{{ getWaitlistCount(schedule) }}</span>
              <el-button class="add-slots-btn" type="warning" size="small" @click.stop="openAddSlotsDialog(schedule)">加号</el-button>
              <el-button 
                v-if="canPopWaitlist(schedule)"
                class="pop-waitlist-btn" 
                type="danger" 
                plain
                size="small" 
                :loading="waitlistPopLoadingId === schedule.id"
                @click.stop="handlePopWaitlistClick(schedule)"
              >
                弹出候补
              </el-button>
            </div>
          </div>
        </div>
      </el-drawer>

      <!-- 列表视图 -->
      <div v-if="currentView === 'list'" class="list-view">
        <el-card>
          <template #header>
            <div class="list-header">
              <h3>排班列表</h3>
              <div class="list-controls">
                <el-button 
                  type="danger" 
                  @click="handleBatchDelete"
                  :loading="batchSubmitting"
                  :disabled="selectedRows.length === 0"
                >
                  <el-icon><Delete /></el-icon>
                  批量删除 ({{ selectedRows.length }})
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            :data="sortedScheduleList"
            v-loading="loading"
            @selection-change="handleSelectionChange"
            @sort-change="onSortChange"
            stripe
            style="width: 100%"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="doctorName" label="医生姓名" width="120" />
            <el-table-column prop="departmentName" label="科室" width="120" />
            <el-table-column prop="clinicName" label="门诊" width="120" />
            <el-table-column 
              prop="scheduleDate" 
              label="排班日期" 
              width="120" 
              sortable="custom" 
              :sort-orders="['ascending','descending']"
            >
              <template #default="scope">
                {{ formatDate(scope.row.scheduleDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="timeSlot" label="时间段" width="100">
              <template #default="scope">
                <el-tag :type="getTimeSlotType(scope.row.timeSlot)" size="small">
                  {{ getTimeSlotText(scope.row.timeSlot) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="slotType" label="号别" width="100">
              <template #default="scope">
                <el-tag :type="getSlotTypeColor(scope.row.slotType)" size="small">
                  {{ getSlotTypeText(scope.row.slotType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalSlots" label="总号源" width="80" />
            <el-table-column prop="availableSlots" label="剩余号源" width="90">
              <template #default="scope">
                <span :class="{ 'low-slots': scope.row.availableSlots < 5 }">
                  {{ scope.row.availableSlots }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="预约率" width="100">
              <template #default="scope">
                <el-progress 
                  :percentage="getBookingPercentage(scope.row)" 
                  :color="getProgressColor(scope.row)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column label="候补人数" width="100">
              <template #default="scope">
                <el-tag :type="getWaitlistCountTagType(scope.row)" size="small">
                  {{ getWaitlistCount(scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="scope">
                <el-button type="success" size="small" @click="openAddSlotsDialog(scope.row)">
                  加号
                </el-button>
                <el-button
                  v-if="canPopWaitlist(scope.row)"
                  type="danger"
                  plain
                  size="small"
                  :loading="waitlistPopLoadingId === scope.row.id"
                  @click="handlePopWaitlistClick(scope.row)"
                >
                  弹出候补
                </el-button>
                <el-button type="primary" size="small" @click="handleEdit(scope.row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </div>
    </div>
    </div>

    <!-- 创建/编辑排班对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑排班' : '新增排班'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="formData.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option
              v-for="doctor in dialogDoctorList"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="formData.scheduleDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="formData.timeSlot" placeholder="请选择时间段" style="width: 100%">
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
          </el-select>
        </el-form-item>
        <el-form-item label="号别" prop="slotType">
          <el-select v-model="formData.slotType" placeholder="请选择号别" style="width: 100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        <el-form-item label="总号源" prop="totalSlots">
          <el-input-number 
            v-model="formData.totalSlots" 
            :min="isEdit ? Math.max(1, editBookedCount) : 1" 
            :max="100" 
            :disabled="isEdit"
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="可用号源" prop="availableSlots">
          <el-input-number 
            v-model="formData.availableSlots" 
            :min="0" 
            :max="formData.totalSlots || 100" 
            :controls="false"
            :disabled="true"
            style="width: 100%" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          v-if="isEdit" 
          type="danger" 
          @click="handleDelete(formData)"
        >删除</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 加号对话框 -->
    <el-dialog
      v-model="addSlotsDialog.visible"
      title="加号"
      width="500px"
      @close="resetAddSlotsForm"
    >
      <el-form :model="addSlotsForm" :rules="addSlotsRules" ref="addSlotsFormRef" label-width="120px">
        <el-form-item label="排班信息">
          <div style="color:#606266;">
            日期：{{ formatDate(addSlotsDialog.schedule?.scheduleDate) }}
            ，时间段：{{ getTimeSlotText(addSlotsDialog.schedule?.timeSlot) }}
            ，号别：{{ getSlotTypeText(addSlotsDialog.schedule?.slotType) }}
          </div>
        </el-form-item>
        <el-form-item label="增加号源" prop="slotsToAdd">
          <el-input-number v-model="addSlotsForm.slotsToAdd" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="加号原因" prop="reason">
          <el-input v-model="addSlotsForm.reason" placeholder="可选，填写加号原因" />
        </el-form-item>
        <div class="add-slots-tip-card">
          <div class="tip-header">
            <el-icon><WarningFilled /></el-icon>
            <span class="tip-title">注意事项</span>
          </div>
          <ol class="tip-list">
            <li>此操作不可逆</li>
            <li>单次最多增加 50 个号源</li>
            <li>建议填写加号原因便于后续追溯</li>
          </ol>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addSlotsDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="addSlotsDialog.loading" @click="submitAddSlots">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量创建对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量创建排班"
      width="800px"
      @close="handleBatchDialogClose"
    >
      <el-form :model="batchFormData" :rules="batchFormRules" ref="batchFormRef" label-width="120px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="batchFormData.doctorId" placeholder="请选择医生" style="width: 100%" filterable clearable>
            <el-option
              v-for="doctor in dialogDoctorList"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchFormData.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlots">
          <el-checkbox-group v-model="batchFormData.timeSlots">
            <el-checkbox label="morning">上午</el-checkbox>
            <el-checkbox label="afternoon">下午</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="号别" prop="slotType">
          <el-select v-model="batchFormData.slotType" placeholder="请选择号别" style="width: 100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        <el-form-item label="总号源" prop="totalSlots">
          <el-input-number v-model="batchFormData.totalSlots" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跳过周末">
          <el-switch v-model="batchFormData.skipWeekends" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导出排班对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="导出排班表"
      width="420px"
    >
      <div class="export-dialog-content">
        <div class="export-tip">只支持已筛选的科室或医生导出</div>
        <el-radio-group v-model="exportMode">
          <el-radio-button label="week">周视图</el-radio-button>
          <el-radio-button label="month">月视图</el-radio-button>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="exporting" @click="performExport">导出</el-button>
      </template>
    </el-dialog>

    <!-- 号源设置管理对话框 -->
    <el-dialog
      v-model="settingsDialogVisible"
      title="号源设置管理"
      width="800px"
      @close="handleSettingsDialogClose"
    >
      <el-tabs v-model="settingsActiveTab" @tab-change="handleSettingsTabChange">
        <!-- 全局设置 -->
        <el-tab-pane label="全局设置" name="global">
          <el-form :model="globalSettings" :rules="settingsRules" ref="globalSettingsFormRef" label-width="180px" style="margin-top: 20px">
            <el-form-item label="允许的号别类型" prop="allowedSlotTypes">
              <el-checkbox-group v-model="globalSettings.allowedSlotTypes">
                <el-checkbox label="normal">普通号</el-checkbox>
                <el-checkbox label="expert">专家号</el-checkbox>
                <el-checkbox label="vip">特需号</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="默认总号源数" prop="defaultTotalSlots">
              <el-input-number v-model="globalSettings.defaultTotalSlots" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="单次排班最大号源数" prop="maxSlotsPerSchedule">
              <el-input-number v-model="globalSettings.maxSlotsPerSchedule" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="医生每日最大预约数" prop="maxAppointmentsPerDayPerDoctor">
              <el-input-number v-model="globalSettings.maxAppointmentsPerDayPerDoctor" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="患者每日最大预约数" prop="maxAppointmentsPerDayPerPatient">
              <el-input-number v-model="globalSettings.maxAppointmentsPerDayPerPatient" :min="1" :max="10" />
            </el-form-item>
            <el-form-item label="特需号每日限额" prop="vipDailyLimitPerDoctor">
              <el-input-number v-model="globalSettings.vipDailyLimitPerDoctor" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="强制执行周末限制" prop="enforceWeekendLimits">
              <el-switch v-model="globalSettings.enforceWeekendLimits" />
            </el-form-item>
            <el-form-item label="取消策略">
              <div style="width: 100%">
                <el-form-item label="最晚取消时间(小时)" prop="cancelPolicy.latestCancelHours" style="margin-bottom: 10px">
                  <el-input-number v-model="globalSettings.cancelPolicy.latestCancelHours" :min="0" :max="48" />
                </el-form-item>
                <el-form-item label="启用取消惩罚" prop="cancelPolicy.penaltyEnabled">
                  <el-switch v-model="globalSettings.cancelPolicy.penaltyEnabled" />
                </el-form-item>
              </div>
            </el-form-item>
            <el-form-item label="覆盖策略" prop="overrideStrategy">
              <el-select v-model="globalSettings.overrideStrategy" style="width: 100%">
                <el-option label="覆盖" value="OVERRIDE" />
                <el-option label="合并" value="MERGE" />
              </el-select>
            </el-form-item>
            <el-form-item label="生效开始日期" prop="effectiveStartDate">
              <el-date-picker
                v-model="globalSettings.effectiveStartDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="生效结束日期" prop="effectiveEndDate">
              <el-date-picker
                v-model="globalSettings.effectiveEndDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 医生级设置 -->
        <el-tab-pane label="医生级设置" name="doctor">
          <div style="margin: 20px 0">
            <el-form :inline="true" style="margin-bottom: 20px">
              <el-form-item label="选择医生">
                <el-select
                  v-model="selectedDoctorId"
                  placeholder="请选择医生"
                  filterable
                  clearable
                  style="width: 300px"
                  @change="loadDoctorSettings"
                >
                  <el-option
                    v-for="doctor in doctorList"
                    :key="doctor.id"
                    :label="doctor.name"
                    :value="doctor.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <el-form 
            v-if="selectedDoctorId" 
            :model="doctorSettings" 
            :rules="settingsRules" 
            ref="doctorSettingsFormRef" 
            label-width="180px"
          >
            <el-form-item label="允许的号别类型" prop="allowedSlotTypes">
              <el-checkbox-group v-model="doctorSettings.allowedSlotTypes">
                <el-checkbox label="normal">普通号</el-checkbox>
                <el-checkbox label="expert">专家号</el-checkbox>
                <el-checkbox label="vip">特需号</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="默认总号源数" prop="defaultTotalSlots">
              <el-input-number v-model="doctorSettings.defaultTotalSlots" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="单次排班最大号源数" prop="maxSlotsPerSchedule">
              <el-input-number v-model="doctorSettings.maxSlotsPerSchedule" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="特需号每日限额" prop="vipDailyLimitPerDoctor">
              <el-input-number v-model="doctorSettings.vipDailyLimitPerDoctor" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="覆盖策略" prop="overrideStrategy">
              <el-select v-model="doctorSettings.overrideStrategy" style="width: 100%">
                <el-option label="覆盖" value="OVERRIDE" />
                <el-option label="合并" value="MERGE" />
              </el-select>
            </el-form-item>
            <el-form-item label="生效开始日期" prop="effectiveStartDate">
              <el-date-picker
                v-model="doctorSettings.effectiveStartDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="生效结束日期" prop="effectiveEndDate">
              <el-date-picker
                v-model="doctorSettings.effectiveEndDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="doctorSettings.description" type="textarea" :rows="3" placeholder="可选，填写医生级设置描述" />
            </el-form-item>
          </el-form>
          <el-empty v-else description="请先选择医生" />
        </el-tab-pane>

        <!-- 门诊级设置 -->
        <el-tab-pane label="门诊级设置" name="clinic">
          <div style="margin: 20px 0">
            <el-form :inline="true" style="margin-bottom: 20px">
              <el-form-item label="选择门诊">
                <el-select
                  v-model="selectedClinicId"
                  placeholder="请选择门诊"
                  filterable
                  clearable
                  style="width: 300px"
                  @change="loadClinicSettings"
                >
                  <el-option
                    v-for="clinic in clinicList"
                    :key="clinic.id"
                    :label="clinic.name"
                    :value="clinic.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <el-form 
            v-if="selectedClinicId" 
            :model="clinicSettings" 
            :rules="settingsRules" 
            ref="clinicSettingsFormRef" 
            label-width="180px"
          >
            <el-form-item label="允许的号别类型" prop="allowedSlotTypes">
              <el-checkbox-group v-model="clinicSettings.allowedSlotTypes">
                <el-checkbox label="normal">普通号</el-checkbox>
                <el-checkbox label="expert">专家号</el-checkbox>
                <el-checkbox label="vip">特需号</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="默认总号源数" prop="defaultTotalSlots">
              <el-input-number v-model="clinicSettings.defaultTotalSlots" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="单次排班最大号源数" prop="maxSlotsPerSchedule">
              <el-input-number v-model="clinicSettings.maxSlotsPerSchedule" :min="1" :max="200" />
            </el-form-item>
            <el-form-item label="特需号每日限额" prop="vipDailyLimitPerDoctor">
              <el-input-number v-model="clinicSettings.vipDailyLimitPerDoctor" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="覆盖策略" prop="overrideStrategy">
              <el-select v-model="clinicSettings.overrideStrategy" style="width: 100%">
                <el-option label="覆盖" value="OVERRIDE" />
                <el-option label="合并" value="MERGE" />
              </el-select>
            </el-form-item>
            <el-form-item label="生效开始日期" prop="effectiveStartDate">
              <el-date-picker
                v-model="clinicSettings.effectiveStartDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="生效结束日期" prop="effectiveEndDate">
              <el-date-picker
                v-model="clinicSettings.effectiveEndDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-form>
          <el-empty v-else description="请先选择门诊" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="settingsSubmitting" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Plus, DocumentAdd, Delete, Calendar, List, 
  ArrowLeft, ArrowRight, Download, WarningFilled, Setting
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { 
  getScheduleList, 
  createSchedule, 
  updateSchedule, 
  deleteSchedule, 
  batchCreateSchedule,
  batchDeleteSchedule,
  getSchedulesByDoctorId,
  addScheduleSlots
} from '@/api/schedule'
import { getDoctorList } from '@/api/doctor'
import { getDepartmentList } from '@/api/department'
import { getClinicList } from '@/api/clinic'
import {
  getGlobalScheduleSettings,
  updateGlobalScheduleSettings,
  getDoctorScheduleSettings,
  updateDoctorScheduleSettings,
  getClinicScheduleSettings,
  updateClinicScheduleSettings
} from '@/api/scheduleSettings'
import { getWaitlistCounts, popNextWaitlist } from '@/api/waitlist'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const batchSubmitting = ref(false)
const exporting = ref(false)
const exportDialogVisible = ref(false)
const exportMode = ref('week')
const scheduleList = ref([])
const allSchedules = ref([]) // 所有排班数据，用于统计
const doctorList = ref([])
const departmentList = ref([])
const clinicList = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const addSlotsFormRef = ref(null)
const batchFormRef = ref(null)
const globalSettingsFormRef = ref(null)
const doctorSettingsFormRef = ref(null)
const clinicSettingsFormRef = ref(null)

// 候补队列相关
const waitlistCounts = ref({}) // 存储每个排班的候补人数 { scheduleId: count }
const waitlistPopLoadingId = ref(null) // 正在弹出候补的排班ID

// 视图状态
const currentView = ref('calendar') // calendar, list
const calendarMode = ref('week') // week, month
const calendarViewMode = ref('department') // department, doctor
const currentDate = ref(new Date())

// 筛选器
const filters = reactive({
  departmentId: '',
  doctorId: '',
  dateRange: null,
  timeSlot: '' // 时间段筛选：morning/afternoon
})



// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  doctorId: '',
  scheduleDate: '',
  timeSlot: '',
  slotType: '',
  totalSlots: 20,
  availableSlots: 20
})

const addSlotsDialog = reactive({
  visible: false,
  loading: false,
  schedule: null
})

const addSlotsForm = reactive({
  slotsToAdd: 1,
  reason: ''
})

const addSlotsAlertDesc = '此操作不可逆\n单次最多增加 50 个号源\n建议填写加号原因便于后续追溯'

const addSlotsRules = {
  slotsToAdd: [
    { required: true, message: '请输入要增加的号源数量', trigger: 'change' },
    { validator: (_rule, value, callback) => {
        if (typeof value !== 'number' || value < 1) return callback(new Error('增加的号源数量至少为1'))
        if (value > 50) return callback(new Error('单次增加的号源数量不能超过50'))
        callback()
      }, trigger: 'change' }
  ]
}

// 记录编辑模式下的已预约数量（保持不变）
const editBookedCount = ref(0)

// 新建/编辑时总号源变化逻辑：
// - 新建：可用号源与总号源一致
// - 编辑：保持已预约数量不变，可用号源 = 总号源 - 已预约数量
watch(() => formData.totalSlots, (newTotal) => {
  if (!isEdit.value) {
    formData.availableSlots = newTotal
  } else {
    const booked = Math.max(0, editBookedCount.value)
    formData.availableSlots = Math.max(0, newTotal - booked)
  }
})

const batchFormData = reactive({
  doctorId: null,
  dateRange: null,
  timeSlots: [],
  slotType: '',
  totalSlots: 20,
  skipWeekends: true
})

// 表单验证规则
const formRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  slotType: [{ required: true, message: '请选择号别', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请输入总号源数', trigger: 'blur' }]
}

const batchFormRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  slotType: [{ required: true, message: '请选择号别', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请输入总号源数', trigger: 'blur' }]
}

// 号源设置相关
const settingsDialogVisible = ref(false)
const settingsActiveTab = ref('global')
const settingsSubmitting = ref(false)
const selectedDoctorId = ref(null)
const selectedClinicId = ref(null)

// 全局设置
const globalSettings = reactive({
  allowedSlotTypes: ['normal', 'expert'],
  defaultTotalSlots: 20,
  maxSlotsPerSchedule: 50,
  maxAppointmentsPerDayPerDoctor: 60,
  maxAppointmentsPerDayPerPatient: 3,
  vipDailyLimitPerDoctor: 10,
  enforceWeekendLimits: true,
  cancelPolicy: {
    latestCancelHours: 2,
    penaltyEnabled: false
  },
  overrideStrategy: 'OVERRIDE',
  effectiveStartDate: null,
  effectiveEndDate: null
})

// 医生级设置
const doctorSettings = reactive({
  allowedSlotTypes: [],
  defaultTotalSlots: null,
  maxSlotsPerSchedule: null,
  vipDailyLimitPerDoctor: null,
  overrideStrategy: 'OVERRIDE',
  effectiveStartDate: null,
  effectiveEndDate: null,
  description: ''
})

// 门诊级设置
const clinicSettings = reactive({
  allowedSlotTypes: [],
  defaultTotalSlots: null,
  maxSlotsPerSchedule: null,
  vipDailyLimitPerDoctor: null,
  overrideStrategy: 'OVERRIDE',
  effectiveStartDate: null,
  effectiveEndDate: null
})

// 设置表单验证规则
const settingsRules = {
  allowedSlotTypes: [{ required: true, message: '请至少选择一个号别类型', trigger: 'change' }],
  defaultTotalSlots: [{ required: true, message: '请输入默认总号源数', trigger: 'blur' }],
  maxSlotsPerSchedule: [{ required: true, message: '请输入单次排班最大号源数', trigger: 'blur' }]
}

// 排序状态与排序后的数据
const sortState = reactive({ prop: null, order: null })

const toTimestamp = (val) => {
  if (!val) return 0
  if (typeof val === 'string') {
    const s = val.includes('T') ? val : `${val}T00:00:00`
    const t = Date.parse(s)
    return isNaN(t) ? 0 : t
  }
  try {
    return new Date(val).getTime()
  } catch {
    return 0
  }
}

const sortedScheduleList = computed(() => {
  const data = scheduleList.value || []
  if (!sortState.order || !sortState.prop) return data
  const arr = [...data]
  if (sortState.prop === 'scheduleDate') {
    arr.sort((a, b) => {
      const ta = toTimestamp(a.scheduleDate)
      const tb = toTimestamp(b.scheduleDate)
      return sortState.order === 'ascending' ? ta - tb : tb - ta
    })
  } else {
    arr.sort((a, b) => {
      const va = a[sortState.prop]
      const vb = b[sortState.prop]
      if (typeof va === 'number' && typeof vb === 'number') {
        return sortState.order === 'ascending' ? va - vb : vb - va
      }
      const sa = String(va ?? '')
      const sb = String(vb ?? '')
      return sortState.order === 'ascending' ? sa.localeCompare(sb) : sb.localeCompare(sa)
    })
  }
  return arr
})

const onSortChange = ({ prop, order }) => {
  sortState.prop = prop
  sortState.order = order
}

const buildScheduleDateTime = (schedule) => {
  if (!schedule?.scheduleDate) return null
  let dateObj
  if (typeof schedule.scheduleDate === 'string') {
    if (schedule.scheduleDate.includes('T')) {
      dateObj = new Date(schedule.scheduleDate)
    } else {
      dateObj = new Date(`${schedule.scheduleDate}T00:00:00`)
    }
  } else {
    dateObj = new Date(schedule.scheduleDate)
  }
  if (Number.isNaN(dateObj.getTime())) return null
  const slot = String(schedule.timeSlot || '').toLowerCase()
  if (slot === 'afternoon') {
    dateObj.setHours(17, 0, 0, 0)
  } else if (slot === 'evening') {
    dateObj.setHours(21, 0, 0, 0)
  } else {
    dateObj.setHours(12, 0, 0, 0)
  }
  return dateObj
}

const isScheduleUpcoming = (schedule) => {
  const dt = buildScheduleDateTime(schedule)
  if (!dt) return false
  return dt.getTime() >= Date.now()
}

// 计算属性
const filteredDoctorList = computed(() => {
  if (!filters.departmentId) return doctorList.value
  // 确保类型匹配（都转换为数字或字符串进行比较）
  const deptId = Number(filters.departmentId)
  return doctorList.value.filter(doctor => {
    // 医生通过 clinic 关联到 department，所以需要通过 clinic.departmentId 获取
    const doctorDeptId = doctor.clinic?.departmentId ? Number(doctor.clinic.departmentId) : null
    return doctorDeptId === deptId
  })
})

// 对话框中的医生列表（根据左侧筛选条件过滤）
const dialogDoctorList = computed(() => {
  // 如果左侧筛选栏选择了科室，则只显示该科室的医生
  if (filters.departmentId) {
    const deptId = Number(filters.departmentId)
    return doctorList.value.filter(doctor => {
      const doctorDeptId = doctor.clinic?.departmentId ? Number(doctor.clinic.departmentId) : null
      return doctorDeptId === deptId
    })
  }
  // 否则显示所有医生
  return doctorList.value
})

const weekDates = computed(() => {
  const dates = []
  const startOfWeek = getStartOfWeek(currentDate.value)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)
    
    dates.push({
      dateStr: formatDateStr(date),
      day: date.getDate(),
      dayName: getDayName(date.getDay()),
      isToday: isToday(date)
    })
  }
  
  return dates
})

const monthDates = computed(() => {
  const dates = []
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  // 获取月份第一天和最后一天
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  // 获取第一周的开始日期
  const startDate = getStartOfWeek(firstDay)
  
  // 生成6周的日期
  for (let week = 0; week < 6; week++) {
    for (let day = 0; day < 7; day++) {
      const date = new Date(startDate)
      date.setDate(startDate.getDate() + week * 7 + day)
      
      dates.push({
        dateStr: formatDateStr(date),
        day: date.getDate(),
        isToday: isToday(date),
        isCurrentMonth: date.getMonth() === month
      })
    }
  }
  
  return dates
})

const currentPeriodText = computed(() => {
  if (calendarMode.value === 'week') {
    const start = weekDates.value[0]
    const end = weekDates.value[6]
    return `${start.dateStr} - ${end.dateStr}`
  } else {
    return `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`
  }
})

const calendarDepartments = computed(() => {
  const depts = departmentList.value.map(dept => ({
    ...dept,
    doctorCount: doctorList.value.filter(doctor => {
      const doctorDeptId = doctor.clinic?.departmentId ? Number(doctor.clinic.departmentId) : null
      return doctorDeptId === Number(dept.id)
    }).length
  }))
  
  // 如果有筛选条件，只显示筛选的科室
  if (filters.departmentId) {
    return depts.filter(dept => dept.id === filters.departmentId)
  }
  
  return depts
})

const calendarDoctors = computed(() => {
  let doctors = doctorList.value.map(doctor => ({
    ...doctor,
    departmentName: doctor.clinic?.department 
      ? doctor.clinic.department.name 
      : (departmentList.value.find(dept => dept.id === (doctor.clinic?.departmentId || null))?.name || '未知科室')
  }))
  
  // 如果有筛选条件，只显示筛选的医生
  if (filters.departmentId) {
    const filterDeptId = Number(filters.departmentId)
    doctors = doctors.filter(doctor => {
      const doctorDeptId = doctor.clinic?.departmentId ? Number(doctor.clinic.departmentId) : null
      return doctorDeptId === filterDeptId
    })
  }
  
  if (filters.doctorId) {
    doctors = doctors.filter(doctor => doctor.id === filters.doctorId)
  }
  
  return doctors
})

// 统计信息
const todaySchedules = computed(() => {
  const today = formatDateStr(new Date())
  return allSchedules.value.filter(schedule => {
    // 处理日期格式，确保比较正确
    const scheduleDate = schedule.scheduleDate
    if (!scheduleDate) return false
    // 如果是日期对象，转换为字符串
    const dateStr = typeof scheduleDate === 'string' 
      ? scheduleDate.split('T')[0] // 处理 ISO 格式日期
      : formatDateStr(new Date(scheduleDate))
    return dateStr === today
  }).length
})

const weekSchedules = computed(() => {
  const today = new Date()
  const startOfWeek = getStartOfWeek(today)
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6)
  
  const weekStartStr = formatDateStr(startOfWeek)
  const weekEndStr = formatDateStr(endOfWeek)
  
  return allSchedules.value.filter(schedule => {
    if (!schedule.scheduleDate) return false
    // 处理日期格式
    const scheduleDate = schedule.scheduleDate
    const dateStr = typeof scheduleDate === 'string' 
      ? scheduleDate.split('T')[0]
      : formatDateStr(new Date(scheduleDate))
    return dateStr >= weekStartStr && dateStr <= weekEndStr
  }).length
})

// 加载所有排班数据（用于统计）
const loadAllSchedules = async () => {
  try {
    // 不传任何筛选条件，获取所有排班
    // 先获取第一页，看看总数
    const firstPageResponse = await getScheduleList({
      page: 1,
      pageSize: 100
    })
    
    let allData = []
    let total = 0
    
    // 解析第一页数据
    if (firstPageResponse && firstPageResponse.data) {
      if (Array.isArray(firstPageResponse.data)) {
        allData = [...firstPageResponse.data]
        total = firstPageResponse.data.length
      } else if (firstPageResponse.data.list && Array.isArray(firstPageResponse.data.list)) {
        allData = [...firstPageResponse.data.list]
        total = firstPageResponse.data.total || firstPageResponse.data.list.length
      } else if (firstPageResponse.data.records && Array.isArray(firstPageResponse.data.records)) {
        allData = [...firstPageResponse.data.records]
        total = firstPageResponse.data.total || firstPageResponse.data.records.length
      }
    }
    
    // 如果总数大于第一页的数量，需要获取剩余数据
    if (total > allData.length) {
      const pageSize = 100
      const totalPages = Math.ceil(total / pageSize)
      
      for (let page = 2; page <= totalPages; page++) {
        const response = await getScheduleList({
          page: page,
          pageSize: pageSize
        })
        
        if (response && response.data) {
          let pageData = []
          if (Array.isArray(response.data)) {
            pageData = response.data
          } else if (response.data.list && Array.isArray(response.data.list)) {
            pageData = response.data.list
          } else if (response.data.records && Array.isArray(response.data.records)) {
            pageData = response.data.records
          }
          allData = [...allData, ...pageData]
        }
      }
    }
    
    allSchedules.value = allData
    
    // 为日历视图加载候补人数
    if (allData.length > 0) {
      const scheduleIds = allData.map(s => s.id).filter(Boolean)
      loadWaitlistCounts(scheduleIds)
    }
  } catch (error) {
    console.error('获取所有排班数据失败:', error)
    allSchedules.value = []
  }
}

// 方法
const loadScheduleList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters
    }
    
    // 处理日期范围
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    
    // 处理时间段筛选
    if (filters.timeSlot) {
      params.timeSlot = filters.timeSlot
    }
    
    const response = await getScheduleList(params)
    
    // 确保数据是数组格式
    let data = []
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        data = response.data
      } else if (response.data.records && Array.isArray(response.data.records)) {
        data = response.data.records
      } else if (response.data.list && Array.isArray(response.data.list)) {
        data = response.data.list
      }
    }
    
    scheduleList.value = data
    pagination.total = response?.data?.total || data.length
    
    selectedRows.value = []
    
    // 批量查询候补人数
    if (data.length > 0) {
      loadWaitlistCounts(data.map(s => s.id))
    }
  } catch (error) {
    ElMessage.error('获取排班列表失败')
    console.error('获取排班列表失败:', error)
    scheduleList.value = []
    selectedRows.value = []
  } finally {
    loading.value = false
  }
}

const loadDoctorList = async () => {
  try {
    const response = await getDoctorList()
    doctorList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取医生列表失败')
    console.error('获取医生列表失败:', error)
  }
}

const loadDepartmentList = async () => {
  try {
    const response = await getDepartmentList()
    departmentList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取科室列表失败')
    console.error('获取科室列表失败:', error)
  }
}

const loadClinicList = async () => {
  try {
    const response = await getClinicList()
    clinicList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取门诊列表失败')
    console.error('获取门诊列表失败:', error)
  }
}

const openAddSlotsDialog = (row) => {
  addSlotsDialog.schedule = row
  addSlotsForm.slotsToAdd = 1
  addSlotsForm.reason = ''
  addSlotsDialog.visible = true
}

const resetAddSlotsForm = () => {
  if (addSlotsFormRef.value) {
    addSlotsFormRef.value.clearValidate()
  }
  addSlotsForm.slotsToAdd = 1
  addSlotsForm.reason = ''
}

const submitAddSlots = async () => {
  try {
    if (addSlotsFormRef.value) {
      const valid = await addSlotsFormRef.value.validate()
      if (!valid) return
    }
    const n = Number(addSlotsForm.slotsToAdd)
    if (!Number.isInteger(n) || n < 1 || n > 50) {
      ElMessage.error('增加的号源数量必须在 1-50 之间')
      return
    }
    await ElMessageBox.confirm('此操作不可逆，确认增加号源吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    addSlotsDialog.loading = true
    const id = addSlotsDialog.schedule?.id
    await addScheduleSlots(id, {
      slotsToAdd: addSlotsForm.slotsToAdd,
      reason: addSlotsForm.reason || undefined
    })
    ElMessage.success('加号成功')
    addSlotsDialog.visible = false
    await loadScheduleList()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '加号失败')
    console.error('加号失败:', error)
  } finally {
    addSlotsDialog.loading = false
  }
}

// 筛选器事件处理
const handleDepartmentChange = () => {
  // 当科室改变时，如果当前选择的医生不属于新科室，则清空医生选择
  if (filters.doctorId) {
    const selectedDoctor = doctorList.value.find(doctor => doctor.id === filters.doctorId)
    if (selectedDoctor) {
      const doctorDeptId = selectedDoctor.clinic?.departmentId ? Number(selectedDoctor.clinic.departmentId) : null
      const filterDeptId = Number(filters.departmentId)
      if (doctorDeptId !== filterDeptId) {
        filters.doctorId = ''
      }
    }
  }
  loadScheduleList()
}

const handleDoctorChange = () => {
  // 当医生改变时，自动设置对应的科室
  if (filters.doctorId) {
    const selectedDoctor = doctorList.value.find(doctor => doctor.id === filters.doctorId)
    if (selectedDoctor && selectedDoctor.clinic?.departmentId) {
      const doctorDeptId = Number(selectedDoctor.clinic.departmentId)
      const currentDeptId = filters.departmentId ? Number(filters.departmentId) : null
      // 只有当科室与医生不匹配时才更新科室，避免不必要的更新
      if (currentDeptId !== doctorDeptId) {
        filters.departmentId = selectedDoctor.clinic.departmentId
      }
    }
  }
  // 注意：当清空医生选择时，不自动清空科室，保持用户的科室筛选
  loadScheduleList()
}

const handleDateRangeChange = () => {
  // 如果选择了日期范围，自动跳转到开始日期
  if (filters.dateRange && filters.dateRange.length === 2) {
    const startDate = new Date(filters.dateRange[0])
    // 根据当前视图模式跳转
    if (calendarMode.value === 'week') {
      // 周视图：跳转到开始日期所在的周
      currentDate.value = startDate
    } else {
      // 月视图：跳转到开始日期所在的月
      currentDate.value = startDate
    }
  }
  // 更新列表数据（连接后端接口）
  loadScheduleList()
  // 更新所有排班数据（用于日历视图）
  loadAllSchedules()
}

const handleTimeSlotChange = () => {
  // 更新列表数据（连接后端接口）
  loadScheduleList()
  // 更新所有排班数据（用于日历视图）
  loadAllSchedules()
}

const setQuickDate = (type) => {
  const today = new Date()
  
  switch (type) {
    case 'today':
      filters.dateRange = [formatDateStr(today), formatDateStr(today)]
      break
    case 'week':
      const weekStart = getStartOfWeek(today)
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)
      filters.dateRange = [formatDateStr(weekStart), formatDateStr(weekEnd)]
      break
    case 'month':
      const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
      const monthEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0)
      filters.dateRange = [formatDateStr(monthStart), formatDateStr(monthEnd)]
      break
  }
  
  loadScheduleList()
}

const resetFilters = () => {
  Object.assign(filters, {
    departmentId: '',
    doctorId: '',
    dateRange: null,
    timeSlot: ''
  })
  pagination.page = 1
  loadScheduleList()
}

// 视图切换
const handleViewChange = (view) => {
  currentView.value = view
}

const handleCalendarModeChange = (mode) => {
  calendarMode.value = mode
}

const handleCalendarViewModeChange = (mode) => {
  calendarViewMode.value = mode
}

// 日历导航
const navigateDate = (direction) => {
  const newDate = new Date(currentDate.value)
  
  if (calendarMode.value === 'week') {
    newDate.setDate(newDate.getDate() + direction * 7)
  } else {
    newDate.setMonth(newDate.getMonth() + direction)
  }
  
  currentDate.value = newDate
}

// 日历事件处理
const handleCellClick = (entity, date) => {
  // 打开创建排班对话框，预填充信息
  showCreateDialog()
  
  if (calendarViewMode.value === 'doctor') {
    formData.doctorId = entity.id
  }
  
  formData.scheduleDate = date.dateStr
}

const handleScheduleClick = (schedule) => {
  handleEdit(schedule)
}

// 打开抽屉显示更多排班
const openDrawer = (dateStr, timeSlot, schedules) => {
  drawerDate.value = dateStr
  drawerTimeSlot.value = timeSlot
  drawerSchedules.value = schedules
  drawerVisible.value = true
}

// 获取号别文本
const getSlotTypeText = (slotType) => {
  const typeMap = {
    normal: '普通号',
    expert: '专家号',
    vip: '特需号'
  }
  return typeMap[slotType] || slotType
}

const handleDayCellClick = (date) => {
  // 打开创建排班对话框，预填充日期
  showCreateDialog()
  formData.scheduleDate = date.dateStr
}

const handleMonthCellClick = (date) => {
  // 切换到周视图并定位到该日期
  currentDate.value = new Date(date.dateStr)
  calendarMode.value = 'week'
}

// 获取日历单元格的排班数据
const getSchedulesForCell = (departmentId, dateStr) => {
  return scheduleList.value.filter(schedule => 
    schedule.departmentId === departmentId && 
    schedule.scheduleDate === dateStr
  )
}

const getDoctorSchedulesForDate = (doctorId, dateStr) => {
  return scheduleList.value.filter(schedule => 
    schedule.doctorId === doctorId && 
    schedule.scheduleDate === dateStr
  )
}

const getSchedulesForDate = (dateStr) => {
  // 使用 allSchedules 而不是 scheduleList，因为日历视图需要显示所有排班数据
  // 但需要应用左侧的筛选条件（科室和医生）
  let filteredSchedules = allSchedules.value
  
  // 应用科室筛选
  if (filters.departmentId) {
    const deptId = Number(filters.departmentId)
    filteredSchedules = filteredSchedules.filter(schedule => {
      // 排班数据中可能有 departmentId 字段，或者需要通过 doctor 关联
      if (schedule.departmentId) {
        return Number(schedule.departmentId) === deptId
      }
      // 如果没有 departmentId，尝试通过 doctor 查找
      const doctor = doctorList.value.find(d => d.id === schedule.doctorId)
      if (doctor && doctor.clinic?.departmentId) {
        return Number(doctor.clinic.departmentId) === deptId
      }
      return false
    })
  }
  
  // 应用医生筛选
  if (filters.doctorId) {
    filteredSchedules = filteredSchedules.filter(schedule => {
      return schedule.doctorId === filters.doctorId
    })
  }
  
  // 应用时间段筛选
  if (filters.timeSlot) {
    filteredSchedules = filteredSchedules.filter(schedule => {
      const timeSlot = (schedule.timeSlot || '').toLowerCase()
      return timeSlot === filters.timeSlot.toLowerCase()
    })
  }
  
  // 应用日期筛选
  return filteredSchedules.filter(schedule => {
    const scheduleDate = schedule.scheduleDate
    const dateStrFormatted = typeof scheduleDate === 'string' 
      ? scheduleDate.split('T')[0]
      : formatDateStr(new Date(scheduleDate))
    return dateStrFormatted === dateStr
  })
}

// 判断日期是否在筛选范围内
const isDateInRange = (dateStr) => {
  if (!filters.dateRange || filters.dateRange.length !== 2) {
    return false
  }
  const startDate = filters.dateRange[0]
  const endDate = filters.dateRange[1]
  return dateStr >= startDate && dateStr <= endDate
}

// 获取按时间段分组的排班数据
const getGroupedSchedulesForDate = (dateStr) => {
  const schedules = getSchedulesForDate(dateStr)
  const grouped = {
    morning: [],
    afternoon: []
  }
  
  schedules.forEach(schedule => {
    const timeSlot = (schedule.timeSlot || '').toLowerCase()
    if (timeSlot === 'morning') {
      grouped.morning.push(schedule)
    } else if (timeSlot === 'afternoon') {
      grouped.afternoon.push(schedule)
    }
  })
  
  return grouped
}

// 抽屉相关状态
const drawerVisible = ref(false)
const drawerDate = ref('')
const drawerTimeSlot = ref('')
const drawerSchedules = ref([])

// 表格事件处理
const handleSelectionChange = (selection) => {
  selectedRows.value = Array.isArray(selection) ? selection : []
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadScheduleList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadScheduleList()
}

// 对话框处理
const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetFormData()
  editBookedCount.value = 0
  
  // 如果左侧筛选栏已经选择了医生，则自动填充
  // 注意：要在 resetFormData() 之后设置，否则会被重置
  if (filters.doctorId) {
    formData.doctorId = filters.doctorId
  }
}

const showBatchCreateDialog = () => {
  batchDialogVisible.value = true
  resetBatchFormData()
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(formData, { ...row })
  // 进入编辑时计算已预约数量，并作为常量维持不变
  editBookedCount.value = Math.max(0, (formData.totalSlots || 0) - (formData.availableSlots || 0))
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    // 更新统计数据
    await loadAllSchedules()
    loadScheduleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除排班失败:', error)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的排班')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个排班吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    batchSubmitting.value = true
    const ids = selectedRows.value.map(row => row.id).filter(Boolean)
    const results = await Promise.allSettled(ids.map(id => deleteSchedule(id)))
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount = results.length - successCount
    if (failCount === 0) {
      ElMessage.success(`批量删除成功，共删除 ${successCount} 条`)
    } else if (successCount > 0) {
      ElMessage.warning(`部分删除成功：成功 ${successCount} 条，失败 ${failCount} 条`)
    } else {
      ElMessage.error('批量删除失败')
    }
    // 更新统计数据
    await loadAllSchedules()
    loadScheduleList()
    selectedRows.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error('批量删除排班失败:', error)
    }
  } finally {
    batchSubmitting.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateSchedule(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      // 创建时将可用号源设为与总号源一致
      formData.availableSlots = formData.totalSlots
      await createSchedule(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    // 更新统计数据
    await loadAllSchedules()
    loadScheduleList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error('提交排班失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

const handleBatchSubmit = async () => {
  try {
    await batchFormRef.value.validate()
    batchSubmitting.value = true
    
    const data = {
      doctorId: batchFormData.doctorId || null,
      startDate: Array.isArray(batchFormData.dateRange) ? batchFormData.dateRange[0] : null,
      endDate: Array.isArray(batchFormData.dateRange) ? batchFormData.dateRange[1] : null,
      timeSlots: batchFormData.timeSlots,
      slotType: batchFormData.slotType,
      totalSlots: batchFormData.totalSlots,
      skipWeekends: batchFormData.skipWeekends,
      excludeDates: batchFormData.excludeDates || []
    }
    
    // 简单校验，避免后端NotNull报错
    if (!data.doctorId || !data.startDate || !data.endDate) {
      ElMessage.warning('请先选择医生和日期范围')
      batchSubmitting.value = false
      return
    }
    
    const response = await batchCreateSchedule(data)
    ElMessage.success(`批量创建完成：成功 ${response.data?.successCount || 0} 个`)
    
    batchDialogVisible.value = false
    // 更新统计数据
    await loadAllSchedules()
    loadScheduleList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('批量创建失败')
      console.error('批量创建排班失败:', error)
    }
  } finally {
    batchSubmitting.value = false
  }
}

const handleDialogClose = () => {
  resetFormData()
  formRef.value?.clearValidate()
}

const handleBatchDialogClose = () => {
  resetBatchFormData()
  batchFormRef.value?.clearValidate()
}

const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    doctorId: '',
    scheduleDate: '',
    timeSlot: '',
    slotType: '',
    totalSlots: 20,
    availableSlots: 20
  })
}

const resetBatchFormData = () => {
  Object.assign(batchFormData, {
    doctorId: null,
    dateRange: null,
    timeSlots: [],
    slotType: '',
    totalSlots: 20,
    skipWeekends: true
  })
}

// 导出功能
const exportSchedules = async () => {
  // 限制：必须筛选科室或医生
  if (!filters.departmentId && !filters.doctorId) {
    ElMessage.warning('请先在左侧筛选科室或医生后再导出')
    return
  }
  exportMode.value = calendarMode.value // 默认使用当前视图
  exportDialogVisible.value = true
}

const performExport = async () => {
  try {
    exporting.value = true
    const wb = XLSX.utils.book_new()
    const title = exportMode.value === 'week' ? '周视图' : '月视图'

    // 构造矩阵（二维数组）
    const aoa = exportMode.value === 'week' ? buildWeekMatrix() : buildMonthMatrix()
    const ws = XLSX.utils.aoa_to_sheet(aoa)
    // 列宽适配
    ws['!cols'] = new Array(7).fill({ wch: 28 })
    XLSX.utils.book_append_sheet(wb, ws, title)

    // 文件名：包含筛选对象与周期
    const targetName = filters.doctorId
      ? (doctorList.value.find(d => d.id === filters.doctorId)?.name || '医生')
      : (departmentList.value.find(dept => Number(dept.id) === Number(filters.departmentId))?.name || '科室')
    const period = exportMode.value === 'week'
      ? `${weekDates.value[0].dateStr}_至_${weekDates.value[6].dateStr}`
      : `${currentDate.value.getFullYear()}年${String(currentDate.value.getMonth() + 1).padStart(2, '0')}月`
    const filename = `排班表_${targetName}_${title}_${period}.xlsx`

    XLSX.writeFile(wb, filename)
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 周视图矩阵：第一行是周几与日期，随后两行分别是“上午”与“下午”内容
const buildWeekMatrix = () => {
  const header = weekDates.value.map(d => `${d.dayName} (${d.dateStr})`)
  const morningRow = weekDates.value.map(d => buildSlotContent(d.dateStr, 'morning'))
  const afternoonRow = weekDates.value.map(d => buildSlotContent(d.dateStr, 'afternoon'))
  return [header, morningRow, afternoonRow]
}

// 月视图矩阵：第一行是周几；每周使用两行，分别输出“上午”与“下午”内容
const buildMonthMatrix = () => {
  const header = ['日', '一', '二', '三', '四', '五', '六']
  const rows = []
  for (let i = 0; i < 6; i++) {
    const morningRow = []
    const afternoonRow = []
    for (let j = 0; j < 7; j++) {
      const index = i * 7 + j
      const date = monthDates.value[index]
      const prefix = `${date.dateStr}${date.isCurrentMonth ? '' : ' (其他月)'}\n`
      const morning = buildSlotContent(date.dateStr, 'morning')
      const afternoon = buildSlotContent(date.dateStr, 'afternoon')
      morningRow.push(`${prefix}${morning}`)
      afternoonRow.push(`${afternoon}`)
    }
    rows.push(morningRow)
    rows.push(afternoonRow)
  }
  return [header, ...rows]
}

// 构造某日期某时间段内容（展开所有排班）
const buildSlotContent = (dateStr, slot) => {
  const grouped = getGroupedSchedulesForDate(dateStr)
  const list = slot === 'morning' ? grouped.morning : grouped.afternoon
  if (!list || list.length === 0) return '—'
  return list.map(s => {
    const tag = s.slotType === 'vip' ? '[特需]' : (s.slotType === 'expert' ? '[专家]' : '')
    const name = s.doctorName || '未知医生'
    const avail = typeof s.availableSlots === 'number' ? s.availableSlots : 0
    return `• ${name} ${tag} 余${avail}`
  }).join('\n')
}

// 辅助方法
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateStr = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getStartOfWeek = (date) => {
  const d = new Date(date)
  // 将一周起始日设为周一；若为周日(getDay()==0)，则回退6天，否则回退(day-1)天
  const day = d.getDay()
  const back = day === 0 ? 6 : day - 1
  d.setDate(d.getDate() - back)
  return d
}

const getDayName = (dayIndex) => {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return days[dayIndex]
}

const isToday = (date) => {
  const today = new Date()
  return formatDateStr(date) === formatDateStr(today)
}

const getTimeSlotType = (timeSlot) => {
  // 处理大小写不敏感
  const slot = (timeSlot || '').toUpperCase()
  const typeMap = {
    MORNING: 'success',
    AFTERNOON: 'warning'
  }
  return typeMap[slot] || ''
}

const getTimeSlotText = (timeSlot) => {
  // 处理大小写不敏感
  const slot = (timeSlot || '').toUpperCase()
  const textMap = {
    MORNING: '上午',
    AFTERNOON: '下午'
  }
  return textMap[slot] || timeSlot
}

const getSlotTypeColor = (slotType) => {
  const colorMap = {
    normal: '',
    expert: 'warning',
    vip: 'danger'
  }
  return colorMap[slotType] || ''
}

const getBookingPercentage = (schedule) => {
  if (!schedule.totalSlots || schedule.totalSlots === 0) return 0
  const booked = schedule.totalSlots - (schedule.availableSlots || 0)
  return Math.round((booked / schedule.totalSlots) * 100)
}

const getProgressColor = (schedule) => {
  const percentage = getBookingPercentage(schedule)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const getScheduleClass = (schedule) => {
  const classes = ['schedule-item']
  
  // 根据时间段添加颜色类（处理大小写不敏感）
  const timeSlot = (schedule.timeSlot || '').toUpperCase()
  if (timeSlot === 'MORNING') classes.push('morning-slot')
  else if (timeSlot === 'AFTERNOON') classes.push('afternoon-slot')
  
  // 根据号别添加样式类
  if (schedule.slotType === 'expert') classes.push('expert-slot')
  else if (schedule.slotType === 'vip') classes.push('vip-slot')
  
  // 根据剩余号源添加警告类
  if (schedule.availableSlots < 5) classes.push('low-slots')
  
  return classes.join(' ')
}

// 获取排班徽章文本（如"上3位"、"下1位"）
const getScheduleBadgeText = (schedule) => {
  // 处理大小写不敏感
  const timeSlot = (schedule.timeSlot || '').toUpperCase()
  const timeSlotMap = {
    MORNING: '上',
    AFTERNOON: '下'
  }
  const timeSlotText = timeSlotMap[timeSlot] || ''
  return `${timeSlotText}${schedule.availableSlots || 0}位`
}

// 获取排班徽章样式类
const getScheduleBadgeClass = (schedule) => {
  const classes = ['schedule-badge']
  
  // 根据时间段添加颜色类（处理大小写不敏感）
  const timeSlot = (schedule.timeSlot || '').toUpperCase()
  if (timeSlot === 'MORNING') classes.push('badge-morning')
  else if (timeSlot === 'AFTERNOON') classes.push('badge-afternoon')
  
  // 根据号别添加醒目标识样式
  if (schedule.slotType === 'expert') classes.push('expert-badge')
  else if (schedule.slotType === 'vip') classes.push('vip-badge')
  
  return classes.join(' ')
}

// 号源设置相关方法
const showSettingsDialog = async () => {
  settingsDialogVisible.value = true
  settingsActiveTab.value = 'global'
  await loadGlobalSettings()
}

const loadGlobalSettings = async () => {
  try {
    const response = await getGlobalScheduleSettings()
    if (response && response.data) {
      const data = response.data
      Object.assign(globalSettings, {
        allowedSlotTypes: data.allowedSlotTypes || ['normal', 'expert'],
        defaultTotalSlots: data.defaultTotalSlots || 20,
        maxSlotsPerSchedule: data.maxSlotsPerSchedule || 50,
        maxAppointmentsPerDayPerDoctor: data.maxAppointmentsPerDayPerDoctor || 60,
        maxAppointmentsPerDayPerPatient: data.maxAppointmentsPerDayPerPatient || 3,
        vipDailyLimitPerDoctor: data.vipDailyLimitPerDoctor || 10,
        enforceWeekendLimits: data.enforceWeekendLimits !== undefined ? data.enforceWeekendLimits : true,
        cancelPolicy: {
          latestCancelHours: data.cancelPolicy?.latestCancelHours || 2,
          penaltyEnabled: data.cancelPolicy?.penaltyEnabled || false
        },
        overrideStrategy: data.overrideStrategy || 'OVERRIDE',
        effectiveStartDate: data.effectiveStartDate || null,
        effectiveEndDate: data.effectiveEndDate || null
      })
    }
  } catch (error) {
    console.error('获取全局设置失败:', error)
    ElMessage.error('获取全局设置失败')
  }
}

const loadDoctorSettings = async () => {
  if (!selectedDoctorId.value) {
    resetDoctorSettings()
    return
  }
  try {
    const response = await getDoctorScheduleSettings(selectedDoctorId.value)
    if (response && response.data) {
      const data = response.data
      // 如果返回空对象，表示继承全局设置
      if (Object.keys(data).length === 0) {
        resetDoctorSettings()
        return
      }
      Object.assign(doctorSettings, {
        allowedSlotTypes: data.allowedSlotTypes || [],
        defaultTotalSlots: data.defaultTotalSlots || null,
        maxSlotsPerSchedule: data.maxSlotsPerSchedule || null,
        vipDailyLimitPerDoctor: data.vipDailyLimitPerDoctor || null,
        overrideStrategy: data.overrideStrategy || 'OVERRIDE',
        effectiveStartDate: data.effectiveStartDate || null,
        effectiveEndDate: data.effectiveEndDate || null,
        description: data.description || ''
      })
    } else {
      resetDoctorSettings()
    }
  } catch (error) {
    console.error('获取医生设置失败:', error)
    ElMessage.error('获取医生设置失败')
    resetDoctorSettings()
  }
}

const loadClinicSettings = async () => {
  if (!selectedClinicId.value) {
    resetClinicSettings()
    return
  }
  try {
    const response = await getClinicScheduleSettings(selectedClinicId.value)
    if (response && response.data) {
      const data = response.data
      // 如果返回空对象，表示继承全局设置
      if (Object.keys(data).length === 0) {
        resetClinicSettings()
        return
      }
      Object.assign(clinicSettings, {
        allowedSlotTypes: data.allowedSlotTypes || [],
        defaultTotalSlots: data.defaultTotalSlots || null,
        maxSlotsPerSchedule: data.maxSlotsPerSchedule || null,
        vipDailyLimitPerDoctor: data.vipDailyLimitPerDoctor || null,
        overrideStrategy: data.overrideStrategy || 'OVERRIDE',
        effectiveStartDate: data.effectiveStartDate || null,
        effectiveEndDate: data.effectiveEndDate || null
      })
    } else {
      resetClinicSettings()
    }
  } catch (error) {
    console.error('获取门诊设置失败:', error)
    ElMessage.error('获取门诊设置失败')
    resetClinicSettings()
  }
}

const resetDoctorSettings = () => {
  Object.assign(doctorSettings, {
    allowedSlotTypes: [],
    defaultTotalSlots: null,
    maxSlotsPerSchedule: null,
    vipDailyLimitPerDoctor: null,
    overrideStrategy: 'OVERRIDE',
    effectiveStartDate: null,
    effectiveEndDate: null,
    description: ''
  })
}

const resetClinicSettings = () => {
  Object.assign(clinicSettings, {
    allowedSlotTypes: [],
    defaultTotalSlots: null,
    maxSlotsPerSchedule: null,
    vipDailyLimitPerDoctor: null,
    overrideStrategy: 'OVERRIDE',
    effectiveStartDate: null,
    effectiveEndDate: null
  })
}

const handleSettingsTabChange = (tabName) => {
  if (tabName === 'global') {
    loadGlobalSettings()
  } else if (tabName === 'doctor') {
    if (selectedDoctorId.value) {
      loadDoctorSettings()
    }
  } else if (tabName === 'clinic') {
    if (selectedClinicId.value) {
      loadClinicSettings()
    }
  }
}

const handleSettingsDialogClose = () => {
  selectedDoctorId.value = null
  selectedClinicId.value = null
  resetDoctorSettings()
  resetClinicSettings()
  globalSettingsFormRef.value?.clearValidate()
  doctorSettingsFormRef.value?.clearValidate()
  clinicSettingsFormRef.value?.clearValidate()
}

// 候补队列相关方法
const loadWaitlistCounts = async (scheduleIds) => {
  if (!scheduleIds || scheduleIds.length === 0) {
    return
  }
  
  try {
    // 分批查询，每批50条，避免请求过大
    const batchSize = 50
    const batches = []
    for (let i = 0; i < scheduleIds.length; i += batchSize) {
      batches.push(scheduleIds.slice(i, i + batchSize))
    }
    
    // 并行查询所有批次
    const results = await Promise.allSettled(
      batches.map(batch => getWaitlistCounts(batch))
    )
    
    // 合并结果
    results.forEach((result, index) => {
      if (result.status === 'fulfilled' && result.value && result.value.data) {
        Object.assign(waitlistCounts.value, result.value.data)
      } else {
        console.error(`查询候补人数失败 (批次 ${index + 1}):`, result.reason)
      }
    })
  } catch (error) {
    console.error('批量查询候补人数失败:', error)
    // 不显示错误提示，避免干扰用户
  }
}

const getWaitlistCount = (schedule) => {
  if (!schedule || !schedule.id) return 0
  const count = waitlistCounts.value[schedule.id]
  return count !== undefined ? count : '-'
}

const getWaitlistCountTagType = (schedule) => {
  const count = getWaitlistCount(schedule)
  if (count === '-' || count === 0) return 'info'
  if (count >= 10) return 'danger'
  if (count >= 5) return 'warning'
  return 'success'
}

const canPopWaitlist = (schedule) => {
  if (!schedule || !schedule.id) return false
  const count = waitlistCounts.value[schedule.id]
  return count !== undefined && count > 0
}

const handlePopWaitlistClick = async (schedule) => {
  if (!schedule || !schedule.id) {
    ElMessage.warning('排班信息不完整')
    return
  }
  
  const count = waitlistCounts.value[schedule.id]
  if (!count || count === 0) {
    ElMessage.warning('该排班没有候补患者')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要弹出候补队首患者吗？当前候补人数：${count}`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    waitlistPopLoadingId.value = schedule.id
    const response = await popNextWaitlist(schedule.id)
    
    if (response && response.data) {
      const patientId = response.data
      ElMessage.success(`已弹出候补患者，患者ID: ${patientId}`)
      
      // 更新候补人数
      if (waitlistCounts.value[schedule.id] !== undefined) {
        waitlistCounts.value[schedule.id] = Math.max(0, waitlistCounts.value[schedule.id] - 1)
      }
      
      // 重新查询候补人数以确保数据准确
      await loadWaitlistCounts([schedule.id])
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error?.response?.data?.msg || error?.message || '弹出候补失败'
      ElMessage.error(errorMsg)
      console.error('弹出候补失败:', error)
    }
  } finally {
    waitlistPopLoadingId.value = null
  }
}

const saveSettings = async () => {
  try {
    settingsSubmitting.value = true
    
    if (settingsActiveTab.value === 'global') {
      // 验证全局设置表单
      if (globalSettingsFormRef.value) {
        await globalSettingsFormRef.value.validate()
      }
      await updateGlobalScheduleSettings(globalSettings)
      ElMessage.success('全局设置保存成功')
    } else if (settingsActiveTab.value === 'doctor') {
      if (!selectedDoctorId.value) {
        ElMessage.warning('请先选择医生')
        return
      }
      // 验证医生设置表单
      if (doctorSettingsFormRef.value) {
        await doctorSettingsFormRef.value.validate()
      }
      await updateDoctorScheduleSettings(selectedDoctorId.value, doctorSettings)
      ElMessage.success('医生设置保存成功')
    } else if (settingsActiveTab.value === 'clinic') {
      if (!selectedClinicId.value) {
        ElMessage.warning('请先选择门诊')
        return
      }
      // 验证门诊设置表单
      if (clinicSettingsFormRef.value) {
        await clinicSettingsFormRef.value.validate()
      }
      await updateClinicScheduleSettings(selectedClinicId.value, clinicSettings)
      ElMessage.success('门诊设置保存成功')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存设置失败')
      console.error('保存设置失败:', error)
    }
  } finally {
    settingsSubmitting.value = false
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  try {
    await Promise.all([
      loadDepartmentList(),
      loadClinicList(),
      loadDoctorList()
    ])
    // 先加载所有排班数据用于统计
    await loadAllSchedules()
    // 然后加载筛选后的排班列表
    await loadScheduleList()
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
})
</script>

<style scoped>
.schedule-management {
  height: 100vh;
  display: flex;
  flex-direction: column; /* 让头部独占一行，主体在其下 */
  background-color: #f5f7fa;
  padding: 20px; /* 与 QA 管理页一致的页面内边距 */
  min-height: 100vh; /* 与 QA 管理页保持整体高度 */
}

/* 主体左右分栏容器 */
.content-grid {
  display: flex;
  gap: 24px;
  align-items: flex-start; /* 确保顶部对齐 */
}

/* 筛选与操作区 */
.filter-section {
  margin-bottom: 20px;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  height: 57px;
  box-sizing: border-box;
  line-height: 1;
  margin: 0 -20px 12px -20px;
}

.filter-section h4 {
  margin: 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.filter-section-header .el-button {
  margin: 0;
}

.filter-item {
  margin-bottom: 16px;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.filter-item label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}


.calendar-header .el-button {
  margin: 0;
  vertical-align: middle;
  height: auto;
}

.calendar-header .el-radio-group {
  margin: 0;
  vertical-align: middle;
}

.calendar-header {
  padding: 18px 20px;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 57px;
  box-sizing: border-box;
  line-height: 1;
}

.calendar-header h3 {
  margin: 0;
  padding: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  vertical-align: middle;
}

.calendar-header .calendar-controls {
  display: flex;
  align-items: center;
  line-height: 1;
}

.date-navigation {
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1;
}

.current-period {
  font-size: 14px;
  color: #606266;
  line-height: 1;
  white-space: nowrap;
}

.calendar-legend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
}
.legend-group { display:flex; align-items:center; gap:12px; }
.legend-item { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 12px; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; }
.legend-dot.morning { background: #409eff; }
.legend-dot.afternoon { background: #67c23a; }
.legend-text { line-height: 1; }
.legend-chip { display:flex; align-items:center; gap:8px; padding:6px 10px; border-radius:12px; background:#ffffff; border:1px solid #ebeef5; }
.legend-chip .chip-left { width:6px; height:18px; border-radius:3px; }
.legend-chip.expert .chip-left { background:#e6a23c; }
.legend-chip.vip .chip-left { background:#f56c6c; }
.legend-chip .chip-text { font-size:12px; color:#606266; }

  .filter-content {
    flex: 1;
    padding: 0 20px 20px 20px;
    overflow-y: auto;
  }

  /* 左侧整体与卡片的顶部对齐调整，使其与右侧主视图对齐 */
  .filter-sidebar {
    display: flex;
    flex-direction: column;
    width: 260px; /* 左侧固定宽度，调整为更窄 */
  }

  .filter-card {
    margin-top: 0; /* 头部独立一行后，不再需要下移 */
  }

  .filter-card :deep(.el-card__body) {
    padding: 0;
  }

  /* 统一右侧卡片的 header 样式 */
  .main-content .el-card :deep(.el-card__header) {
    padding: 0;
    border-bottom: 1px solid #e4e7ed;
    height: auto;
    min-height: 0;
  }


.filter-group {
  margin-bottom: 24px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.quick-date-buttons {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.quick-date-buttons .el-button {
  flex: 1;
  font-size: 12px;
}

.view-switcher {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.view-switcher .el-button {
  flex: 1;
}

.batch-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.batch-actions .el-button {
  width: 100%;
  margin: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  box-sizing: border-box;
}

.batch-actions .el-button .el-icon {
  margin-right: 6px;
  font-size: 16px;
  flex-shrink: 0;
}

.batch-actions .el-button.is-loading {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.export-dialog-content {
  display: flex;
  gap: 12px;
  align-items: center;
}
.export-tip {
  color: #909399;
  font-size: 13px;
}


.statistics-panel {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin-top: 16px;
}

.statistics-panel h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  color: #606266;
  font-size: 13px;
}

.stat-value {
  color: #409eff;
  font-weight: 600;
  font-size: 14px;
}

/* 主展示区 */
.main-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.main-title {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

  .main-content {
    flex: 1;
    padding: 24px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  /* 顶部信息卡片 */
  .header-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24px;
    background: #ffffff;
    border-radius: 8px; /* 与 QA 管理页卡片统一 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 与 QA 管理页卡片统一 */
    margin-bottom: 20px;
    /* 与 QA 管理页 page-header 尺寸一致，不设置最小高度 */
  }

  /* 统计卡片样式 */
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    height: 120px;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
  }

  .stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }

  .stat-content {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 20px;
  }

  .stat-icon {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    font-size: 28px;
    color: white;
  }

  .stat-icon.total {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .stat-icon.today {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  .stat-icon.week {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  .stat-info {
    flex: 1;
  }

  .stat-number {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    line-height: 1;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }

  .header-left {
    display: flex;
    flex-direction: column;
    text-align: left; /* 确保文字左对齐 */
  }

  .header-title {
    margin: 0 0 8px 0; /* 与 QA 页标题一致 */
    font-size: 24px; /* 与 QA 页标题一致 */
    line-height: 1.2;
    font-weight: 600;
    color: #303133; /* 与 QA 页标题色一致 */
    text-align: left; /* 标题左对齐 */
  }

  .header-subtitle {
    margin: 0; /* 与 QA 页副标题一致 */
    font-size: 14px; /* 与 QA 页副标题一致 */
    color: #606266;
    text-align: left; /* 副标题左对齐 */
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .soft-button {
    background: #ffffff;
    border: 1px solid #e4e7ed;
    color: #303133;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  }

  .soft-button:hover {
    background: #f7f8fa;
    border-color: #dcdfe6;
  }

  @media (max-width: 768px) {
    .header-card {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
    .header-right {
      width: 100%;
      justify-content: flex-start;
      gap: 10px;
    }
  }

/* 日历视图 */
.calendar-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 20px;
}

/* 统一的日历网格视图 */
.calendar-grid-view {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.calendar-header-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e4e7ed;
  border: 1px solid #e4e7ed;
  border-radius: 8px 8px 0 0;
  overflow: hidden;
}

.day-header-cell {
  background: #f5f7fa;
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.calendar-body-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e4e7ed;
  border: 1px solid #e4e7ed;
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.calendar-body-grid.month-grid {
  min-height: 500px;
}

.calendar-day-cell {
  background: white;
  min-height: 120px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.calendar-day-cell:hover {
  background: #f5f7fa;
}

.calendar-day-cell.today {
  background: #ecf5ff;
}

.calendar-day-cell.today .day-number {
  color: #409eff;
  font-weight: 700;
}

.calendar-day-cell.other-month {
  background: #fafafa;
}

.calendar-day-cell.other-month .day-number {
  color: #c0c4cc;
}

.calendar-day-cell.in-date-range {
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
}

.calendar-day-cell.in-date-range.today {
  background-color: #fffbe6;
  border: 2px solid #409eff;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1;
}

.day-schedules {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  min-height: 0;
}

.schedule-badge {
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.3;
  margin: 0 auto 4px auto;
  width: calc(100% - 12px);
  border-left: 3px solid transparent;
  text-align: left;
}

.schedule-badge .doctor-name {
  font-weight: 600;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
}

.schedule-badge .slots-info {
  font-size: 11px;
  opacity: 0.9;
}

.schedule-badge .waitlist-info {
  font-size: 11px;
  color: #f56c6c;
  font-weight: 600;
}

.slots-info-row .waitlist-info {
  margin-left: 0;
}

.slots-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  width: 100%;
}

.slots-info-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  width: 100%;
}

.slots-info-row .slots-info,
.slots-info-row .waitlist-info {
  text-align: left;
  width: 100%;
  margin: 0 !important;
  padding: 0;
  display: block;
  line-height: 1.4;
}

.slots-buttons-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  width: 100%;
}

.slots-buttons-row .el-button {
  align-self: flex-start;
  margin-left: 0 !important;
  margin-right: 0 !important;
  margin-top: 0 !important;
}

.slots-buttons-row .el-button:not(:first-child) {
  margin-top: 4px !important;
}

/* 徽章悬停时显示加号按钮 */
.schedule-badge .add-slots-btn { display: none; }
.schedule-badge:hover .add-slots-btn { display: inline-flex; }
.schedule-badge .pop-waitlist-btn { display: none; }
.schedule-badge:hover .pop-waitlist-btn { display: inline-flex; }

/* 缩小加号按钮尺寸以减少视觉占用 */
.add-slots-btn {
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  font-size: 12px;
  border-radius: 10px;
}
.pop-waitlist-btn {
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  font-size: 10px;
  border-radius: 10px;
}

.slots-buttons-row .add-slots-btn,
.slots-buttons-row .pop-waitlist-btn {
  margin-left: 0 !important;
  margin-right: 0 !important;
  width: auto;
}
/* 让 Alert 描述支持换行 */
::v-deep(.el-alert__description) {
  white-space: pre-line;
}

/* 加号弹窗提示卡片样式 */
.add-slots-tip-card {
  background: #fff7e6;
  border: 1px solid #faad14;
  border-radius: 8px;
  padding: 12px 16px;
  color: #a76a1d;
}
.add-slots-tip-card .tip-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-weight: 600;
  color: #d48806;
  margin-bottom: 8px;
}
.add-slots-tip-card .tip-header .el-icon {
  margin-right: 6px;
}
.add-slots-tip-card .tip-title {
  font-size: 14px;
}
.add-slots-tip-card .tip-list {
  margin: 0;
  padding-left: 20px;
  text-align: left;
  font-size: 12px;
}
.add-slots-tip-card .tip-list li {
  line-height: 1.8;
}

.schedule-badge:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.schedule-badge.badge-morning {
  background: #409eff;
}

.schedule-badge.badge-afternoon {
  background: #67c23a;
}

/* 专家/特需醒目标识与左侧色条 */
.schedule-badge.expert-badge {
  border-left-color: #e6a23c;
}
.schedule-badge.vip-badge {
  border-left-color: #f56c6c;
}

.badge-top {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.badge-label {
  font-size: 10px;
  font-weight: 700;
  padding: 0 6px;
  border-radius: 10px;
  line-height: 16px;
  background: rgba(255, 255, 255, 0.9);
}
.badge-top .doctor-name { margin-top: 2px; }
.badge-expert {
  color: #a76a1d;
  border: 1px solid #e6a23c;
}
.badge-vip {
  color: #c45656;
  border: 1px solid #f56c6c;
}
.badge-normal {
  color: #606266;
  border: 1px solid #909399;
}

.schedule-badge.badge-more {
  background: #909399;
  font-weight: 600;
  text-align: center;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.schedule-badge.badge-more:hover {
  background: #606266;
}

.drawer-schedules {
  padding: 10px 0;
}

.drawer-schedule-item {
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e4e7ed;
}

.drawer-schedule-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.drawer-schedule-item.badge-morning {
  background: #f0f9ff;
  border-color: #409eff;
}

.drawer-schedule-item.badge-afternoon {
  background: #fff7e6;
  border-color: #67c23a;
}

.schedule-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.schedule-item-header .doctor-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.schedule-item-header .slot-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f0f2f5;
  color: #606266;
}

.schedule-item-info {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.schedule-item-info .waitlist-info {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
}

.empty-schedule {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.empty-schedule:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.add-schedule-icon {
  color: #909399;
  font-size: 20px;
}

.empty-schedule:hover .add-schedule-icon {
  color: #409eff;
}

.calendar-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.period-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  min-width: 200px;
  text-align: center;
}

.calendar-mode-switcher {
  display: flex;
  gap: 8px;
}

.view-mode-switcher {
  display: flex;
  gap: 8px;
}

.calendar-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 周视图 */
.week-view {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.week-header {
  display: grid;
  grid-template-columns: 150px repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.week-header-cell {
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
}

.week-header-cell:last-child {
  border-right: none;
}

.week-header-cell.today {
  background: #409eff;
  color: white;
}

.week-body {
  flex: 1;
  overflow-y: auto;
}

.week-row {
  display: grid;
  grid-template-columns: 150px repeat(7, 1fr);
  border-bottom: 1px solid #e4e7ed;
  min-height: 80px;
}

.week-row:last-child {
  border-bottom: none;
}

.week-row-header {
  padding: 12px;
  background: #fafafa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.entity-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.entity-info {
  font-size: 12px;
  color: #909399;
}

.week-cell {
  padding: 8px;
  border-right: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.2s;
  min-height: 80px;
}

.week-cell:last-child {
  border-right: none;
}

.week-cell:hover {
  background-color: #f5f7fa;
}

.week-cell.today {
  background-color: #ecf5ff;
}

/* 月视图 */
.month-view {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.month-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: auto repeat(6, 1fr);
  gap: 1px;
  background: #e4e7ed;
}

.month-header-cell {
  background: #f5f7fa;
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #606266;
}

.month-cell {
  background: white;
  padding: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  min-height: 100px;
  display: flex;
  flex-direction: column;
}

.month-cell:hover {
  background-color: #f5f7fa;
}

.month-cell.today {
  background-color: #ecf5ff;
}

.month-cell.other-month {
  background-color: #fafafa;
  color: #c0c4cc;
}

.month-cell-date {
  font-weight: 600;
  margin-bottom: 4px;
}

.month-cell.today .month-cell-date {
  color: #409eff;
}

/* 排班项目 */
.schedule-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.schedule-item {
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.schedule-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.schedule-item.morning-slot {
  background-color: #e7f5e7;
  border-left-color: #67c23a;
  color: #529b2e;
}

.schedule-item.afternoon-slot {
  background-color: #fdf6ec;
  border-left-color: #e6a23c;
  color: #b88230;
}

.schedule-item.evening-slot {
  background-color: #edf2fc;
  border-left-color: #909399;
  color: #73767a;
}

.schedule-item.expert-slot {
  border-left-color: #e6a23c;
  font-weight: 600;
}

.schedule-item.vip-slot {
  border-left-color: #f56c6c;
  font-weight: 600;
}

/* 专家号在日历视图中颜色更深，区分于普通上午/下午 */
.schedule-item.morning-slot.expert-slot {
  background-color: #d9ecd9; /* 深一点的绿色背景 */
  color: #3f7f3a; /* 更深的文字颜色 */
}

.schedule-item.afternoon-slot.expert-slot {
  background-color: #fae7d3; /* 深一点的橙色背景 */
  color: #a76a1d; /* 更深的文字颜色 */
}

.schedule-item.low-slots {
  background-color: #fef0f0;
  border-left-color: #f56c6c;
  color: #c45656;
}

.schedule-doctor {
  font-weight: 600;
  margin-bottom: 2px;
}

.schedule-time {
  color: #909399;
  font-size: 11px;
}

.schedule-slots {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.slots-info {
  font-size: 11px;
  color: #606266;
}

.slots-progress {
  width: 40px;
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.slots-progress-bar {
  height: 100%;
  transition: width 0.3s;
}

/* 列表视图 */
.list-view {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.list-actions {
  display: flex;
  gap: 8px;
}

.list-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-table {
  flex: 1;
}

.list-pagination {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: center;
}

/* 表格样式 */
.el-table .cell {
  padding: 0 8px;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doctor-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.doctor-details {
  flex: 1;
}

.doctor-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.doctor-department {
  font-size: 12px;
  color: #909399;
}

.schedule-date {
  font-weight: 600;
  color: #303133;
}

.schedule-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-slot-tag {
  font-size: 12px;
}

.slot-type-tag {
  font-size: 12px;
}

.slots-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.slots-numbers {
  font-size: 12px;
  color: #606266;
}

.slots-progress-table {
  width: 60px;
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
}

.slots-progress-bar-table {
  height: 100%;
  transition: width 0.3s;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

/* 对话框样式 */
.dialog-form {
  padding: 0 20px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .el-form-item {
  flex: 1;
}

.batch-form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.batch-form-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-sidebar {
    width: 240px;
  }
  
  .week-header-cell,
  .week-row-header {
    padding: 8px;
  }
  
  .week-cell {
    padding: 6px;
  }
}

@media (max-width: 768px) {
  .schedule-management {
    flex-direction: column;
    height: auto;
  }
  
  .filter-sidebar {
    width: 100%;
  }
  
  .filter-content {
    max-height: 300px;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .calendar-controls {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .calendar-nav {
    justify-content: center;
  }
  
  .week-header {
    grid-template-columns: 100px repeat(7, 1fr);
  }
  
  .week-row {
    grid-template-columns: 100px repeat(7, 1fr);
  }
  
  .week-row-header {
    padding: 8px;
  }
  
  .entity-name {
    font-size: 12px;
  }
  
  .entity-info {
    font-size: 10px;
  }
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}

/* 滚动条样式 */
.filter-content::-webkit-scrollbar,
.week-body::-webkit-scrollbar {
  width: 6px;
}

.filter-content::-webkit-scrollbar-track,
.week-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.filter-content::-webkit-scrollbar-thumb,
.week-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.filter-content::-webkit-scrollbar-thumb:hover,
.week-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 加载状态 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state-text {
  font-size: 14px;
  margin-bottom: 16px;
}
</style>
<template>
  <div class="data-table">
    <!-- 表格工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left">
          <el-button
            v-if="showRefresh"
            type="primary"
            :icon="Refresh"
            @click="handleRefresh"
          >
            刷新
          </el-button>
        </slot>
      </div>
      
      <div class="toolbar-right">
        <slot name="toolbar-right">
          <!-- 密度设置 -->
          <el-tooltip content="密度" placement="top">
            <el-dropdown @command="handleDensityChange">
              <el-button :icon="Operation" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="large">宽松</el-dropdown-item>
                  <el-dropdown-item command="default">默认</el-dropdown-item>
                  <el-dropdown-item command="small">紧凑</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>
          
          <!-- 列设置 -->
          <el-tooltip content="列设置" placement="top">
            <el-popover
              placement="bottom-end"
              :width="200"
              trigger="click"
            >
              <template #reference>
                <el-button :icon="Setting" circle />
              </template>
              
              <div class="column-setting">
                <el-checkbox
                  v-model="checkAll"
                  :indeterminate="isIndeterminate"
                  @change="handleCheckAllChange"
                >
                  全选
                </el-checkbox>
                <el-divider />
                <el-checkbox-group v-model="checkedColumns" @change="handleCheckedColumnsChange">
                  <div
                    v-for="column in settableColumns"
                    :key="column.prop"
                    class="column-item"
                  >
                    <el-checkbox :label="column.prop">
                      {{ column.label }}
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>
            </el-popover>
          </el-tooltip>
        </slot>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :size="tableSize"
      :stripe="stripe"
      :border="border"
      :height="height"
      :max-height="maxHeight"
      :row-key="rowKey"
      :default-expand-all="defaultExpandAll"
      :tree-props="treeProps"
      :show-summary="showSummary"
      :summary-method="summaryMethod"
      :span-method="spanMethod"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDblclick"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        :selectable="selectable"
        fixed="left"
      />
      
      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        :index="indexMethod"
        fixed="left"
      />

      <!-- 数据列 -->
      <template v-for="column in visibleColumns" :key="column.prop">
        <!-- 普通列 -->
        <el-table-column
          v-if="!column.children"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :fixed="column.fixed"
          :sortable="column.sortable"
          :sort-method="column.sortMethod"
          :sort-by="column.sortBy"
          :sort-orders="column.sortOrders"
          :resizable="column.resizable"
          :formatter="column.formatter"
          :show-overflow-tooltip="column.showOverflowTooltip !== false"
          :align="column.align || 'left'"
          :header-align="column.headerAlign || column.align || 'left'"
          :class-name="column.className"
          :label-class-name="column.labelClassName"
        >
          <template #default="scope">
            <slot
              :name="column.prop"
              :row="scope.row"
              :column="scope.column"
              :$index="scope.$index"
            >
              <!-- 自定义渲染 -->
              <component
                v-if="column.render"
                :is="column.render"
                :row="scope.row"
                :column="scope.column"
                :index="scope.$index"
              />
              
              <!-- 默认显示 -->
              <span v-else>{{ getCellValue(scope.row, column) }}</span>
            </slot>
          </template>
          
          <template v-if="column.headerSlot" #header="scope">
            <slot
              :name="column.headerSlot"
              :column="scope.column"
              :$index="scope.$index"
            />
          </template>
        </el-table-column>

        <!-- 多级表头 -->
        <el-table-column
          v-else
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :fixed="column.fixed"
          :align="column.align || 'center'"
          :header-align="column.headerAlign || column.align || 'center'"
        >
          <template v-for="child in column.children" :key="child.prop">
            <el-table-column
              :prop="child.prop"
              :label="child.label"
              :width="child.width"
              :min-width="child.minWidth"
              :sortable="child.sortable"
              :formatter="child.formatter"
              :show-overflow-tooltip="child.showOverflowTooltip !== false"
              :align="child.align || 'left'"
              :header-align="child.headerAlign || child.align || 'left'"
            >
              <template #default="scope">
                <slot
                  :name="child.prop"
                  :row="scope.row"
                  :column="scope.column"
                  :$index="scope.$index"
                >
                  <component
                    v-if="child.render"
                    :is="child.render"
                    :row="scope.row"
                    :column="scope.column"
                    :index="scope.$index"
                  />
                  <span v-else>{{ getCellValue(scope.row, child) }}</span>
                </slot>
              </template>
            </el-table-column>
          </template>
        </el-table-column>
      </template>

      <!-- 操作列 -->
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionWidth"
        :min-width="actionMinWidth"
        :fixed="actionFixed"
        :align="actionAlign"
      >
        <template #default="scope">
          <slot
            name="actions"
            :row="scope.row"
            :column="scope.column"
            :$index="scope.$index"
          />
        </template>
      </el-table-column>

      <!-- 空状态 -->
      <template #empty>
        <slot name="empty">
          <el-empty :description="emptyText" />
        </slot>
      </template>
    </el-table>

    <!-- 分页 -->
    <div v-if="showPagination" class="table-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        :background="paginationBackground"
        :small="paginationSmall"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Refresh, Operation, Setting } from '@element-plus/icons-vue'

const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 表格列配置
  columns: {
    type: Array,
    default: () => []
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 表格尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 表格高度
  height: {
    type: [String, Number],
    default: undefined
  },
  // 表格最大高度
  maxHeight: {
    type: [String, Number],
    default: undefined
  },
  // 行数据的 Key
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 是否默认展开所有行
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  // 树形数据配置
  treeProps: {
    type: Object,
    default: () => ({ children: 'children', hasChildren: 'hasChildren' })
  },
  // 是否显示合计行
  showSummary: {
    type: Boolean,
    default: false
  },
  // 合计方法
  summaryMethod: {
    type: Function,
    default: undefined
  },
  // 合并行或列的方法
  spanMethod: {
    type: Function,
    default: undefined
  },
  // 是否显示选择列
  showSelection: {
    type: Boolean,
    default: false
  },
  // 选择函数
  selectable: {
    type: Function,
    default: undefined
  },
  // 是否显示序号列
  showIndex: {
    type: Boolean,
    default: false
  },
  // 序号方法
  indexMethod: {
    type: Function,
    default: undefined
  },
  // 是否显示操作列
  showActions: {
    type: Boolean,
    default: false
  },
  // 操作列宽度
  actionWidth: {
    type: [String, Number],
    default: undefined
  },
  // 操作列最小宽度
  actionMinWidth: {
    type: [String, Number],
    default: 120
  },
  // 操作列固定
  actionFixed: {
    type: [String, Boolean],
    default: 'right'
  },
  // 操作列对齐
  actionAlign: {
    type: String,
    default: 'center'
  },
  // 是否显示工具栏
  showToolbar: {
    type: Boolean,
    default: true
  },
  // 是否显示刷新按钮
  showRefresh: {
    type: Boolean,
    default: true
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 当前页
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页条数
  pageSize: {
    type: Number,
    default: 20
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 每页显示个数选择器的选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页布局
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 分页背景
  paginationBackground: {
    type: Boolean,
    default: true
  },
  // 小型分页
  paginationSmall: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'refresh',
  'selection-change',
  'sort-change',
  'row-click',
  'row-dblclick',
  'size-change',
  'current-change'
])

const tableRef = ref()
const tableSize = ref(props.size)

// 列设置相关
const checkedColumns = ref([])
const checkAll = ref(true)
const isIndeterminate = ref(false)

// 可设置的列（排除操作列等）
const settableColumns = computed(() => {
  return props.columns.filter(column => !column.fixed && column.prop)
})

// 可见的列
const visibleColumns = computed(() => {
  if (checkedColumns.value.length === 0) {
    return props.columns
  }
  return props.columns.filter(column => {
    if (!column.prop) return true
    return checkedColumns.value.includes(column.prop)
  })
})

// 初始化列设置
const initColumnSetting = () => {
  checkedColumns.value = settableColumns.value.map(column => column.prop)
}

// 全选变化
const handleCheckAllChange = (val) => {
  checkedColumns.value = val ? settableColumns.value.map(column => column.prop) : []
  isIndeterminate.value = false
}

// 选中列变化
const handleCheckedColumnsChange = (value) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === settableColumns.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < settableColumns.value.length
}

// 密度变化
const handleDensityChange = (command) => {
  tableSize.value = command
}

// 获取单元格值
const getCellValue = (row, column) => {
  if (column.formatter) {
    return column.formatter(row, column, row[column.prop], 0)
  }
  return row[column.prop]
}

// 刷新
const handleRefresh = () => {
  emit('refresh')
}

// 选择变化
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

// 排序变化
const handleSortChange = (sortInfo) => {
  emit('sort-change', sortInfo)
}

// 行点击
const handleRowClick = (row, column, event) => {
  emit('row-click', row, column, event)
}

// 行双击
const handleRowDblclick = (row, column, event) => {
  emit('row-dblclick', row, column, event)
}

// 每页条数变化
const handleSizeChange = (size) => {
  emit('size-change', size)
}

// 当前页变化
const handleCurrentChange = (page) => {
  emit('current-change', page)
}

// 暴露方法
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
  toggleRowSelection: (row, selected) => tableRef.value?.toggleRowSelection(row, selected),
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  toggleRowExpansion: (row, expanded) => tableRef.value?.toggleRowExpansion(row, expanded),
  setCurrentRow: (row) => tableRef.value?.setCurrentRow(row),
  clearSort: () => tableRef.value?.clearSort(),
  clearFilter: (columnKey) => tableRef.value?.clearFilter(columnKey),
  doLayout: () => tableRef.value?.doLayout(),
  sort: (prop, order) => tableRef.value?.sort(prop, order)
})

// 初始化
initColumnSetting()

// 监听列变化
watch(
  () => props.columns,
  () => {
    initColumnSetting()
  },
  { deep: true }
)
</script>

<style scoped>
.data-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  flex: 1;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.column-setting {
  max-height: 300px;
  overflow-y: auto;
}

.column-item {
  padding: 4px 0;
}

.table-pagination {
  padding: 16px;
  text-align: right;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-table) {
  border-radius: 0;
}

:deep(.el-table__header) {
  background-color: #fafafa;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f5f5f5;
}

:deep(.el-table--border) {
  border: 1px solid #ebeef5;
}

:deep(.el-table--border::after) {
  display: none;
}

:deep(.el-table__empty-block) {
  background-color: white;
}
</style>
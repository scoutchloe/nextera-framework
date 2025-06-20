<template>
  <div class="data-table">
    <!-- 表格工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left">
          <el-button 
            v-if="showAdd" 
            type="primary" 
            :icon="Plus" 
            @click="$emit('add')"
          >
            {{ addText }}
          </el-button>
          <el-button 
            v-if="showBatchDelete && selectedRows.length > 0" 
            type="danger" 
            :icon="Delete" 
            @click="$emit('batchDelete', selectedRows)"
          >
            批量删除
          </el-button>
        </slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right">
          <el-button :icon="Refresh" @click="$emit('refresh')">
            刷新
          </el-button>
          <el-dropdown @command="handleDensity">
            <el-button :icon="Setting">
              密度<el-icon><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="default">默认</el-dropdown-item>
                <el-dropdown-item command="medium">中等</el-dropdown-item>
                <el-dropdown-item command="small">紧凑</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </slot>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="data"
        :stripe="stripe"
        :border="border"
        :size="tableSize"
        :row-key="rowKey"
        :default-expand-all="defaultExpandAll"
        :tree-props="treeProps"
        :selection="selection"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @filter-change="handleFilterChange"
        v-bind="$attrs"
      >
        <!-- 选择列 -->
        <el-table-column 
          v-if="selection" 
          type="selection" 
          width="55" 
          :selectable="selectable"
        />
        
        <!-- 序号列 -->
        <el-table-column 
          v-if="showIndex" 
          type="index" 
          label="序号" 
          width="60"
          :index="indexMethod"
        />

        <!-- 动态列 -->
        <template v-for="column in columns" :key="column.prop">
          <el-table-column
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
            :sortable="column.sortable"
            :show-overflow-tooltip="column.showOverflowTooltip !== false"
            :align="column.align || 'left'"
            :header-align="column.headerAlign"
            :formatter="column.formatter"
            :filters="column.filters"
            :filter-method="column.filterMethod"
          >
            <template v-if="column.slot" #default="scope">
              <slot :name="column.slot" v-bind="scope" />
            </template>
            <template v-else-if="column.type === 'tag'" #default="{ row }">
              <el-tag 
                :type="getTagType(row[column.prop], column.tagMap)" 
                :size="column.tagSize || 'small'"
              >
                {{ getTagText(row[column.prop], column.tagMap) }}
              </el-tag>
            </template>
            <template v-else-if="column.type === 'switch'" #default="{ row }">
              <el-switch
                v-model="row[column.prop]"
                :active-value="column.activeValue || true"
                :inactive-value="column.inactiveValue || false"
                :disabled="column.disabled"
                @change="(val: any) => $emit('switchChange', row, column.prop, val)"
              />
            </template>
            <template v-else-if="column.type === 'image'" #default="{ row }">
              <el-image
                :src="row[column.prop]"
                :preview-src-list="[row[column.prop]]"
                :style="{ width: column.imageWidth || '50px', height: column.imageHeight || '50px' }"
                fit="cover"
              />
            </template>
            <template v-else-if="column.type === 'link'" #default="{ row }">
              <el-link 
                :type="column.linkType || 'primary'" 
                @click="$emit('linkClick', row, column.prop)"
              >
                {{ row[column.prop] }}
              </el-link>
            </template>
            <template v-else-if="column.type === 'date'" #default="{ row }">
              {{ formatDate(row[column.prop], column.dateFormat) }}
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
          align="center"
        >
          <template #default="scope">
            <slot name="actions" v-bind="scope">
              <el-button
                v-if="showEdit"
                size="small"
                type="primary"
                :icon="Edit"
                @click="$emit('edit', scope.row, scope.$index)"
              >
                编辑
              </el-button>
              <el-button
                v-if="showDelete"
                size="small"
                type="danger"
                :icon="Delete"
                @click="$emit('delete', scope.row, scope.$index)"
              >
                删除
              </el-button>
            </slot>
          </template>
        </el-table-column>

        <!-- 空数据 -->
        <template #empty>
          <div class="table-empty">
            <slot name="empty">
              <el-empty :description="emptyText" />
            </slot>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div v-if="showPagination" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="pageSizes"
          :layout="paginationLayout"
          :background="paginationBackground"
          @size-change="handlePageSizeChange"
          @current-change="handleCurrentPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { ElTable } from 'element-plus'
import { Plus, Delete, Refresh, Setting, Edit, ArrowDown } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 定义接口
interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | string
  sortable?: boolean | string
  showOverflowTooltip?: boolean
  align?: string
  headerAlign?: string
  formatter?: Function
  filters?: Array<{ text: string; value: any }>
  filterMethod?: Function
  slot?: string
  type?: 'tag' | 'switch' | 'image' | 'link' | 'date'
  tagMap?: Record<string, { type: string; text: string }>
  tagSize?: string
  activeValue?: any
  inactiveValue?: any
  disabled?: boolean
  imageWidth?: string
  imageHeight?: string
  linkType?: string
  dateFormat?: string
}

// Props
interface Props {
  // 表格数据
  data: any[]
  columns: TableColumn[]
  loading?: boolean
  stripe?: boolean
  border?: boolean
  rowKey?: string
  defaultExpandAll?: boolean
  treeProps?: object
  
  // 选择
  selection?: boolean
  selectable?: (row: any, index: number) => boolean
  
  // 序号
  showIndex?: boolean
  indexMethod?: (index: number) => number
  
  // 工具栏
  showToolbar?: boolean
  showAdd?: boolean
  addText?: string
  showBatchDelete?: boolean
  
  // 操作列
  showActions?: boolean
  actionWidth?: string | number
  actionMinWidth?: string | number
  actionFixed?: boolean | string
  showEdit?: boolean
  showDelete?: boolean
  
  // 分页
  showPagination?: boolean
  total?: number
  currentPage?: number
  pageSize?: number
  pageSizes?: number[]
  paginationLayout?: string
  paginationBackground?: boolean
  
  // 其他
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  stripe: true,
  border: true,
  selection: false,
  showIndex: false,
  showToolbar: true,
  showAdd: true,
  addText: '新增',
  showBatchDelete: true,
  showActions: true,
  actionWidth: 200,
  showEdit: true,
  showDelete: true,
  showPagination: true,
  total: 0,
  currentPage: 1,
  pageSize: 20,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  paginationBackground: true,
  emptyText: '暂无数据'
})

// Emits
const emit = defineEmits<{
  add: []
  batchDelete: [rows: any[]]
  refresh: []
  edit: [row: any, index: number]
  delete: [row: any, index: number]
  switchChange: [row: any, prop: string, value: any]
  linkClick: [row: any, prop: string]
  selectionChange: [selection: any[]]
  sortChange: [sort: { column: any; prop: string; order: string }]
  filterChange: [filters: Record<string, any[]>]
  pageChange: [page: number, size: number]
}>()

// 响应式数据
const tableRef = ref<InstanceType<typeof ElTable>>()
const selectedRows = ref<any[]>([])
const tableSize = ref<'default' | 'large' | 'small'>('default')

// 计算属性
const currentPage = computed({
  get: () => props.currentPage,
  set: (val) => emit('pageChange', val, props.pageSize)
})

const pageSize = computed({
  get: () => props.pageSize,
  set: (val) => emit('pageChange', props.currentPage, val)
})

// 方法
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
  emit('selectionChange', selection)
}

const handleSortChange = (sort: { column: any; prop: string; order: string }) => {
  emit('sortChange', sort)
}

const handleFilterChange = (filters: Record<string, any[]>) => {
  emit('filterChange', filters)
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
}

const handleCurrentPageChange = (page: number) => {
  currentPage.value = page
}

const handleDensity = (command: string) => {
  tableSize.value = command as 'default' | 'large' | 'small'
}

const getTagType = (value: any, tagMap?: Record<string, { type: string; text: string }>) => {
  return tagMap?.[value]?.type || 'info'
}

const getTagText = (value: any, tagMap?: Record<string, { type: string; text: string }>) => {
  return tagMap?.[value]?.text || value
}

const formatDate = (date: string | Date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return '-'
  return dayjs(date).format(format)
}

// 清空选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
}

// 切换行选择状态
const toggleRowSelection = (row: any, selected?: boolean) => {
  tableRef.value?.toggleRowSelection(row, selected)
}

// 暴露方法
defineExpose({
  clearSelection,
  toggleRowSelection,
  tableRef
})
</script>

<style lang="scss" scoped>
.data-table {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);

    .toolbar-left,
    .toolbar-right {
      display: flex;
      gap: 12px;
    }
  }

  .table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
    overflow: hidden;

    :deep(.el-table) {
      --el-table-border-color: var(--border-color);
      --el-table-bg-color: var(--bg-primary);
      --el-table-tr-bg-color: var(--bg-primary);

      .el-table__header-wrapper {
        .el-table__header {
          .el-table__cell {
            background: var(--bg-secondary);
            color: var(--text-primary);
            font-weight: 600;
          }
        }
      }

      .el-table__body-wrapper {
        .el-table__row {
          &:hover > .el-table__cell {
            background: var(--bg-secondary);
          }
        }

        .el-table__cell {
          color: var(--text-primary);
        }
      }
    }

    .table-empty {
      padding: 40px 0;
    }

    .pagination-container {
      padding: 16px 24px;
      display: flex;
      justify-content: flex-end;
      border-top: 1px solid var(--border-color);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .data-table {
    .table-toolbar {
      flex-direction: column;
      gap: 12px;

      .toolbar-left,
      .toolbar-right {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }
}
</style> 
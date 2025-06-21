<template>
  <div class="category-management">
    <div class="page-header">
      <div class="header-title">
        <h2>{{ t('article.category.title') }}</h2>
        <p>{{ t('article.category.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button 
          v-permission="'article:category:add'"
          type="primary" 
          :icon="Plus" 
          @click="handleAdd"
        >
          {{ t('article.category.addCategory') }}
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('article.category.nameLabel')">
          <el-input
            v-model="searchForm.name"
            :placeholder="t('article.category.namePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('article.category.statusLabel')">
          <el-select v-model="searchForm.status" :placeholder="t('article.category.statusPlaceholder')" clearable>
            <el-option :label="t('common.enable')" :value="1" />
            <el-option :label="t('common.disable')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            {{ t('common.search') }}
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            {{ t('common.reset') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        row-key="id"
        stripe
      >
        <el-table-column prop="name" :label="t('article.category.categoryName')" width="200" />
        <el-table-column prop="description" :label="t('article.category.description')" min-width="300" show-overflow-tooltip />
        <el-table-column prop="parentName" :label="t('article.category.parentName')" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.parentName" type="info" size="small">{{ row.parentName }}</el-tag>
            <span v-else class="text-muted">{{ t('article.category.topLevel') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" :label="t('article.category.sortOrder')" width="100" align="center" />
        <el-table-column prop="status" :label="t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? t('common.enable') : t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('common.createTime')" width="180" />
        <el-table-column :label="t('common.operation')" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'article:category:edit'"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >
              {{ t('article.category.edit') }}
            </el-button>
            <el-button
              v-permission="'article:category:edit'"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? t('article.category.disable') : t('article.category.enable') }}
            </el-button>
            <el-button
              v-permission="'article:category:delete'"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              {{ t('article.category.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { categoryApi } from '@/api/system'

const { t } = useI18n()

// 响应式数据
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref<any[]>([])

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: ''
  })
  handleSearch()
}

const loadTableData = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    }
    
    const response = await categoryApi.getCategoryList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  ElMessage.info('新增分类功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑分类 "${row.name}" 功能开发中...`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${row.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await categoryApi.deleteCategory(row.id)
    
    ElMessage.success('删除成功')
    loadTableData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

const handleStatusChange = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    
    await categoryApi.updateCategoryStatus(row.id, newStatus)
    
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('更新分类状态失败:', error)
    ElMessage.error('更新分类状态失败')
  }
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

onMounted(() => {
  loadTableData()
})
</script>

<style lang="scss" scoped>
.category-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);
    
    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: var(--text-primary);
        font-size: 24px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
  }

  .search-bar {
    background: var(--bg-primary);
    padding: 20px;
    border-radius: var(--radius-md);
    margin-bottom: 16px;
    border: 1px solid var(--border-color);
    
    .search-form {
      margin: 0;
    }
  }

  .table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
    overflow: hidden;
    
    .el-table {
      border: none;
    }
    
    .text-muted {
      color: var(--text-tertiary);
      font-size: 12px;
    }
    
    .pagination-container {
      padding: 16px 20px;
      border-top: 1px solid var(--border-color);
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 
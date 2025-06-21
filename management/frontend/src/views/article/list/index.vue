<template>
  <div class="article-list">
    <div class="page-header">
      <div class="header-title">
        <h2>{{ $t('article.list.title') }}</h2>
        <p>{{ $t('article.list.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button 
          v-permission="'article:add'"
          type="primary" 
          :icon="Plus" 
          @click="handleAdd"
        >
          {{ $t('article.list.publishArticle') }}
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('article.list.titleLabel')">
          <el-input
            v-model="searchForm.title"
            :placeholder="$t('article.list.titlePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="$t('article.list.authorLabel')">
          <el-input
            v-model="searchForm.authorName"
            :placeholder="$t('article.list.authorPlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="$t('article.list.categoryLabel')">
          <el-select v-model="searchForm.categoryId" :placeholder="$t('article.list.categoryPlaceholder')" clearable>
            <el-option 
              v-for="category in categoryOptions" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('article.list.statusLabel')">
          <el-select v-model="searchForm.status" :placeholder="$t('article.list.statusPlaceholder')" clearable>
            <el-option :label="$t('article.list.draft')" :value="0" />
            <el-option :label="$t('article.list.published')" :value="1" />
            <el-option :label="$t('article.list.unpublished')" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            {{ $t('common.search') }}
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            {{ $t('common.reset') }}
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
        <el-table-column prop="coverImage" :label="$t('article.list.cover')" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 60px; height: 40px; border-radius: 4px;"
            />
            <div v-else class="no-image">{{ $t('article.list.noCover') }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" :label="$t('article.list.articleTitle')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" :label="$t('article.list.category')" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" :label="$t('article.list.author')" width="120" />
        <el-table-column prop="viewCount" :label="$t('article.list.viewCount')" width="100" align="center" />
        <el-table-column prop="isTop" :label="$t('article.list.isTop')" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">{{ $t('article.list.topTag') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRecommend" :label="$t('article.list.isRecommend')" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommend === 1" type="warning" size="small">{{ $t('article.list.recommendTag') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('article.list.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" :label="$t('article.list.publishTime')" width="180" />
        <el-table-column prop="createTime" :label="$t('article.list.createTime')" width="180" />
        <el-table-column :label="$t('article.list.operation')" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleView(row)"
            >
              {{ $t('article.list.preview') }}
            </el-button>
            <el-button
              v-permission="'article:edit'"
              type="warning"
              link
              size="small"
              @click="handleEdit(row)"
            >
              {{ $t('article.list.edit') }}
            </el-button>
            <el-button
              v-if="row.status === 0"
              v-permission="'article:publish'"
              type="success"
              link
              size="small"
              @click="handlePublish(row)"
            >
              {{ $t('article.list.publish') }}
            </el-button>
            <el-button
              v-else-if="row.status === 1"
              v-permission="'article:publish'"
              type="info"
              link
              size="small"
              @click="handleUnpublish(row)"
            >
              {{ $t('article.list.unpublish') }}
            </el-button>
            <el-dropdown @command="handleMoreActions" v-if="hasAnyOperationPermission">
              <el-button type="info" link size="small">
                {{ $t('article.list.more') }} <el-icon><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-permission="'article:top'"
                    :command="`top-${row.id}`"
                  >
                    {{ row.isTop === 1 ? $t('article.list.cancelTop') : $t('article.list.setTop') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-permission="'article:recommend'"
                    :command="`recommend-${row.id}`"
                  >
                    {{ row.isRecommend === 1 ? $t('article.list.cancelRecommend') : $t('article.list.setRecommend') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-permission="'article:delete'"
                    :command="`delete-${row.id}`"
                    divided
                  >
                    {{ $t('article.list.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete, ArrowDown } from '@element-plus/icons-vue'
import { articleApi, categoryApi } from '@/api/system'
import { usePermission } from '@/directives/permission'
import { useI18n } from '@/composables/useI18n'

// 权限检查
const { hasPermission, hasAnyPermission } = usePermission()
const { t } = useI18n()

// 检查是否有任何操作权限（用于显示更多菜单）
const hasAnyOperationPermission = computed(() => {
  return hasAnyPermission(['article:top', 'article:recommend', 'article:delete'])
})

// 响应式数据
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: '',
  status: '',
  authorName: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 分类选项
const categoryOptions = ref<any[]>([])

// 表格数据
const tableData = ref<any[]>([])

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    categoryId: '',
    status: '',
    authorName: ''
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
    
    const response = await articleApi.getArticleList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategoryOptions = async () => {
  try {
    const response = await categoryApi.getEnabledCategories()
    if (response.code === 200) {
      categoryOptions.value = response.data
    }
  } catch (error) {
    console.error('加载分类选项失败:', error)
  }
}

const handleAdd = () => {
  ElMessage.info('发布文章功能开发中...')
}

const handleView = (row: any) => {
  ElMessage.info(`预览文章 "${row.title}" 功能开发中...`)
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑文章 "${row.title}" 功能开发中...`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除文章 "${row.title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    loadTableData()
  }).catch(() => {
    // 取消删除
  })
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

// 状态相关方法
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return t('article.list.draft')
    case 1: return t('article.list.published')
    case 2: return t('article.list.unpublished')
    default: return t('common.unknown')
  }
}

// 发布文章
const handlePublish = async (row: any) => {
  try {
    // await articleApi.publishArticle(row.id)
    row.status = 1
    row.publishTime = new Date().toLocaleString()
    ElMessage.success('发布成功')
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  }
}

// 下架文章
const handleUnpublish = async (row: any) => {
  try {
    // await articleApi.unpublishArticle(row.id)
    row.status = 2
    ElMessage.success('下架成功')
  } catch (error) {
    console.error('下架失败:', error)
    ElMessage.error('下架失败')
  }
}

// 更多操作
const handleMoreActions = async (command: string) => {
  const [action, id] = command.split('-')
  const row = tableData.value.find(item => item.id === parseInt(id))
  
  if (!row) return
  
  try {
    switch (action) {
      case 'top':
        const newTopStatus = row.isTop === 1 ? 0 : 1
        // await articleApi.setArticleTop(row.id, newTopStatus)
        row.isTop = newTopStatus
        ElMessage.success(newTopStatus === 1 ? '置顶成功' : '取消置顶成功')
        break
        
      case 'recommend':
        const newRecommendStatus = row.isRecommend === 1 ? 0 : 1
        // await articleApi.setArticleRecommend(row.id, newRecommendStatus)
        row.isRecommend = newRecommendStatus
        ElMessage.success(newRecommendStatus === 1 ? '推荐成功' : '取消推荐成功')
        break
        
      case 'delete':
        await ElMessageBox.confirm(
          `确定要删除文章 "${row.title}" 吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        // await articleApi.deleteArticle(row.id)
        ElMessage.success('删除成功')
        loadTableData()
        break
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadTableData()
  loadCategoryOptions()
})
</script>

<style lang="scss" scoped>
.article-list {
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
    
    .no-image {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 60px;
      height: 40px;
      background: var(--bg-secondary);
      border-radius: 4px;
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
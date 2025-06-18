<template>
  <div class="article-list">
    <div class="page-header">
      <div class="header-title">
        <h2>文章列表</h2>
        <p>管理系统文章内容</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          发布文章
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文章标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入文章标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option label="技术分享" value="1" />
            <el-option label="产品介绍" value="2" />
            <el-option label="公司动态" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="已发布" value="1" />
            <el-option label="草稿" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
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
        <el-table-column prop="coverImage" label="封面" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 60px; height: 40px; border-radius: 4px;"
            />
            <div v-else class="no-image">无封面</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              预览
            </el-button>
            <el-button
              type="warning"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
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
import { Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([
  {
    id: 1,
    title: 'Vue3 + TypeScript 开发实践指南',
    categoryId: 1,
    categoryName: '技术分享',
    author: '张三',
    coverImage: 'https://picsum.photos/200/150?random=1',
    viewCount: 1256,
    status: 1,
    publishTime: '2024-01-01 10:00:00',
    createTime: '2023-12-28 15:30:00'
  },
  {
    id: 2,
    title: 'Nextera产品功能详细介绍',
    categoryId: 2,
    categoryName: '产品介绍',
    author: '李四',
    coverImage: 'https://picsum.photos/200/150?random=2',
    viewCount: 856,
    status: 1,
    publishTime: '2024-01-02 14:20:00',
    createTime: '2024-01-01 09:15:00'
  },
  {
    id: 3,
    title: '公司2024年发展规划',
    categoryId: 3,
    categoryName: '公司动态',
    author: '王五',
    coverImage: null,
    viewCount: 432,
    status: 0,
    publishTime: null,
    createTime: '2024-01-03 11:45:00'
  },
  {
    id: 4,
    title: 'React vs Vue 框架对比分析',
    categoryId: 1,
    categoryName: '技术分享',
    author: '赵六',
    coverImage: 'https://picsum.photos/200/150?random=4',
    viewCount: 678,
    status: 1,
    publishTime: '2024-01-04 16:30:00',
    createTime: '2024-01-03 20:10:00'
  }
])

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    categoryId: '',
    status: ''
  })
  handleSearch()
}

const loadTableData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = tableData.value.length
    loading.value = false
  }, 500)
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

onMounted(() => {
  loadTableData()
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
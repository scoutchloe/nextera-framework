<template>
  <div class="category-management">
    <div class="page-header">
      <div class="header-title">
        <h2>分类管理</h2>
        <p>管理文章分类信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增分类
        </el-button>
      </div>
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
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column prop="articleCount" label="文章数量" width="120" align="center" />
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)

// 表格数据
const tableData = ref([
  {
    id: 1,
    name: '技术分享',
    description: '分享前端、后端等技术相关文章',
    articleCount: 25,
    sort: 1,
    status: 1,
    createTime: '2023-01-01 10:00:00'
  },
  {
    id: 2,
    name: '产品介绍',
    description: '介绍公司产品功能和特色',
    articleCount: 12,
    sort: 2,
    status: 1,
    createTime: '2023-01-02 14:30:00'
  },
  {
    id: 3,
    name: '公司动态',
    description: '发布公司最新动态和新闻',
    articleCount: 8,
    sort: 3,
    status: 1,
    createTime: '2023-01-03 09:15:00'
  },
  {
    id: 4,
    name: '行业资讯',
    description: '分享行业相关资讯和趋势',
    articleCount: 5,
    sort: 4,
    status: 0,
    createTime: '2023-01-04 16:45:00'
  }
])

// 方法
const loadTableData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleAdd = () => {
  ElMessage.info('新增分类功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑分类 "${row.name}" 功能开发中...`)
}

const handleDelete = (row: any) => {
  if (row.articleCount > 0) {
    ElMessage.warning('该分类下还有文章，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除分类 "${row.name}" 吗？`,
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

const handleStatusChange = (row: any) => {
  setTimeout(() => {
    ElMessage.success(row.status ? '启用成功' : '禁用成功')
  }, 200)
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

  .table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
    overflow: hidden;
    
    .el-table {
      border: none;
    }
  }
}
</style> 
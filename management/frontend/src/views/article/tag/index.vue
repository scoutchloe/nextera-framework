<template>
  <div class="tag-management">
    <div class="page-header">
      <div class="header-title">
        <h2>标签管理</h2>
        <p>管理文章标签信息</p>
      </div>
      <div class="header-actions">
        <el-button 
          v-permission="'article:tag:add'"
          type="primary" 
          :icon="Plus" 
          @click="handleAdd"
        >
          新增标签
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
        <el-table-column prop="name" label="标签名称" width="200" />
        <el-table-column prop="color" label="标签颜色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :color="row.color" style="color: white;">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
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
              v-permission="'article:tag:edit'"
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-permission="'article:tag:delete'"
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
    name: 'Vue3',
    color: '#4fc08d',
    description: 'Vue.js 3.0 相关技术文章',
    articleCount: 15,
    sort: 1,
    status: 1,
    createTime: '2023-01-01 10:00:00'
  },
  {
    id: 2,
    name: 'TypeScript',
    color: '#3178c6',
    description: 'TypeScript 开发技术分享',
    articleCount: 12,
    sort: 2,
    status: 1,
    createTime: '2023-01-02 14:30:00'
  },
  {
    id: 3,
    name: '前端工程化',
    color: '#f56565',
    description: '前端工程化相关内容',
    articleCount: 8,
    sort: 3,
    status: 1,
    createTime: '2023-01-03 09:15:00'
  },
  {
    id: 4,
    name: 'Node.js',
    color: '#68d391',
    description: 'Node.js 后端开发技术',
    articleCount: 6,
    sort: 4,
    status: 1,
    createTime: '2023-01-04 16:45:00'
  },
  {
    id: 5,
    name: '数据库',
    color: '#ed8936',
    description: '数据库设计与优化',
    articleCount: 4,
    sort: 5,
    status: 0,
    createTime: '2023-01-05 11:20:00'
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
  ElMessage.info('新增标签功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑标签 "${row.name}" 功能开发中...`)
}

const handleDelete = (row: any) => {
  if (row.articleCount > 0) {
    ElMessage.warning('该标签下还有文章，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除标签 "${row.name}" 吗？`,
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
.tag-management {
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
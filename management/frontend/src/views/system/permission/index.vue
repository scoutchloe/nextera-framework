<template>
  <div class="permission-management" style="width: 100%; height: 100%; min-height: 500px; background: white; padding: 20px;">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>权限管理</h2>
        <p>管理系统权限配置和菜单访问控制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增权限
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="权限名称">
          <el-input
            v-model="searchForm.permissionName"
            placeholder="请输入权限名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select v-model="searchForm.type" placeholder="请选择权限类型" clearable>
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
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
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permissionName" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.type === 'menu' ? 'primary' : row.type === 'button' ? 'success' : 'info'"
            >
              {{ row.type === 'menu' ? '菜单' : row.type === 'button' ? '按钮' : 'API' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径/URL" min-width="150" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.method" size="small" :type="getMethodTagType(row.method)">
              {{ row.method }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Plus"
              @click="handleAddChild(row)"
              v-if="row.type === 'menu'"
            >
              添加
            </el-button>
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

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permissionName">
              <el-input v-model="formData.permissionName" placeholder="请输入权限名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码" prop="permissionCode">
              <el-input v-model="formData.permissionCode" placeholder="请输入权限编码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限类型" prop="type">
              <el-select v-model="formData.type" @change="handleTypeChange" style="width: 100%">
                <el-option label="菜单" value="menu" />
                <el-option label="按钮" value="button" />
                <el-option label="接口" value="api" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父级权限" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="permissionTreeData"
                :props="treeSelectProps"
                placeholder="请选择父级权限"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="formData.type === 'menu'">
          <el-col :span="12">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="formData.path" placeholder="请输入路由路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="formData.component" placeholder="请输入组件路径" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="formData.type === 'api'">
          <el-col :span="12">
            <el-form-item label="API地址" prop="path">
              <el-input v-model="formData.path" placeholder="请输入API地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求方式" prop="method">
              <el-select v-model="formData.method" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="请输入图标名称">
                <template #prepend>
                  <el-icon><component :is="formData.icon || 'Menu'" /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="formData.type === 'menu'">
            <el-form-item label="显示状态" prop="visible">
              <el-radio-group v-model="formData.visible">
                <el-radio :label="1">显示</el-radio>
                <el-radio :label="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { permissionApi } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  permissionName: '',
  type: '',
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
    permissionName: '系统管理',
    permissionCode: 'system',
    type: 'menu',
    path: '/system',
    component: 'Layout',
    icon: 'Setting',
    sort: 1,
    status: 1,
    visible: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: 11,
        permissionName: '用户管理',
        permissionCode: 'system:user',
        type: 'menu',
        path: '/system/user',
        component: 'system/user/index',
        icon: 'User',
        sort: 1,
        status: 1,
        visible: 1,
        createTime: '2024-01-01 10:05:00',
        children: [
          {
            id: 111,
            permissionName: '用户查询',
            permissionCode: 'system:user:list',
            type: 'button',
            sort: 1,
            status: 1,
            createTime: '2024-01-01 10:10:00'
          },
          {
            id: 112,
            permissionName: '用户新增',
            permissionCode: 'system:user:add',
            type: 'button',
            sort: 2,
            status: 1,
            createTime: '2024-01-01 10:15:00'
          }
        ]
      },
      {
        id: 12,
        permissionName: '角色管理',
        permissionCode: 'system:role',
        type: 'menu',
        path: '/system/role',
        component: 'system/role/index',
        icon: 'Avatar',
        sort: 2,
        status: 1,
        visible: 1,
        createTime: '2024-01-01 10:20:00'
      }
    ]
  },
  {
    id: 2,
    permissionName: '内容管理',
    permissionCode: 'content',
    type: 'menu',
    path: '/content',
    component: 'Layout',
    icon: 'EditPen',
    sort: 2,
    status: 1,
    visible: 1,
    createTime: '2024-01-01 11:00:00',
    children: [
      {
        id: 21,
        permissionName: '文章管理',
        permissionCode: 'content:article',
        type: 'menu',
        path: '/content/article',
        component: 'content/article/index',
        icon: 'Document',
        sort: 1,
        status: 1,
        visible: 1,
        createTime: '2024-01-01 11:05:00'
      }
    ]
  },
  {
    id: 3,
    permissionName: '用户查询API',
    permissionCode: 'api:user:list',
    type: 'api',
    path: '/api/user/list',
    method: 'GET',
    sort: 1,
    status: 1,
    createTime: '2024-01-01 12:00:00'
  }
])

const selectedRows = ref<any[]>([])

// 表单数据
const formData = reactive({
  id: null,
  permissionName: '',
  permissionCode: '',
  type: 'menu',
  parentId: null,
  path: '',
  component: '',
  method: '',
  icon: '',
  sort: 0,
  description: '',
  status: 1,
  visible: 1
})

// 表单验证规则
const formRules = {
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 20, message: '权限名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-z:_]+$/, message: '权限编码只能包含小写字母、冒号和下划线', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
}

// 权限树数据
const permissionTreeData = computed(() => {
  const buildTree = (items: any[], parentId = null) => {
    return items
      .filter(item => {
        if (parentId === null) {
          return !item.parentId
        }
        return item.parentId === parentId
      })
      .map(item => ({
        value: item.id,
        label: item.permissionName,
        children: buildTree(items, item.id)
      }))
      .filter(item => item.value !== formData.id) // 排除自己
  }
  
  return [
    { value: null, label: '顶级权限', children: buildTree(flattenTableData(tableData.value)) }
  ]
})

const treeSelectProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// 扁平化表格数据
const flattenTableData = (data: any[]): any[] => {
  const result: any[] = []
  
  const flatten = (items: any[]) => {
    items.forEach(item => {
      result.push(item)
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  
  flatten(data)
  return result
}

// 获取请求方式标签类型
const getMethodTagType = (method: string) => {
  const typeMap: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return typeMap[method] || 'info'
}

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    permissionName: '',
    type: '',
    status: ''
  })
  handleSearch()
}

const loadTableData = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      permissionName: searchForm.permissionName || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }
    
    console.log('加载权限列表，参数:', params)
    const response = await permissionApi.getPermissionList(params)
    console.log('权限列表响应:', response)
    console.log('响应数据结构:', response.data)
    
    if (response.code === 200 || response.success) {
      // 处理不同的响应数据结构
      let dataList = []
      let total = 0
      
      if (response.data) {
        // 检查是否是分页结构
        if (response.data.records && Array.isArray(response.data.records)) {
          dataList = response.data.records
          total = response.data.total || response.data.records.length
        } else if (response.data.list && Array.isArray(response.data.list)) {
          dataList = response.data.list
          total = response.data.total || response.data.list.length
        } else if (Array.isArray(response.data)) {
          dataList = response.data
          total = response.data.length
        } else {
          console.warn('未识别的数据结构:', response.data)
          // 使用现有的模拟数据作为fallback
          dataList = tableData.value
          total = flattenTableData(tableData.value).length
        }
      }
      
      console.log('处理后的权限列表:', dataList)
      console.log('权限总数:', total)
      
      tableData.value = dataList
      pagination.total = total
      
      if (dataList.length > 0) {
        ElMessage.success(`权限列表加载成功，共${total}条数据`)
      } else {
        ElMessage.info('暂无权限数据')
      }
    } else {
      ElMessage.error(response.message || '获取权限列表失败')
      // 使用现有的模拟数据作为fallback
      pagination.total = flattenTableData(tableData.value).length
    }
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败，请检查网络连接')
    // 使用现有的模拟数据作为fallback
    pagination.total = flattenTableData(tableData.value).length
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增权限'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    type: 'menu',
    parentId: null,
    path: '',
    component: '',
    method: '',
    icon: '',
    sort: 0,
    description: '',
    status: 1,
    visible: 1
  })
  dialogVisible.value = true
}

const handleAddChild = (row: any) => {
  dialogTitle.value = '新增子权限'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    type: 'button',
    parentId: row.id,
    path: '',
    component: '',
    method: '',
    icon: '',
    sort: 0,
    description: '',
    status: 1,
    visible: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑权限'
  isEdit.value = true
  Object.assign(formData, { ...row, parentId: row.parentId || null })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除权限 "${row.permissionName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 执行删除操作
    ElMessage.success('删除成功')
    loadTableData()
  }).catch(() => {
    // 取消删除
  })
}

const handleTypeChange = (type: string) => {
  // 根据类型重置相关字段
  if (type === 'api') {
    formData.component = ''
    formData.visible = 1
  } else if (type === 'button') {
    formData.path = ''
    formData.component = ''
    formData.method = ''
    formData.visible = 1
  } else {
    formData.method = ''
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

const handleSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      // 模拟API调用
      setTimeout(() => {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        submitLoading.value = false
        dialogVisible.value = false
        loadTableData()
      }, 1000)
    }
  })
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadTableData()
})
</script>

<style lang="scss" scoped>
.permission-management {
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
    
    .pagination-container {
      padding: 16px 20px;
      border-top: 1px solid var(--border-color);
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 对话框样式调整
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-tree-select) {
  width: 100%;
}
</style> 
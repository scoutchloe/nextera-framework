<template>
  <div class="permission-tree-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>权限管理 - 树形结构</h2>
        <p>基于树形结构的权限配置和菜单访问控制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增权限
        </el-button>
        <el-button type="success" :icon="Refresh" @click="handleRefresh">
          刷新权限树
        </el-button>
        <el-button type="info" :icon="Expand" @click="handleExpandAll" v-if="!isAllExpanded">
          展开全部
        </el-button>
        <el-button type="info" :icon="Fold" @click="handleCollapseAll" v-else>
          收起全部
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
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select v-model="searchForm.permissionType" placeholder="请选择权限类型" clearable @change="handleSearch">
            <el-option label="菜单权限" value="menu" />
            <el-option label="按钮权限" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 权限树表格 -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="filteredTreeData"
        style="width: 100%"
        row-key="id"
        :default-expand-all="isAllExpanded"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
        class="permission-tree-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permissionName" label="权限名称" min-width="250">
          <template #default="{ row }">
            <div class="permission-name">
              <el-icon v-if="row.icon" class="permission-icon">
                <component :is="getIconComponent(row.icon)" />
              </el-icon>
              <span>{{ row.permissionName }}</span>
              <el-tag v-if="row.children && row.children.length > 0" type="info" size="small" class="children-count">
                {{ row.children.length }}个子权限
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permissionType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.permissionType === 'menu' ? 'primary' : 'success'"
              size="small"
              class="permission-type-tag"
              :class="row.permissionType"
            >
              {{ row.permissionType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuPath" label="路径/URL" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.menuPath" class="path-text">{{ row.menuPath }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="componentPath" label="组件路径" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.componentPath" class="component-text">{{ row.componentPath }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.sortOrder }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              :disabled="loading"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                link
                :icon="Plus"
                @click="handleAddChild(row)"
                v-if="row.permissionType === 'menu'"
                size="small"
              >
                添加子权限
              </el-button>
              <el-button
                type="warning"
                link
                :icon="Edit"
                @click="handleEdit(row)"
                size="small"
              >
                编辑
              </el-button>
              <el-button
                type="info"
                link
                :icon="View"
                @click="handleView(row)"
                size="small"
              >
                查看
              </el-button>
              <el-button
                type="danger"
                link
                :icon="Delete"
                @click="handleDelete(row)"
                size="small"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :before-close="handleDialogClose"
      :close-on-click-modal="false"
      class="permission-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="permission-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permissionName">
              <el-input v-model="formData.permissionName" placeholder="请输入权限名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码" prop="permissionCode">
              <el-input v-model="formData.permissionCode" placeholder="请输入权限编码，如：system:user:add" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permissionType">
              <el-select v-model="formData.permissionType" @change="handleTypeChange" style="width: 100%">
                <el-option label="菜单权限" value="menu" />
                <el-option label="按钮权限" value="button" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父级权限" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="parentPermissionOptions"
                :props="treeSelectProps"
                placeholder="请选择父级权限"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="formData.permissionType === 'menu'">
          <el-col :span="12">
            <el-form-item label="菜单路径" prop="menuPath">
              <el-input v-model="formData.menuPath" placeholder="请输入菜单路径，如：/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径" prop="componentPath">
              <el-input v-model="formData.componentPath" placeholder="请输入组件路径，如：system/user/index" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="请输入图标名称，如：User">
                <template #prepend>
                  <el-icon v-if="formData.icon">
                    <component :is="getIconComponent(formData.icon)" />
                  </el-icon>
                  <el-icon v-else><Menu /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="formData.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权限描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
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

    <!-- 权限详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="权限详情"
      width="600px"
      class="permission-detail-dialog"
    >
      <div v-if="currentPermission" class="permission-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="权限名称">{{ currentPermission.permissionName }}</el-descriptions-item>
          <el-descriptions-item label="权限编码">{{ currentPermission.permissionCode }}</el-descriptions-item>
          <el-descriptions-item label="权限类型">
            <el-tag :type="currentPermission.permissionType === 'menu' ? 'primary' : 'success'">
              {{ currentPermission.permissionType === 'menu' ? '菜单权限' : '按钮权限' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentPermission.status === 1 ? 'success' : 'danger'">
              {{ currentPermission.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="菜单路径" v-if="currentPermission.menuPath">
            {{ currentPermission.menuPath }}
          </el-descriptions-item>
          <el-descriptions-item label="组件路径" v-if="currentPermission.componentPath">
            {{ currentPermission.componentPath }}
          </el-descriptions-item>
          <el-descriptions-item label="图标" v-if="currentPermission.icon">
            <el-icon><component :is="getIconComponent(currentPermission.icon)" /></el-icon>
            {{ currentPermission.icon }}
          </el-descriptions-item>
          <el-descriptions-item label="排序">{{ currentPermission.sortOrder }}</el-descriptions-item>
          <el-descriptions-item label="权限描述" :span="2" v-if="currentPermission.description">
            {{ currentPermission.description }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, View, Expand, Fold, Menu } from '@element-plus/icons-vue'
import { permissionApi } from '@/api/system'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const isAllExpanded = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const tableRef = ref<InstanceType<typeof ElTable>>()
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  permissionName: '',
  permissionType: '',
  status: ''
})

// 表格数据
const treeData = ref<any[]>([])
const selectedRows = ref<any[]>([])
const currentPermission = ref<any>(null)

// 表单数据
const formData = reactive({
  id: null,
  permissionName: '',
  permissionCode: '',
  permissionType: 'menu',
  parentId: null,
  menuPath: '',
  componentPath: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const formRules = reactive({
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' }
  ],
  permissionType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
})

// 父级权限选项
const parentPermissionOptions = ref<any[]>([])
const treeSelectProps = {
  value: 'id',
  label: 'permissionName',
  children: 'children'
}

// 计算属性
const filteredTreeData = computed(() => {
  if (!searchForm.permissionName && !searchForm.permissionType && searchForm.status === '') {
    return treeData.value
  }
  
  return filterTreeData(treeData.value)
})

// 方法
const loadTreeData = async () => {
  try {
    loading.value = true
    const response = await permissionApi.getPermissionTree()
    if (response.code === 200) {
      treeData.value = response.data
      parentPermissionOptions.value = buildParentOptions(response.data)
    }
  } catch (error) {
    console.error('加载权限树失败:', error)
    ElMessage.error('加载权限树失败')
  } finally {
    loading.value = false
  }
}

const filterTreeData = (data: any[]): any[] => {
  return data.filter(item => {
    let match = true
    
    if (searchForm.permissionName) {
      match = match && item.permissionName.includes(searchForm.permissionName)
    }
    
    if (searchForm.permissionType) {
      match = match && item.permissionType === searchForm.permissionType
    }
    
    if (searchForm.status !== '') {
      match = match && item.status === searchForm.status
    }
    
    if (item.children && item.children.length > 0) {
      item.children = filterTreeData(item.children)
      if (item.children.length > 0) {
        match = true
      }
    }
    
    return match
  })
}

const buildParentOptions = (data: any[]): any[] => {
  const options: any[] = [{ id: null, permissionName: '顶级权限', children: [] }]
  
  const buildOptions = (items: any[], parentOption: any) => {
    items.forEach(item => {
      if (item.permissionType === 'menu') {
        const option = {
          id: item.id,
          permissionName: item.permissionName,
          children: []
        }
        parentOption.children.push(option)
        
        if (item.children && item.children.length > 0) {
          buildOptions(item.children, option)
        }
      }
    })
  }
  
  buildOptions(data, options[0])
  return options
}

const getIconComponent = (iconName: string) => {
  return (ElementPlusIconsVue as any)[iconName] || Menu
}

const handleSearch = () => {
  // 搜索逻辑在计算属性中处理
}

const handleReset = () => {
  Object.assign(searchForm, {
    permissionName: '',
    permissionType: '',
    status: ''
  })
}

const handleRefresh = () => {
  loadTreeData()
}

const handleExpandAll = () => {
  isAllExpanded.value = true
  tableRef.value?.doLayout()
}

const handleCollapseAll = () => {
  isAllExpanded.value = false
  tableRef.value?.doLayout()
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleAdd = () => {
  dialogTitle.value = '新增权限'
  resetFormData()
  dialogVisible.value = true
}

const handleAddChild = (row: any) => {
  dialogTitle.value = '新增子权限'
  resetFormData()
  formData.parentId = row.id
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑权限'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row: any) => {
  currentPermission.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限"${row.permissionName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await permissionApi.deletePermission(row.id)
    ElMessage.success('删除成功')
    loadTreeData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error('删除权限失败')
    }
  }
}

const handleStatusChange = async (row: any) => {
  try {
    await permissionApi.updatePermissionStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('更新权限状态失败:', error)
    ElMessage.error('更新权限状态失败')
    row.status = row.status === 1 ? 0 : 1 // 回滚状态
  }
}

const handleTypeChange = (value: string) => {
  if (value === 'button') {
    formData.menuPath = ''
    formData.componentPath = ''
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  resetFormData()
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    if (formData.id) {
      await permissionApi.updatePermission(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await permissionApi.createPermission(formData)
      ElMessage.success('创建成功')
    }
    
    handleDialogClose()
    loadTreeData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'menu',
    parentId: null,
    menuPath: '',
    componentPath: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    description: ''
  })
}

onMounted(() => {
  loadTreeData()
})
</script> 
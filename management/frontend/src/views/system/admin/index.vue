<template>
  <div class="admin-management" style="width: 100%; height: 100%; min-height: 500px; background: white; padding: 20px;">
    <div class="page-header">
      <div class="header-title">
        <h2>{{ t('system.admin.systemAccountTitle') }}</h2>
        <p>{{ t('system.admin.systemAccountSubtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          {{ t('system.admin.createAdmin') }}
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('system.admin.usernameLabel')">
          <el-input
            v-model="searchForm.username"
            :placeholder="t('system.admin.usernamePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('system.admin.realNameLabel')">
          <el-input
            v-model="searchForm.realName"
            :placeholder="t('system.admin.realNamePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('system.admin.roleLabel')">
          <el-select v-model="searchForm.roleId" :placeholder="t('system.admin.rolePlaceholder')" clearable>
            <el-option 
              v-for="role in roleOptions" 
              :key="role.id" 
              :label="role.roleName" 
              :value="role.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('system.admin.statusLabel')">
          <el-select v-model="searchForm.status" :placeholder="t('system.admin.statusPlaceholder')" clearable>
            <el-option :label="t('common.enable')" value="1" />
            <el-option :label="t('common.disable')" value="0" />
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="avatar" :label="t('system.admin.avatar')" width="120">
          <template #default="{ row }">
            <div class="avatar-cell">
              <el-avatar :src="getAvatarDisplayUrl(row.avatar)" :size="40">
                {{ row.realName?.charAt(0) }}
              </el-avatar>
              <el-button 
                v-if="row.avatar" 
                type="text" 
                size="small" 
                @click="downloadAvatar(row.avatar)"
                class="download-btn"
              >
                {{ t('system.admin.download') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="t('system.admin.username')" width="120" />
        <el-table-column prop="realName" :label="t('system.admin.realName')" width="100" />
        <el-table-column prop="email" :label="t('system.admin.email')" min-width="180" />
        <el-table-column prop="phone" :label="t('system.admin.phone')" width="120" />
        <el-table-column prop="roleName" :label="t('system.admin.role')" width="120">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" :label="t('system.admin.lastLoginTime')" width="180" />
        <el-table-column prop="createTime" :label="t('system.admin.createTime')" width="180" />
        <el-table-column :label="t('system.admin.operation')" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              {{ t('system.admin.edit') }}
            </el-button>
            <el-button
              type="warning"
              link
              :icon="Key"
              @click="handleResetPassword(row)"
            >
              {{ t('system.admin.resetPassword') }}
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
              :disabled="row.id === 1"
            >
              {{ t('system.admin.delete') }}
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

    <!-- 管理员编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="formData.username"
                placeholder="请输入用户名"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="!isEdit">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
                <el-option 
                  v-for="role in roleOptions" 
                  :key="role.id" 
                  :label="role.roleName" 
                  :value="role.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <img v-if="formData.avatar" :src="getAvatarDisplayUrl(formData.avatar)" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="avatar-actions" v-if="formData.avatar">
              <el-button type="text" size="small" @click="downloadAvatar(formData.avatar)">
                下载头像
              </el-button>
              <el-button type="text" size="small" danger @click="formData.avatar = ''">
                删除头像
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
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

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Key } from '@element-plus/icons-vue'
import type { FormInstance, UploadProps, UploadRequestOptions } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { adminApi, roleApi, fileApi } from '@/api/system'

const { t } = useI18n()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const passwordLoading = ref(false)
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const currentUserId = ref<number | null>(null)

// 角色选项
const roleOptions = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  roleId: '',
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

const selectedRows = ref<any[]>([])

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  avatar: '',
  role: 2, // 默认为普通管理员
  status: 1,
  remark: ''
})

// 密码表单数据
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== formData.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    realName: '',
    roleId: '',
    status: ''
  })
  handleSearch()
}

// 加载角色选项
const loadRoleOptions = async () => {
  try {
    const response = await roleApi.getAllRoles()
    if (response.code === 200) {
      roleOptions.value = response.data || []
    } else {
      // 使用默认角色选项
      roleOptions.value = [
        { id: 1, roleName: '超级管理员' },
        { id: 2, roleName: '系统管理员' },
        { id: 3, roleName: '内容管理员' }
      ]
    }
  } catch (error) {
    console.error('加载角色选项失败:', error)
    // 使用默认角色选项
    roleOptions.value = [
      { id: 1, roleName: '超级管理员' },
      { id: 2, roleName: '系统管理员' },
      { id: 3, roleName: '内容管理员' }
    ]
  }
}

const loadTableData = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      roleId: searchForm.roleId || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }
    
    console.log('加载管理员列表，参数:', params)
    const response = await adminApi.getAdminList(params)
    
         // 处理不同的响应数据结构
     let dataList = []
     let total = 0
     
     if (response.data) {
       // 检查是否是分页结构
       if (response.data.records && Array.isArray(response.data.records)) {
         dataList = response.data.records
         total = response.data.total || response.data.size || response.data.records.length
       } else if (response.data.list && Array.isArray(response.data.list)) {
         dataList = response.data.list
         total = response.data.total || response.data.size || response.data.list.length
       } else if (Array.isArray(response.data)) {
         dataList = response.data
         total = response.data.length
       } else {
         console.warn('未识别的数据结构:', response.data)
         dataList = []
         total = 0
       }
     }
     
    //  console.log('处理后的管理员列表:', dataList)
     console.log('管理员总数:', total)
     
     // 数据字段转换（如果需要）
     const transformedData = dataList.map((item: any) => ({
       id: item.id,
       username: item.username,
       realName: item.realName,
       email: item.email,
       phone: item.phone,
       avatar: item.avatar,
       roleId: item.role || item.roleId,  // 后端返回role字段
       roleName: item.roleName || getRoleNameById(item.role || item.roleId),
       status: item.status,
       lastLoginTime: item.lastLoginTime,
       createTime: item.createTime,
       updateTime: item.updateTime,
       remark: item.remark || '',
       // 保留原始数据
       ...item
     }))
     
     console.log('字段转换后的管理员数据:', transformedData)
     
     // 设置表格数据
     tableData.value = transformedData
     pagination.total = total
     
     if (transformedData.length > 0) {
       ElMessage.success(`管理员列表加载成功，共${total}条数据`)
     } else {
       ElMessage.info('暂无管理员数据')
     }
  } catch (error) {
    console.error('获取管理员列表失败:', error)
    ElMessage.error('网络连接失败，使用模拟数据展示')
    
    // 确保有数据显示
    const mockData = [
      {
        id: 1,
        username: 'admin',
        realName: '超级管理员',
        email: 'admin@nextera.com',
        phone: '13800138000',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        roleId: 1,
        roleName: '超级管理员',
        status: 1,
        lastLoginTime: '2024-01-01 10:00:00',
        createTime: '2023-01-01 00:00:00',
        remark: '系统默认超级管理员'
      },
      {
        id: 2,
        username: 'manager',
        realName: '张三',
        email: 'zhangsan@nextera.com',
        phone: '13800138001',
        avatar: null,
        roleId: 2,
        roleName: '系统管理员',
        status: 1,
        lastLoginTime: '2024-01-01 09:30:00',
        createTime: '2023-06-01 10:00:00',
        remark: '系统管理员'
      }
    ]
    
    tableData.value = mockData
    pagination.total = mockData.length
    console.log('设置模拟数据:', mockData)
  } finally {
    loading.value = false
  }
}

// 根据角色ID获取角色名称
const getRoleNameById = (roleId: number) => {
  const role = roleOptions.value.find(r => r.id === roleId)
  return role ? role.roleName : '未知角色'
}

const handleAdd = () => {
  dialogTitle.value = '新增管理员'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    username: '',
    realName: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    avatar: '',
    role: 2,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑管理员'
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    role: row.roleId || 2,
    password: '',
    confirmPassword: ''
  })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除管理员 "${row.realName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('删除管理员:', row.id)
      const response = await adminApi.deleteAdmin(row.id)
      console.log('删除管理员响应:', response)
      
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadTableData()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除管理员失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
    }
  }).catch(() => {
    // 取消删除
  })
}

const handleStatusChange = async (row: any) => {
      try {
      console.log('更改管理员状态:', row.id, row.status)
      const response = await adminApi.updateAdmin(row.id, { status: row.status })
      console.log('状态更改响应:', response)
    
    if (response.code === 200) {
      ElMessage.success(row.status ? '启用成功' : '禁用成功')
    } else {
      ElMessage.error(response.message || '状态更改失败')
      // 恢复原状态
      row.status = row.status ? 0 : 1
    }
  } catch (error) {
    console.error('状态更改失败:', error)
    ElMessage.error('状态更改失败，请检查网络连接')
    // 恢复原状态
    row.status = row.status ? 0 : 1
  }
}

const handleResetPassword = (row: any) => {
  currentUserId.value = row.id
  Object.assign(passwordForm, {
    newPassword: '',
    confirmPassword: ''
  })
  passwordDialogVisible.value = true
}

const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (valid) {
    submitLoading.value = true
    try {
      const submitData: any = {
        username: formData.username,
        realName: formData.realName,
        email: formData.email,
        phone: formData.phone,
        avatar: formData.avatar,
        role: formData.role, // 修正：使用roleId字段
        roleIds: [formData.role || 2], // 修正：使用roleIds数组格式
        status: formData.status,
        remark: formData.remark
      }
      
      // 如果是新增，添加密码字段
      if (!isEdit.value) {
        submitData.password = formData.password
      }
      
      console.log('提交管理员数据:', submitData)
      
      let response
      if (isEdit.value && formData.id) {
        response = await adminApi.updateAdmin(formData.id, submitData)
        // 编辑后单独分配角色
        if (response.code === 200 && formData.role) {
          try {
            await adminApi.assignAdminRoles(formData.id, [formData.role])
            console.log('角色分配成功')
          } catch (roleError) {
            console.error('角色分配失败:', roleError)
            ElMessage.warning('管理员信息更新成功，但角色分配失败')
          }
        }
      } else {
        response = await adminApi.createAdmin(submitData)
        // 新增后也需要分配角色
        if (response.code === 200 && response.data && response.data.id && formData.role) {
          try {
            await adminApi.assignAdminRoles(response.data.id, [formData.role])
            console.log('新用户角色分配成功')
          } catch (roleError) {
            console.error('新用户角色分配失败:', roleError)
            ElMessage.warning('管理员创建成功，但角色分配失败')
          }
        }
      }
      
      console.log('管理员操作响应:', response)
      
      if (response.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadTableData()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('管理员操作失败:', error)
      ElMessage.error('操作失败，请检查网络连接')
    } finally {
      submitLoading.value = false
    }
  }
}

const handlePasswordSubmit = async () => {
  const valid = await passwordFormRef.value?.validate()
  if (valid && currentUserId.value) {
    passwordLoading.value = true
    try {
      console.log('重置密码:', currentUserId.value, passwordForm.newPassword)
      const response = await adminApi.resetAdminPassword(currentUserId.value, passwordForm.newPassword)
      console.log('密码重置响应:', response)
      
      if (response.code === 200) {
        ElMessage.success('密码重置成功')
        passwordDialogVisible.value = false
        // 清空表单
        Object.assign(passwordForm, {
          newPassword: '',
          confirmPassword: ''
        })
      } else {
        ElMessage.error(response.message || '密码重置失败')
      }
    } catch (error) {
      console.error('密码重置失败:', error)
      ElMessage.error('密码重置失败，请检查网络连接')
    } finally {
      passwordLoading.value = false
    }
  }
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

// 头像上传相关
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png' && rawFile.type !== 'image/gif' && rawFile.type !== 'image/webp') {
    ElMessage.error('头像图片只能是 JPG/PNG/GIF/WEBP 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('头像图片大小不能超过 10MB!')
    return false
  }
  return true
}

const uploadAvatar = async (options: UploadRequestOptions) => {
  try {
    console.log('开始上传头像:', options.file.name)
    const response = await fileApi.uploadAvatar(options.file)
    
    if (response.code === 200 && response.data) {
      // 使用返回的文件URL更新头像
      formData.avatar = response.data.fileUrl
      ElMessage.success('头像上传成功')
      
      console.log('头像上传成功:', response.data)
      options.onSuccess?.(response.data)
    } else {
      ElMessage.error(response.message || '头像上传失败')
      options.onError?.({ 
        status: 400, 
        method: 'POST', 
        url: '/file/avatar/upload',
        message: response.message || '上传失败'
      } as any)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
    options.onError?.({ 
      status: 500, 
      method: 'POST', 
      url: '/file/avatar/upload',
      message: '网络错误'
    } as any)
  }
}

// 处理头像显示URL
const getAvatarDisplayUrl = (avatar: string | null) => {
  if (!avatar) return ''
  
  // 如果是完整的URL（以http开头），直接返回
  if (avatar.startsWith('http')) {
    return avatar
  }
  
  // 如果是相对路径，添加API基础路径
  if (avatar.startsWith('/file/avatar/download/')) {
    return `/api${avatar}`
  }
  
  // 如果是base64数据，直接返回
  if (avatar.startsWith('data:image')) {
    return avatar
  }
  
  return avatar
}

// 下载头像
const downloadAvatar = (avatar: string) => {
  if (!avatar) {
    ElMessage.warning('暂无头像可下载')
    return
  }
  
  // 创建临时下载链接
  const link = document.createElement('a')
  link.href = getAvatarDisplayUrl(avatar)
  link.download = 'avatar.jpg'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 组件挂载时加载数据
onMounted(() => {
  loadRoleOptions()
  loadTableData()
})
</script>

<style lang="scss" scoped>
.admin-management {
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

// 头像相关样式
.avatar-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .download-btn {
    font-size: 12px;
    padding: 2px 4px;
  }
}

.avatar-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--primary-color);
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 80px;
    height: 80px;
    text-align: center;
    line-height: 80px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
    display: block;
    object-fit: cover;
  }
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .el-button {
    padding: 4px 8px;
    font-size: 12px;
  }
}

// 对话框样式调整
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style> 
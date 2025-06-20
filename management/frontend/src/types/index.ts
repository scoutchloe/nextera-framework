// 基础响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}

// 分页响应类型
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 基础实体类型
export interface BaseEntity {
  id?: number
  createTime?: string
  updateTime?: string
  createBy?: number
  updateBy?: number
}

// 管理员类型
export interface Admin extends BaseEntity {
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  role: number
  lastLoginTime?: string
  lastLoginIp?: string
  isDeleted?: number
}

// 系统用户类型
export interface SysUser extends BaseEntity {
  username: string
  password?: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  roleIds?: number[]
}

// 系统角色类型
export interface SysRole extends BaseEntity {
  roleCode: string
  roleName: string
  status: number
  description?: string
  permissionIds?: number[]
  permissions?: SysPermission[]
}

// 系统权限类型
export interface SysPermission extends BaseEntity {
  permissionCode: string
  permissionName: string
  permissionType: 'menu' | 'button'
  parentId: number
  menuPath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  status: number
  description?: string
  children?: SysPermission[]
}

// 用户类型
export interface User extends BaseEntity {
  username: string
  password?: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  gender?: number
  birthday?: string
  bio?: string
  website?: string
  location?: string
  lastLoginTime?: string
  isDeleted?: number
}

// 用户档案类型
export interface UserProfile extends BaseEntity {
  userId: number
  realName?: string
  idCard?: string
  address?: string
  profession?: string
  company?: string
  education?: string
  interests?: string
}

// 文章类型
export interface Article extends BaseEntity {
  title: string
  content: string
  summary?: string
  categoryId?: number
  authorId: number
  authorName: string
  status: number
  viewCount?: number
  likeCount?: number
  commentCount?: number
  isTop?: number
  isRecommend?: number
  tags?: string
  coverImage?: string
  publishTime?: string
  isDeleted?: number
}

// 文章分类类型
export interface ArticleCategory extends BaseEntity {
  name: string
  description?: string
  parentId?: number
  sortOrder?: number
  status: number
  icon?: string
  color?: string
  articleCount?: number
  isDeleted?: number
  children?: ArticleCategory[]
}

// 操作日志类型
export interface OperationLog extends BaseEntity {
  userId?: number
  username?: string
  operation: string
  method?: string
  params?: string
  result?: string
  ip?: string
  location?: string
  userAgent?: string
  executionTime?: number
}

// 登录请求类型
export interface LoginRequest {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
  rememberMe?: boolean
}

// 登录响应类型
export interface LoginResponse {
  token: string
  refreshToken?: string
  user?: Admin | SysUser
  adminInfo?: {
    id: number
    username: string
    realName?: string
    email?: string
    phone?: string
    avatar?: string
    role: number
    status: number
  }
  permissions?: string[]
  roles?: string[]
  expiresIn?: number
}

// 菜单项类型
export interface MenuItem {
  id: number
  title: string
  path?: string
  component?: string
  icon?: string
  parentId?: number
  sortOrder?: number
  children?: MenuItem[]
  meta?: {
    title: string
    icon?: string
    hidden?: boolean
    noCache?: boolean
    requireAuth?: boolean
    permissions?: string[]
  }
}

// 面包屑项类型
export interface BreadcrumbItem {
  title: string
  path?: string
  icon?: string
}

// 表格列配置类型
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  fixed?: 'left' | 'right'
  formatter?: (row: any, column: any, cellValue: any, index: number) => string
}

// 查询参数类型
export interface QueryParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  startTime?: string
  endTime?: string
  [key: string]: any
}

// 用户状态枚举
export enum UserStatus {
  NORMAL = 0,
  DISABLED = 1
}

// 文章状态枚举
export enum ArticleStatus {
  DRAFT = 0,
  PUBLISHED = 1,
  ARCHIVED = 2
}

// 权限类型枚举
export enum PermissionType {
  MENU = 'menu',
  BUTTON = 'button'
}

// 性别枚举
export enum Gender {
  UNKNOWN = 0,
  MALE = 1,
  FEMALE = 2
}

// 主题模式类型
export type ThemeMode = 'light' | 'dark' | 'auto'

// 侧边栏折叠状态类型
export type SidebarStatus = 'opened' | 'closed'

// 设备类型
export type DeviceType = 'desktop' | 'tablet' | 'mobile' 
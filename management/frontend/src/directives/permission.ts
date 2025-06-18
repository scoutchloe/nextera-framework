import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 用法: v-permission="'system:admin:create'" 或 v-permission="['system:admin:create', 'system:admin:update']"
 * 模式: v-permission:hide="'system:admin:create'" (隐藏元素)
 * 模式: v-permission:disable="'system:admin:create'" (禁用元素)
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  }
}

/**
 * 角色指令
 * 用法: v-role="'admin'" 或 v-role="['admin', 'super_admin']"
 * 模式: v-role:hide="'admin'" (隐藏元素)
 * 模式: v-role:disable="'admin'" (禁用元素)
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkRole(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkRole(el, binding)
  }
}

/**
 * 检查权限
 */
function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value, modifiers } = binding
  const userStore = useUserStore()

  if (!value) {
    console.warn('v-permission directive requires a permission value')
    return
  }

  // admin账户拥有所有权限，直接跳过检查
  if (userStore.isAdmin) {
    // 确保元素是可见和可用的
    if (modifiers.hide) {
      el.style.display = ''
    } else if (modifiers.disable) {
      el.removeAttribute('disabled')
      el.style.opacity = ''
      el.style.cursor = ''
      el.style.pointerEvents = ''
    }
    return
  }

  // 权限值可以是字符串或数组
  const permissions = Array.isArray(value) ? value : [value]
  const hasPermission = permissions.some(permission => userStore.hasPermission(permission))

  if (!hasPermission) {
    if (modifiers.hide) {
      // 隐藏模式：直接隐藏元素
      el.style.display = 'none'
    } else if (modifiers.disable) {
      // 禁用模式：禁用元素
      el.setAttribute('disabled', 'disabled')
      el.style.opacity = '0.5'
      el.style.cursor = 'not-allowed'
      el.style.pointerEvents = 'none'
    } else {
      // 默认模式：移除元素
      el.remove()
    }
  } else {
    // 有权限时，确保元素是可见和可用的
    if (modifiers.hide) {
      el.style.display = ''
    } else if (modifiers.disable) {
      el.removeAttribute('disabled')
      el.style.opacity = ''
      el.style.cursor = ''
      el.style.pointerEvents = ''
    }
  }
}

/**
 * 检查角色
 */
function checkRole(el: HTMLElement, binding: DirectiveBinding) {
  const { value, modifiers } = binding
  const userStore = useUserStore()

  if (!value) {
    console.warn('v-role directive requires a role value')
    return
  }

  // admin账户拥有所有角色权限，直接跳过检查
  if (userStore.isAdmin) {
    // 确保元素是可见和可用的
    if (modifiers.hide) {
      el.style.display = ''
    } else if (modifiers.disable) {
      el.removeAttribute('disabled')
      el.style.opacity = ''
      el.style.cursor = ''
      el.style.pointerEvents = ''
    }
    return
  }

  // 角色值可以是字符串或数组
  const roles = Array.isArray(value) ? value : [value]
  const hasRole = roles.some(role => userStore.hasRole(role))

  if (!hasRole) {
    if (modifiers.hide) {
      // 隐藏模式：直接隐藏元素
      el.style.display = 'none'
    } else if (modifiers.disable) {
      // 禁用模式：禁用元素
      el.setAttribute('disabled', 'disabled')
      el.style.opacity = '0.5'
      el.style.cursor = 'not-allowed'
      el.style.pointerEvents = 'none'
    } else {
      // 默认模式：移除元素
      el.remove()
    }
  } else {
    // 有角色时，确保元素是可见和可用的
    if (modifiers.hide) {
      el.style.display = ''
    } else if (modifiers.disable) {
      el.removeAttribute('disabled')
      el.style.opacity = ''
      el.style.cursor = ''
      el.style.pointerEvents = ''
    }
  }
}

/**
 * 权限检查函数（用于组合式API）
 */
export function usePermission() {
  const userStore = useUserStore()

  const hasPermission = (permissions: string | string[]) => {
    const permissionList = Array.isArray(permissions) ? permissions : [permissions]
    return permissionList.some(permission => userStore.hasPermission(permission))
  }

  const hasRole = (roles: string | string[]) => {
    const roleList = Array.isArray(roles) ? roles : [roles]
    return roleList.some(role => userStore.hasRole(role))
  }

  const hasAnyPermission = (permissions: string[]) => {
    return permissions.some(permission => userStore.hasPermission(permission))
  }

  const hasAllPermissions = (permissions: string[]) => {
    return permissions.every(permission => userStore.hasPermission(permission))
  }

  const hasAnyRole = (roles: string[]) => {
    return roles.some(role => userStore.hasRole(role))
  }

  const hasAllRoles = (roles: string[]) => {
    return roles.every(role => userStore.hasRole(role))
  }

  return {
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAllPermissions,
    hasAnyRole,
    hasAllRoles
  }
}

/**
 * 安装权限指令
 */
export function setupPermissionDirectives(app: any) {
  app.directive('permission', permission)
  app.directive('role', role)
}

/**
 * 导出所有指令
 */
export default {
  permission,
  role
} 
<template>
  <div class="login-container">
    <!-- 登录卡片 - PC端优化布局 -->
    <div class="login-card">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <div class="logo">
            <div class="logo-icon">
              <el-icon :size="48">
                <Avatar />
              </el-icon>
            </div>
            <h1 class="gradient-text">Nextera</h1>
          </div>
          <p class="welcome-title">欢迎登录管理平台</p>
          <p class="welcome-description">
            专业的企业级管理系统，为您提供安全、
            <p></p>
            高效的数据管理解决方案
          </p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>安全可靠的数据保护</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>灵活的权限管理体系</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>高效的业务流程管理</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单区域 -->
      <div class="form-section">
        <div class="form-header">
          <h2>用户登录</h2>
          <p>请输入您的账号信息</p>
        </div>
        
        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
              :disabled="loading"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
              :disabled="loading"
            />
          </el-form-item>
          
          <!-- 选项 -->
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe" :disabled="loading">
              记住我
            </el-checkbox>
            <el-link type="primary" @click="handleForgotPassword">
              忘记密码？
            </el-link>
          </div>
          
          <!-- 测试连接按钮 -->
          <!-- <el-button
            type="info"
            size="large"
            class="test-btn"
            @click="testConnection"
            style="margin-bottom: 15px;"
          >
            测试连接
          </el-button> -->
          
          <!-- 登录按钮 -->
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
          
          <!-- 简单测试按钮 -->
          <!-- <el-button
            type="success"
            size="large"
            @click="simpleTest"
            style="margin-top: 10px;"
          >
            简单测试
          </el-button> -->
        </el-form>
      </div>
      
      <!-- 主题切换 -->
      <div class="theme-toggle">
        <el-tooltip content="切换主题" placement="top">
          <el-button
            circle
            :icon="appStore.isDark ? 'Sunny' : 'Moon'"
            @click="appStore.toggleTheme"
          />
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Check, Avatar } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import type { LoginRequest } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive({
  username: 'admin',
  password: '123456',
  rememberMe: false
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 状态
const loading = ref(false)

// 简单测试方法
const simpleTest = () => {
  console.log('简单测试按钮被点击了!')
  ElMessage.info('简单测试成功 - 按钮事件正常')
}

// 登录处理
const handleLogin = async () => {
  console.log('=== 登录按钮被点击 ===')
  console.log('表单引用状态:', loginFormRef.value)
  console.log('表单数据:', loginForm)
  
  // 暂时跳过表单验证，直接进行登录
  loading.value = true
  
  try {
    console.log('开始调用用户Store登录方法，参数:', loginForm)

    // 使用用户Store的login方法
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    console.log('登录成功，用户Store状态已更新')
    
    ElNotification({
      title: '登录成功',
      message: '欢迎回来！正在跳转到主页...',
      type: 'success',
      position: 'top-right'
    })
    
    // 跳转到主页
    setTimeout(() => {
      console.log('准备跳转到主页')
      router.push('/')
    }, 1000)

  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 测试连接
const testConnection = async () => {
  try {
    console.log('开始测试连接...')
    
    // 直接使用fetch测试
    const response = await fetch('/api/test/ping', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('测试连接响应状态:', response.status)
    
    if (response.ok) {
      const data = await response.json()
      console.log('测试连接响应数据:', data)
      ElMessage.success('连接测试成功')
    } else {
      console.error('连接测试失败:', response.status, response.statusText)
      ElMessage.error(`连接测试失败: ${response.status}`)
    }
  } catch (error) {
    console.error('连接测试出错:', error)
    ElMessage.error('连接测试出错')
  }
}

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

onMounted(() => {
  // 如果已登录，直接跳转
  if (userStore.isLoggedIn) {
    router.push('/')
  }
})
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";

.login-container {
  position: relative;
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 30%, #f093fb 70%, #f5576c 100%);
  overflow: hidden;
  padding: 40px 20px;

  // 添加背景装饰
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
    background-size: 40px 40px;
    animation: float 20s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    top: 20%;
    right: 10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
    animation: pulse 4s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.login-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 
    0 32px 64px rgba(0, 0, 0, 0.12), 
    0 0 0 1px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: row;
  overflow: hidden;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 40px 80px rgba(0, 0, 0, 0.15), 
      0 0 0 1px rgba(255, 255, 255, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.4);
  }

  [data-theme='dark'] & {
    background: rgba(17, 24, 39, 0.95);
    border: 1px solid rgba(55, 65, 81, 0.3);
    box-shadow: 
      0 32px 64px rgba(0, 0, 0, 0.3), 
      0 0 0 1px rgba(55, 65, 81, 0.2),
      inset 0 1px 0 rgba(55, 65, 81, 0.3);
  }
}

// 左侧欢迎区域
.welcome-section {
  width: 50%;
  flex: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(0,0,0,0.05) 0%, rgba(0,0,0,0.2) 100%);
    z-index: 1;
  }

  .welcome-content {
    position: relative;
    z-index: 2;
    text-align: center;
    max-width: 480px;

    .logo {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
      margin-bottom: 40px;

      .logo-icon {
        padding: 20px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }
      }

      h1 {
        font-size: 2.8rem;
        font-weight: 700;
        margin: 0;
        letter-spacing: -1px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        background: linear-gradient(45deg, #ffffff, #f0f0f0);
        background-clip: text;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }

    .welcome-title {
      font-size: 1.5rem;
      font-weight: 600;
      margin: 0 0 15px 0;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .welcome-description {
      font-size: 1rem;
      line-height: 1.6;
      opacity: 0.9;
      margin: 0 0 30px 0;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    }

    .feature-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      text-align: left;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 1rem;
        opacity: 0.95;
        transition: transform 0.2s ease;

        &:hover {
          transform: translateX(5px);
        }

        .el-icon {
          color: #10b981;
          background: rgba(255, 255, 255, 0.15);
          border-radius: 50%;
          padding: 4px;
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}

// 右侧表单区域
.form-section {
  width: 50%;
  flex: none;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.98);

  [data-theme='dark'] & {
    background: rgba(17, 24, 39, 0.98);
  }

  .form-header {
    text-align: center;
    margin-bottom: 40px;

    h2 {
      font-size: 1.8rem;
      font-weight: 700;
      margin: 0 0 10px 0;
      color: #1f2937;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;

      [data-theme='dark'] & {
        color: #f9fafb;
        -webkit-text-fill-color: #f9fafb;
      }
    }

    p {
      font-size: 1rem;
      color: #6b7280;
      margin: 0;

      [data-theme='dark'] & {
        color: #9ca3af;
      }
    }
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 28px;
  }
  
  :deep(.el-input) {
    .el-input__wrapper {
      padding: 16px 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(0, 0, 0, 0.08);
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      min-height: 50px;
      background: rgba(255, 255, 255, 0.9);
      
      &.is-focus {
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
        border-color: #667eea;
        transform: translateY(-2px);
        background: rgba(255, 255, 255, 1);
      }

      &:hover {
        border-color: #667eea;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }
    }
    
    .el-input__inner {
      font-size: 16px;
      color: var(--text-primary);
      line-height: 1.5;
      
      &::placeholder {
        color: var(--text-secondary);
        opacity: 0.6;
      }
    }
    
    .el-input__prefix {
      font-size: 18px;
      color: var(--text-secondary);
    }
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  
  .el-checkbox {
    :deep(.el-checkbox__label) {
      font-size: 14px;
      color: var(--text-primary);
    }
  }
  
  .el-link {
    font-size: 14px;
  }
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
    background: linear-gradient(135deg, #5a67d8 0%, #667eea 100%);
  }

  &:active {
    transform: translateY(0);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.6s ease;
  }

  &:hover::before {
    left: 100%;
  }
}


.theme-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;

  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }
  }
}
</style> 
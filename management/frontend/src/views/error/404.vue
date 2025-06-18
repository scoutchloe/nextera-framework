<template>
  <div class="error-404">
    <div class="error-content">
      <h1>404</h1>
      <p>页面未找到</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.error-404 {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  
  .error-content {
    text-align: center;
    
    h1 {
      font-size: 4rem;
      color: var(--text-primary);
      margin: 0 0 16px 0;
    }
    
    p {
      font-size: 1.2rem;
      color: var(--text-secondary);
      margin: 0 0 24px 0;
    }
  }
}
</style> 
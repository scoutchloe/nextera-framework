<template>
  <div class="profile">
    <div class="page-header">
      <h1>个人资料</h1>
      <p>管理个人信息</p>
    </div>
    <div class="content">
      <el-card>
        <p>个人资料功能开发中...</p>
        <el-button type="primary" @click="handleClick">测试按钮</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const handleClick = () => {
  ElMessage.success('个人资料页面正常运行！')
}
</script>

<style lang="scss" scoped>
.profile {
  .page-header {
    margin-bottom: 20px;
    h1 {
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }
}
</style> 
<template>
  <div class="settings">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>管理系统配置</p>
    </div>
    <div class="content">
      <el-card>
        <p>系统设置功能开发中...</p>
        <el-button type="primary" @click="handleClick">测试按钮</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const handleClick = () => {
  ElMessage.success('系统设置页面正常运行！')
}
</script>

<style lang="scss" scoped>
.settings {
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
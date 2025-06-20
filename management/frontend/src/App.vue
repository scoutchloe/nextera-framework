<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

onMounted(() => {
  // 初始化应用
  appStore.initApp()
})
</script>

<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
/* 全局样式已在 main.ts 中导入 */
#app {
  width: 100%;
  height: 100vh;
  /* overflow: hidden; */
}
</style>

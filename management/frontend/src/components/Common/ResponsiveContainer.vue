<template>
  <div 
    class="responsive-container" 
    :class="containerClasses"
    :style="containerStyles"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

interface Props {
  maxWidth?: string
  padding?: string | number
  mobilePadding?: string | number
  tabletPadding?: string | number
  desktopPadding?: string | number
  centered?: boolean
  fluid?: boolean
  noGutters?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  maxWidth: 'none',
  padding: '24px',
  mobilePadding: '16px',
  tabletPadding: '20px',
  desktopPadding: '24px',
  centered: false,
  fluid: false,
  noGutters: false
})

const appStore = useAppStore()

const containerClasses = computed(() => {
  return {
    'responsive-container--centered': props.centered,
    'responsive-container--fluid': props.fluid,
    'responsive-container--no-gutters': props.noGutters,
    'responsive-container--mobile': appStore.isMobile,
    'responsive-container--tablet': appStore.isTablet,
    'responsive-container--desktop': appStore.isDesktop,
    'responsive-container--small-mobile': appStore.isSmallMobile,
    'responsive-container--large-desktop': appStore.isLargeDesktop,
    'responsive-container--xl-desktop': appStore.isXLDesktop,
    'responsive-container--landscape': appStore.isLandscape,
    'responsive-container--portrait': appStore.isPortrait
  }
})

const containerStyles = computed(() => {
  const styles: Record<string, any> = {}
  
  if (!props.fluid && props.maxWidth !== 'none') {
    styles.maxWidth = props.maxWidth
  }
  
  if (props.centered) {
    styles.marginLeft = 'auto'
    styles.marginRight = 'auto'
  }
  
  if (!props.noGutters) {
    if (appStore.isMobile) {
      styles.padding = typeof props.mobilePadding === 'number' 
        ? `${props.mobilePadding}px` 
        : props.mobilePadding
    } else if (appStore.isTablet) {
      styles.padding = typeof props.tabletPadding === 'number' 
        ? `${props.tabletPadding}px` 
        : props.tabletPadding
    } else {
      styles.padding = typeof props.desktopPadding === 'number' 
        ? `${props.desktopPadding}px` 
        : props.desktopPadding
    }
  }
  
  return styles
})
</script>

<style lang="scss" scoped>
.responsive-container {
  width: 100%;
  transition: all var(--transition-base);
  
  &--fluid {
    max-width: 100%;
  }
  
  &--centered {
    margin-left: auto;
    margin-right: auto;
  }
  
  &--no-gutters {
    padding: 0;
  }
  
  // 设备特定样式
  &--mobile {
    @media (max-width: 767px) {
      // 移动端特定样式
    }
  }
  
  &--tablet {
    @media (min-width: 768px) and (max-width: 1023px) {
      // 平板端特定样式
    }
  }
  
  &--desktop {
    @media (min-width: 1024px) {
      // 桌面端特定样式
    }
  }
  
  &--small-mobile {
    @media (max-width: 479px) {
      // 小屏幕移动端特定样式
    }
  }
  
  &--large-desktop {
    @media (min-width: 1440px) {
      // 大屏幕桌面端特定样式
    }
  }
  
  &--xl-desktop {
    @media (min-width: 1920px) {
      // 超大屏幕桌面端特定样式
    }
  }
  
  &--landscape {
    // 横屏特定样式
  }
  
  &--portrait {
    // 竖屏特定样式
  }
}
</style> 
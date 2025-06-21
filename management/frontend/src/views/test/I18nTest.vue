<template>
  <div class="i18n-test">
    <div class="page-header">
      <h1>{{ t('test.title') }}</h1>
      <p>{{ t('test.subtitle') }}</p>
    </div>

    <el-card>
      <h3>{{ t('test.currentLanguage') }}: {{ getCurrentLanguageName() }}</h3>
      
      <div class="language-controls">
        <el-button @click="toggleLanguage" type="primary">
          {{ t('test.toggleLanguage') }}: {{ getNextLanguageName() }}
        </el-button>
        
        <el-button @click="changeLanguage('zh-CN')" :disabled="locale === 'zh-CN'">
          中文
        </el-button>
        
        <el-button @click="changeLanguage('en-US')" :disabled="locale === 'en-US'">
          English
        </el-button>
      </div>

      <div class="test-content">
        <h4>{{ t('test.commonTexts') }}</h4>
        <ul>
          <li>{{ t('common.confirm') }}</li>
          <li>{{ t('common.cancel') }}</li>
          <li>{{ t('common.save') }}</li>
          <li>{{ t('common.delete') }}</li>
          <li>{{ t('common.edit') }}</li>
          <li>{{ t('common.add') }}</li>
        </ul>

        <h4>{{ t('test.headerTexts') }}</h4>
        <ul>
          <li>{{ t('header.profile') }}</li>
          <li>{{ t('header.settings') }}</li>
          <li>{{ t('header.logout') }}</li>
          <li>{{ t('header.language') }}</li>
        </ul>

        <h4>{{ t('test.menuTexts') }}</h4>
        <ul>
          <li>{{ t('menu.dashboard') }}</li>
          <li>{{ t('menu.system') }}</li>
          <li>{{ t('menu.admin') }}</li>
          <li>{{ t('menu.role') }}</li>
          <li>{{ t('menu.user') }}</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/composables/useI18n'

const { t, locale, changeLanguage, toggleLanguage, getCurrentLanguageName, getNextLanguageName } = useI18n()
</script>

<style lang="scss" scoped>
.i18n-test {
  padding: 20px;

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

  .language-controls {
    margin: 20px 0;
    display: flex;
    gap: 10px;
  }

  .test-content {
    margin-top: 20px;

    h4 {
      color: var(--text-primary);
      margin: 20px 0 10px 0;
    }

    ul {
      list-style-type: disc;
      margin-left: 20px;
      
      li {
        margin: 5px 0;
        color: var(--text-secondary);
      }
    }
  }
}
</style> 
<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">AI 面试 · 候选人端</div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/resumes/upload">上传简历</el-menu-item>
        <el-menu-item index="/resumes">简历列表</el-menu-item>
        <el-menu-item index="/interview">开始面试</el-menu-item>
        <el-menu-item index="/interview/history">面试历史</el-menu-item>
      </el-menu>
      <div class="header-right">
        <span class="user-name">{{ userName }}</span>
        <el-button link type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearToken, getCandidateName } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  if (route.path.startsWith('/interview/session') || route.path.startsWith('/interview/history/')) {
    return route.path.startsWith('/interview/history') ? '/interview/history' : '/interview'
  }
  if (route.path.startsWith('/resumes/') && route.path !== '/resumes/upload') {
    return '/resumes'
  }
  return route.path
})

const userName = computed(() => getCandidateName())

function logout() {
  clearToken()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100%;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7eb;
  height: 56px !important;
  padding: 0 20px;
}

.brand {
  font-weight: 700;
  color: #1b4332;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.user-name {
  color: #52796f;
  font-size: 14px;
}

.main {
  padding: 20px;
  max-width: 1080px;
  margin: 0 auto;
  width: 100%;
}
</style>

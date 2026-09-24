<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">AI 面试管理端</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-sub-menu index="candidateMng">
          <template #title>
            <el-icon><User /></el-icon>
            <span>候选人管理</span>
          </template>
          <el-menu-item index="/candidates">候选人列表</el-menu-item>
          <el-menu-item index="/candidates/create">创建候选人</el-menu-item>
          <el-menu-item index="/interview-records">面试记录</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="jobMng">
          <template #title>
            <el-icon><Suitcase /></el-icon>
            <span>职位管理</span>
          </template>
          <el-menu-item index="/jobs">职位列表</el-menu-item>
          <el-menu-item index="/jobs/create">创建职位</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="aiMng">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>AI面试官</span>
          </template>
          <el-menu-item index="/interviewers">面试官设置</el-menu-item>
          <el-menu-item index="/questions">面试题库</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <span class="admin-name">{{ adminName }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Setting, Suitcase, User } from '@element-plus/icons-vue'
import { clearToken, getAdminName } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string) || '管理后台')
const adminName = computed(() => getAdminName())

function logout() {
  clearToken()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #1f2d3d;
  color: #fff;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.aside :deep(.el-menu) {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-name {
  color: #606266;
  font-size: 14px;
}

.main {
  padding: 16px;
}
</style>

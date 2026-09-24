<template>
  <div class="page-card">
    <div class="toolbar">
      <h2>我的面试</h2>
      <el-button type="primary" @click="$router.push('/interview')">开始新面试</el-button>
    </div>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="jobName" label="岗位" min-width="140" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="evaluateStatus" label="评估" width="110" />
      <el-table-column prop="overallScore" label="总分" width="80" align="center" />
      <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/interview/history/${row.id}`)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMySessions } from '@/api'
import type { SessionSummary } from '@/types'

const loading = ref(false)
const list = ref<SessionSummary[]>([])

async function load() {
  loading.value = true
  try {
    list.value = await fetchMySessions()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>

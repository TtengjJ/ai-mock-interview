<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="candidateName" label="候选人" min-width="120" />
      <el-table-column prop="jobName" label="岗位" min-width="140" />
      <el-table-column prop="status" label="状态" min-width="110" />
      <el-table-column prop="evaluateStatus" label="评估" width="110" />
      <el-table-column prop="overallScore" label="总分" width="90" align="center" />
      <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/interview-records/${row.id}`)">
            详情
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-hint">
          <p>暂无面试记录</p>
          <p style="font-size: 13px">候选人完成文字面试后，记录将显示在此处</p>
        </div>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchInterviewRecords } from '@/api'
import type { InterviewRecordItem } from '@/types'

const loading = ref(false)
const list = ref<InterviewRecordItem[]>([])

async function loadData() {
  loading.value = true
  try {
    list.value = await fetchInterviewRecords()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.empty-hint {
  padding: 24px;
  color: #909399;
}
</style>

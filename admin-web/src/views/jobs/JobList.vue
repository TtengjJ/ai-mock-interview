<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="router.push('/jobs/create')">创建职位</el-button>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无职位">
      <el-table-column type="index" width="55" align="center" />
      <el-table-column prop="jobName" label="职位名称" min-width="140" />
      <el-table-column prop="jobDesc" label="职位描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val: string | number | boolean) => onToggle(row, Boolean(val))"
          />
        </template>
      </el-table-column>
      <el-table-column prop="interviewerName" label="面试官" min-width="120" />
      <el-table-column prop="prompt" label="提示词" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/jobs/${row.id}/edit`)">
            编辑
          </el-button>
          <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteJob, fetchJobs, toggleJobStatus } from '@/api'
import type { JobItem } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<JobItem[]>([])

async function loadData() {
  loading.value = true
  try {
    list.value = await fetchJobs()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function onToggle(row: JobItem, enabled: boolean) {
  const status = enabled ? 1 : 0
  try {
    await toggleJobStatus(row.id, status)
    row.status = status
    ElMessage.success(enabled ? '已启用' : '已停用')
  } catch {
    /* interceptor already showed message */
  }
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确认删除该职位？', '提示', { type: 'warning' })
  await deleteJob(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

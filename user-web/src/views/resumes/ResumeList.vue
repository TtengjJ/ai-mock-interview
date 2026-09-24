<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="router.push('/resumes/upload')">上传简历</el-button>
      <el-button :loading="loading" @click="loadData">刷新</el-button>
      <span class="poll-hint">分析中的简历每 3 秒自动刷新</span>
    </div>

    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无简历，请先上传">
      <el-table-column type="index" width="55" align="center" />
      <el-table-column prop="fileName" label="文件名" min-width="180" />
      <el-table-column label="分析状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.analyzeStatus)">{{ statusLabel(row.analyzeStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="overallScore" label="总分" width="90" align="center">
        <template #default="{ row }">
          {{ row.overallScore ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" min-width="160" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/resumes/${row.id}`)">详情</el-button>
          <el-button link type="danger" :loading="deletingId === row.id" @click="onDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteResume, fetchResumes } from '@/api'
import type { ResumeListItem } from '@/types'

const router = useRouter()
const loading = ref(false)
const deletingId = ref<number | null>(null)
const list = ref<ResumeListItem[]>([])
let timer: ReturnType<typeof setInterval> | null = null

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待分析',
    PROCESSING: '分析中',
    ANALYZING: '分析中',
    COMPLETED: '已完成',
    FAILED: '失败',
  }
  return map[status] || status || '-'
}

function statusType(status: string) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PROCESSING' || status === 'ANALYZING' || status === 'PENDING') return 'warning'
  return 'info'
}

function needsPoll() {
  return list.value.some((item) => {
    const s = item.analyzeStatus
    return s === 'PENDING' || s === 'PROCESSING' || s === 'ANALYZING'
  })
}

async function loadData(silent = false) {
  if (!silent) loading.value = true
  try {
    list.value = await fetchResumes()
  } catch {
    if (!silent) list.value = []
  } finally {
    if (!silent) loading.value = false
  }
}

async function onDelete(row: ResumeListItem) {
  try {
    await ElMessageBox.confirm(`确认删除简历「${row.fileName || row.id}」？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  deletingId.value = row.id
  try {
    await deleteResume(row.id)
    ElMessage.success('已删除')
    await loadData()
  } finally {
    deletingId.value = null
  }
}

onMounted(async () => {
  await loadData()
  timer = setInterval(() => {
    if (needsPoll()) {
      void loadData(true)
    }
  }, 3000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.poll-hint {
  color: #909399;
  font-size: 13px;
}
</style>

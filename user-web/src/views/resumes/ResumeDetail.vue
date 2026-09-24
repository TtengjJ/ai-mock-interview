<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <el-button @click="router.push('/resumes')">返回列表</el-button>
      <el-button @click="loadData">刷新</el-button>
      <el-button type="danger" plain :loading="deleting" @click="onDelete">删除</el-button>
    </div>

    <template v-if="detail">
      <div class="header-row">
        <h3>{{ detail.fileName || `简历 #${detail.id}` }}</h3>
        <el-tag :type="statusType(detail.analyzeStatus)">
          {{ statusLabel(detail.analyzeStatus) }}
        </el-tag>
      </div>

      <el-alert
        v-if="isAnalyzing(detail.analyzeStatus)"
        title="AI 正在分析中，请稍候…"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />

      <el-alert
        v-if="detail.analyzeStatus === 'FAILED'"
        :title="detail.analyzeError || '分析失败，可重新上传或稍后刷新'"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="总分">
          <span class="score">{{ analysis?.overallScore ?? '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ detail.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="综合评语" :span="2">
          {{ analysis?.summary || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <h4 class="section-title">优势</h4>
      <ul v-if="strengths.length" class="list">
        <li v-for="(item, idx) in strengths" :key="idx">{{ item }}</li>
      </ul>
      <p v-else class="muted">暂无</p>

      <h4 class="section-title">改进建议</h4>
      <template v-if="suggestions.length">
        <div v-for="(item, idx) in suggestions" :key="idx" class="suggestion">
          <template v-if="typeof item === 'string'">{{ item }}</template>
          <template v-else>
            <div class="sug-title">
              <el-tag size="small" v-if="item.priority">{{ item.priority }}</el-tag>
              <span>{{ item.category || '建议' }}</span>
            </div>
            <p v-if="item.issue">问题：{{ item.issue }}</p>
            <p v-if="item.recommendation">建议：{{ item.recommendation }}</p>
          </template>
        </div>
      </template>
      <p v-else class="muted">暂无</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteResume, fetchResumeDetail } from '@/api'
import type { ResumeDetail, SuggestionItem } from '@/types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const deleting = ref(false)
const detail = ref<ResumeDetail | null>(null)
let timer: ReturnType<typeof setInterval> | null = null

const resumeId = computed(() => Number(route.params.id))

const analysis = computed(() => detail.value?.analysis ?? null)

const strengths = computed(() => analysis.value?.strengths || [])

const suggestions = computed<(SuggestionItem | string)[]>(
  () => (analysis.value?.suggestions || []) as (SuggestionItem | string)[],
)

function isAnalyzing(status?: string) {
  return status === 'PENDING' || status === 'PROCESSING' || status === 'ANALYZING'
}

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
  if (isAnalyzing(status)) return 'warning'
  return 'info'
}

async function loadData(silent = false) {
  if (!Number.isFinite(resumeId.value)) {
    ElMessage.error('无效的简历 ID')
    router.replace('/resumes')
    return
  }
  if (!silent) loading.value = true
  try {
    detail.value = await fetchResumeDetail(resumeId.value)
  } catch {
    if (!silent) detail.value = null
  } finally {
    if (!silent) loading.value = false
  }
}

async function onDelete() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      `确认删除简历「${detail.value.fileName || detail.value.id}」？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  deleting.value = true
  try {
    await deleteResume(detail.value.id)
    ElMessage.success('已删除')
    router.replace('/resumes')
  } finally {
    deleting.value = false
  }
}

onMounted(async () => {
  await loadData()
  timer = setInterval(() => {
    if (isAnalyzing(detail.value?.analyzeStatus)) {
      void loadData(true)
    }
  }, 3000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.header-row h3 {
  margin: 0;
}

.score {
  font-size: 20px;
  font-weight: 700;
  color: #2d6a4f;
}

.section-title {
  margin: 20px 0 8px;
}

.list {
  margin: 0;
  padding-left: 20px;
}

.muted {
  color: #909399;
}

.suggestion {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.sug-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 4px;
}

.suggestion p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}
</style>

<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <el-button @click="$router.back()">返回</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <template v-if="detail">
      <h2>{{ detail.jobName }} · 面试详情</h2>
      <el-descriptions :column="2" border class="mb">
        <el-descriptions-item label="状态">{{ sessionStatusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="评估状态">{{ evaluateStatusLabel(detail.evaluateStatus) }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ detail.overallScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="detail.evaluateStatus === 'PROCESSING' || detail.status === 'EVALUATING'"
        type="warning"
        :closable="false"
        title="评估进行中，可稍后刷新"
        class="mb"
      />

      <template v-if="detail.overallFeedback">
        <h3>总评</h3>
        <p class="text">{{ detail.overallFeedback }}</p>
        <h3>优势</h3>
        <ul>
          <li v-for="(s, i) in detail.strengths || []" :key="'s' + i">{{ s }}</li>
        </ul>
        <h3>改进建议</h3>
        <ul>
          <li v-for="(s, i) in detail.improvements || []" :key="'i' + i">{{ s }}</li>
        </ul>
      </template>

      <h3>问答记录</h3>
      <el-collapse>
        <el-collapse-item
          v-for="q in detail.questions"
          :key="q.questionIndex"
          :title="`第 ${q.questionIndex + 1} 题（${q.source === 'BANK' ? '题库' : 'LLM'}）`"
        >
          <p><strong>问：</strong>{{ q.questionText }}</p>
          <p><strong>答：</strong>{{ q.answerText || '未作答' }}</p>
          <template v-if="q.followUpQuestion">
            <p><strong>追问：</strong>{{ q.followUpQuestion }}</p>
            <p><strong>追问答：</strong>{{ q.followUpAnswer || '未作答' }}</p>
          </template>
        </el-collapse-item>
      </el-collapse>

      <div v-if="detail.status === 'IN_PROGRESS'" class="mt">
        <el-button type="primary" @click="$router.push(`/interview/session/${detail.id}`)">
          继续面试
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchSessionDetail } from '@/api'
import type { SessionDetail } from '@/types'
import { formatDateTime } from '@/utils/datetime'
import { evaluateStatusLabel, sessionStatusLabel } from '@/utils/labels'

const route = useRoute()
const loading = ref(false)
const detail = ref<SessionDetail | null>(null)

async function load() {
  loading.value = true
  try {
    detail.value = await fetchSessionDetail(Number(route.params.id))
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
  gap: 8px;
  margin-bottom: 12px;
}
.mb {
  margin-bottom: 16px;
}
.mt {
  margin-top: 16px;
}
.text {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>

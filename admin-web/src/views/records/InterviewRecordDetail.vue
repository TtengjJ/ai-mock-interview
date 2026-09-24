<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <el-button @click="$router.push('/interview-records')">返回列表</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <template v-if="detail">
      <h2>面试记录 #{{ detail.id }}</h2>
      <el-descriptions :column="2" border class="mb">
        <el-descriptions-item label="候选人">{{ detail.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detail.jobName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        <el-descriptions-item label="评估">{{ detail.evaluateStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ detail.overallScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>

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

      <h3>问答详情</h3>
      <el-timeline>
        <el-timeline-item
          v-for="q in detail.questions || []"
          :key="q.questionIndex"
          :timestamp="`第 ${q.questionIndex + 1} 题 · ${q.source}`"
        >
          <p><strong>问：</strong>{{ q.questionText }}</p>
          <p><strong>答：</strong>{{ q.answerText || '未作答' }}</p>
          <template v-if="q.followUpQuestion">
            <p><strong>追问：</strong>{{ q.followUpQuestion }}</p>
            <p><strong>追问答：</strong>{{ q.followUpAnswer || '未作答' }}</p>
          </template>
        </el-timeline-item>
      </el-timeline>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchInterviewRecordDetail } from '@/api'
import type { InterviewRecordItem } from '@/types'

const route = useRoute()
const loading = ref(false)
const detail = ref<InterviewRecordItem | null>(null)

async function load() {
  loading.value = true
  try {
    detail.value = await fetchInterviewRecordDetail(Number(route.params.id))
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.mb {
  margin-bottom: 16px;
}
.text {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>

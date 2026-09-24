<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <h2>面试进行中</h2>
      <div class="meta" v-if="session">
        <el-tag>{{ session.jobName }}</el-tag>
        <span>第 {{ (session.currentIndex ?? 0) + 1 }} / {{ session.totalQuestions }} 题</span>
        <el-tag type="info">{{ current?.source === 'BANK' ? '题库' : 'LLM补题' }}</el-tag>
      </div>
    </div>

    <template v-if="session && current && session.status === 'IN_PROGRESS'">
      <el-card shadow="never" class="q-card">
        <h3>题目</h3>
        <p class="q-text">{{ current.questionText }}</p>

        <el-input
          v-model="answerText"
          type="textarea"
          :rows="6"
          :disabled="current.answered"
          placeholder="请输入你的回答"
        />
        <div class="actions">
          <el-button
            type="primary"
            :disabled="current.answered"
            :loading="submitting"
            @click="onSubmitAnswer"
          >
            提交作答
          </el-button>
          <el-button
            v-if="current.answered && !current.followUpGenerated"
            :loading="followLoading"
            @click="onFollowUp"
          >
            生成追问（1 次）
          </el-button>
        </div>
      </el-card>

      <el-card v-if="current.followUpGenerated" shadow="never" class="q-card">
        <h3>追问</h3>
        <p class="q-text">{{ current.followUpQuestion }}</p>
        <el-input
          v-model="followAnswer"
          type="textarea"
          :rows="4"
          :disabled="current.followUpAnswered"
          placeholder="请回答追问"
        />
        <div class="actions">
          <el-button
            type="primary"
            :disabled="current.followUpAnswered"
            :loading="followSubmitLoading"
            @click="onSubmitFollow"
          >
            提交追问作答
          </el-button>
        </div>
      </el-card>

      <div class="footer-actions" v-if="current.answered">
        <el-button
          v-if="!isLast"
          type="success"
          :disabled="needFollowAnswer"
          @click="onNext"
        >
          下一题
        </el-button>
        <el-button type="warning" :disabled="needFollowAnswer" :loading="completing" @click="onComplete">
          结束面试并评估
        </el-button>
      </div>
    </template>

    <template v-else-if="session">
      <el-result
        icon="success"
        title="面试已结束"
        :sub-title="statusText"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push(`/interview/history/${session.id}`)">
            查看详情
          </el-button>
          <el-button @click="$router.push('/interview/history')">返回历史</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  completeInterview,
  fetchSessionDetail,
  generateFollowUp,
  nextQuestion,
  submitAnswer,
  submitFollowUpAnswer,
} from '@/api'
import type { QuestionView, SessionDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const sessionId = computed(() => Number(route.params.id))

const loading = ref(false)
const submitting = ref(false)
const followLoading = ref(false)
const followSubmitLoading = ref(false)
const completing = ref(false)
const session = ref<SessionDetail | null>(null)
const answerText = ref('')
const followAnswer = ref('')
let pollTimer: number | undefined

const current = computed<QuestionView | undefined>(() => {
  if (!session.value) return undefined
  return session.value.questions?.find((q) => q.questionIndex === session.value!.currentIndex)
})

const isLast = computed(() => {
  if (!session.value) return true
  return session.value.currentIndex >= session.value.totalQuestions - 1
})

const needFollowAnswer = computed(() => {
  const c = current.value
  return !!(c?.followUpGenerated && !c.followUpAnswered)
})

const statusText = computed(() => {
  const s = session.value
  if (!s) return ''
  if (s.status === 'EVALUATED') return `评估完成，总分：${s.overallScore ?? '-'}`
  if (s.status === 'EVALUATING' || s.evaluateStatus === 'PROCESSING' || s.evaluateStatus === 'PENDING') {
    return '正在异步评估中，请稍候刷新…'
  }
  if (s.status === 'EVAL_FAILED') return `评估失败：${s.evaluateError || ''}`
  return `状态：${s.status}`
})

async function load() {
  loading.value = true
  try {
    session.value = await fetchSessionDetail(sessionId.value)
    syncInputs()
    maybePoll()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function syncInputs() {
  const c = current.value
  answerText.value = c?.answerText || ''
  followAnswer.value = c?.followUpAnswer || ''
}

function maybePoll() {
  stopPoll()
  const s = session.value
  if (!s) return
  if (['COMPLETED', 'EVALUATING'].includes(s.status)
    || ['PENDING', 'PROCESSING'].includes(s.evaluateStatus || '')) {
    pollTimer = window.setInterval(async () => {
      try {
        session.value = await fetchSessionDetail(sessionId.value)
        if (session.value.status === 'EVALUATED' || session.value.status === 'EVAL_FAILED') {
          stopPoll()
        }
      } catch {
        /* ignore */
      }
    }, 3000)
  }
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = undefined
  }
}

async function onSubmitAnswer() {
  if (!answerText.value.trim()) {
    ElMessage.warning('请填写作答')
    return
  }
  submitting.value = true
  try {
    session.value = await submitAnswer(sessionId.value, answerText.value.trim())
    syncInputs()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    submitting.value = false
  }
}

async function onFollowUp() {
  followLoading.value = true
  try {
    session.value = await generateFollowUp(sessionId.value)
    syncInputs()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '生成追问失败')
  } finally {
    followLoading.value = false
  }
}

async function onSubmitFollow() {
  if (!followAnswer.value.trim()) {
    ElMessage.warning('请填写追问作答')
    return
  }
  followSubmitLoading.value = true
  try {
    session.value = await submitFollowUpAnswer(sessionId.value, followAnswer.value.trim())
    syncInputs()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    followSubmitLoading.value = false
  }
}

async function onNext() {
  try {
    session.value = await nextQuestion(sessionId.value)
    syncInputs()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '切换失败')
  }
}

async function onComplete() {
  completing.value = true
  try {
    session.value = await completeInterview(sessionId.value)
    ElMessage.success('已提交评估')
    maybePoll()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '结束失败')
  } finally {
    completing.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
onUnmounted(stopPoll)
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.toolbar {
  margin-bottom: 16px;
}
.meta {
  display: flex;
  gap: 12px;
  align-items: center;
  color: #52796f;
  margin-top: 8px;
}
.q-card {
  margin-bottom: 16px;
}
.q-text {
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: 12px;
}
.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.footer-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
</style>

<template>
  <div class="page-card">
    <h2>开始文字面试</h2>
    <p class="hint">选择开启中的岗位；可选用已分析简历辅助出题。</p>

    <el-form label-width="100px" class="form">
      <el-form-item label="应聘岗位" required>
        <el-select v-model="jobId" placeholder="请选择岗位" filterable style="width: 100%">
          <el-option
            v-for="j in jobs"
            :key="j.id"
            :label="j.jobName"
            :value="j.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关联简历">
        <el-select v-model="resumeId" clearable placeholder="可选" style="width: 100%">
          <el-option
            v-for="r in resumes"
            :key="r.id"
            :label="`${r.fileName || '简历'} #${r.id}（${r.analyzeStatus}）`"
            :value="r.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="starting" @click="start">开始面试</el-button>
        <el-button @click="$router.push('/interview/history')">查看历史</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="selectedJobDesc"
      type="info"
      :closable="false"
      show-icon
      :title="selectedJobDesc"
      style="margin-top: 12px"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createInterviewSession, fetchInterviewJobs, fetchResumes } from '@/api'
import type { JobOption, ResumeListItem } from '@/types'

const router = useRouter()
const jobs = ref<JobOption[]>([])
const resumes = ref<ResumeListItem[]>([])
const jobId = ref<number | null>(null)
const resumeId = ref<number | null>(null)
const starting = ref(false)

const selectedJobDesc = computed(() => {
  const job = jobs.value.find((j) => j.id === jobId.value)
  return job?.jobDesc || ''
})

async function load() {
  try {
    ;[jobs.value, resumes.value] = await Promise.all([fetchInterviewJobs(), fetchResumes()])
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  }
}

async function start() {
  if (!jobId.value) {
    ElMessage.warning('请选择岗位')
    return
  }
  starting.value = true
  try {
    const session = await createInterviewSession(jobId.value, resumeId.value)
    ElMessage.success('面试已开始')
    router.push(`/interview/session/${session.id}`)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '创建面试失败')
  } finally {
    starting.value = false
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
.hint {
  color: #6c757d;
  margin: 0 0 20px;
}
.form {
  max-width: 520px;
}
</style>

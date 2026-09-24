<template>
  <div class="page-card">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" style="max-width: 680px">
      <el-form-item label="职位名称" prop="jobName">
        <el-input v-model="form.jobName" placeholder="职位名称" />
      </el-form-item>
      <el-form-item label="职位描述" prop="jobDesc">
        <el-input v-model="form.jobDesc" type="textarea" :rows="4" placeholder="职位描述" />
      </el-form-item>
      <el-form-item label="面试官" prop="interviewerConfigId">
        <el-select
          v-model="form.interviewerConfigId"
          clearable
          placeholder="选择 AI 面试官"
          style="width: 100%"
        >
          <el-option
            v-for="item in interviewers"
            :key="item.id"
            :label="item.aiName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="提示词" prop="prompt">
        <el-input v-model="form.prompt" type="textarea" :rows="4" placeholder="岗位相关提示词（可选）" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        <el-button @click="router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  createJob,
  fetchInterviewerConfigs,
  fetchJobs,
  updateJob,
} from '@/api'
import type { InterviewerConfigItem, JobForm } from '@/types'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const interviewers = ref<InterviewerConfigItem[]>([])

const isEdit = computed(() => !!route.params.id)
const jobId = computed(() => Number(route.params.id))

const form = reactive<JobForm>({
  jobName: '',
  jobDesc: '',
  status: 1,
  interviewerConfigId: null,
  prompt: '',
})

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
}

async function loadInterviewers() {
  try {
    interviewers.value = await fetchInterviewerConfigs()
  } catch {
    interviewers.value = []
  }
}

async function loadJob() {
  if (!isEdit.value) return
  const list = await fetchJobs()
  const item = list.find((j) => j.id === jobId.value)
  if (!item) {
    ElMessage.error('职位不存在')
    router.replace('/jobs')
    return
  }
  form.jobName = item.jobName
  form.jobDesc = item.jobDesc || ''
  form.status = item.status
  form.interviewerConfigId = item.interviewerConfigId ?? null
  form.prompt = item.prompt || ''
}

async function onSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateJob(jobId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createJob(form)
      ElMessage.success('创建成功')
    }
    router.push('/jobs')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadInterviewers()
  await loadJob()
})
</script>

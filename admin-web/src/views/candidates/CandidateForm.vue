<template>
  <div class="page-card">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 640px">
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="真实姓名" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="form.mobile" placeholder="用于候选人登录" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="邮箱（可选）" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="form.sex">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="0">女</el-radio>
          <el-radio :value="2">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
      </el-form-item>
      <el-form-item v-else label="密码">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="留空则不修改"
        />
      </el-form-item>
      <el-form-item label="面试岗位" prop="jobId">
        <el-select v-model="form.jobId" clearable placeholder="请选择岗位" style="width: 100%">
          <el-option v-for="job in jobs" :key="job.id" :label="job.jobName" :value="job.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注（可选）" />
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
  createCandidate,
  fetchCandidates,
  fetchJobs,
  updateCandidate,
} from '@/api'
import type { CandidateForm, JobItem } from '@/types'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const jobs = ref<JobItem[]>([])

const isEdit = computed(() => !!route.params.id)
const candidateId = computed(() => Number(route.params.id))

const form = reactive<CandidateForm>({
  realName: '',
  mobile: '',
  email: '',
  sex: 2,
  password: '',
  jobId: null,
  remark: '',
})

const rules = computed<FormRules>(() => ({
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: isEdit.value
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }],
}))

async function loadJobs() {
  try {
    jobs.value = await fetchJobs()
  } catch {
    jobs.value = []
  }
}

async function loadCandidate() {
  if (!isEdit.value) return
  const list = await fetchCandidates()
  const item = list.find((c) => c.id === candidateId.value)
  if (!item) {
    ElMessage.error('候选人不存在')
    router.replace('/candidates')
    return
  }
  form.realName = item.realName
  form.mobile = item.mobile
  form.email = item.email || ''
  form.sex = item.sex ?? 2
  form.jobId = item.jobId ?? null
  form.remark = item.remark || ''
  form.password = ''
}

async function onSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload: CandidateForm = {
      realName: form.realName,
      mobile: form.mobile,
      email: form.email || undefined,
      sex: form.sex,
      jobId: form.jobId,
      remark: form.remark || undefined,
    }
    if (form.password) {
      payload.password = form.password
    }
    if (isEdit.value) {
      await updateCandidate(candidateId.value, payload)
      ElMessage.success('更新成功')
    } else {
      payload.password = form.password
      await createCandidate(payload)
      ElMessage.success('创建成功')
    }
    router.push('/candidates')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadJobs()
  await loadCandidate()
})
</script>

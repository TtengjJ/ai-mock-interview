<template>
  <div class="page-card">
    <div class="toolbar">
      <el-select
        v-model="filterJobId"
        clearable
        placeholder="按岗位筛选"
        style="width: 220px"
        @change="loadData"
      >
        <el-option v-for="job in jobs" :key="job.id" :label="job.jobName" :value="job.id" />
      </el-select>
      <el-button type="primary" @click="openDialog()">新增题目</el-button>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无题目">
      <el-table-column type="index" width="55" align="center" />
      <el-table-column prop="question" label="面试题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="referenceAnswer" label="参考答案" min-width="220" show-overflow-tooltip />
      <el-table-column prop="jobName" label="岗位" min-width="120" />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="启用" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.isOn === 1"
            @change="(val: string | number | boolean) => onToggle(row, Boolean(val))"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="editingId ? '编辑题目' : '新增题目'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属岗位" prop="jobId">
          <el-select v-model="form.jobId" placeholder="请选择岗位" style="width: 100%">
            <el-option v-for="job in jobs" :key="job.id" :label="job.jobName" :value="job.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试题" prop="question">
          <el-input v-model="form.question" type="textarea" :rows="3" placeholder="题目内容" />
        </el-form-item>
        <el-form-item label="参考答案" prop="referenceAnswer">
          <el-input
            v-model="form.referenceAnswer"
            type="textarea"
            :rows="5"
            placeholder="参考答案（可选）"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="启用" prop="isOn">
          <el-switch v-model="form.isOn" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createQuestion,
  deleteQuestion,
  fetchJobs,
  fetchQuestions,
  toggleQuestion,
  updateQuestion,
} from '@/api'
import type { JobItem, QuestionForm, QuestionItem } from '@/types'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editingId = ref<number | null>(null)
const filterJobId = ref<number | null>(null)
const list = ref<QuestionItem[]>([])
const jobs = ref<JobItem[]>([])
const formRef = ref<FormInstance>()

const form = reactive<QuestionForm>({
  question: '',
  referenceAnswer: '',
  jobId: null,
  isOn: 1,
  sortOrder: 0,
})

const rules: FormRules = {
  jobId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  question: [{ required: true, message: '请输入面试题', trigger: 'blur' }],
}

async function loadJobs() {
  try {
    jobs.value = await fetchJobs()
  } catch {
    jobs.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    list.value = await fetchQuestions(filterJobId.value)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: QuestionItem) {
  editingId.value = row?.id ?? null
  form.question = row?.question || ''
  form.referenceAnswer = row?.referenceAnswer || ''
  form.jobId = row?.jobId ?? filterJobId.value
  form.isOn = row?.isOn ?? 1
  form.sortOrder = row?.sortOrder ?? 0
  visible.value = true
}

async function onSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateQuestion(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createQuestion(form)
      ElMessage.success('创建成功')
    }
    visible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function onToggle(row: QuestionItem, enabled: boolean) {
  const isOn = enabled ? 1 : 0
  try {
    await toggleQuestion(row.id, isOn)
    row.isOn = isOn
    ElMessage.success(enabled ? '已启用' : '已停用')
  } catch {
    /* interceptor */
  }
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确认删除该题目？', '提示', { type: 'warning' })
  await deleteQuestion(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(async () => {
  await loadJobs()
  await loadData()
})
</script>

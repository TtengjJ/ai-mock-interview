<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增面试官</el-button>
      <el-button @click="loadData">刷新</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无面试官配置">
      <el-table-column type="index" width="55" align="center" />
      <el-table-column prop="aiName" label="名称" min-width="140" />
      <el-table-column prop="systemPrompt" label="系统提示词" min-width="280" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="editingId ? '编辑面试官' : '新增面试官'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="aiName">
          <el-input v-model="form.aiName" placeholder="如：技术面试官" />
        </el-form-item>
        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="8"
            placeholder="评估/面试人设提示词"
          />
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
  createInterviewerConfig,
  deleteInterviewerConfig,
  fetchInterviewerConfigs,
  updateInterviewerConfig,
} from '@/api'
import type { InterviewerConfigForm, InterviewerConfigItem } from '@/types'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editingId = ref<number | null>(null)
const list = ref<InterviewerConfigItem[]>([])
const formRef = ref<FormInstance>()

const form = reactive<InterviewerConfigForm>({
  aiName: '',
  systemPrompt: '',
})

const rules: FormRules = {
  aiName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  systemPrompt: [{ required: true, message: '请输入系统提示词', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    list.value = await fetchInterviewerConfigs()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: InterviewerConfigItem) {
  editingId.value = row?.id ?? null
  form.aiName = row?.aiName || ''
  form.systemPrompt = row?.systemPrompt || ''
  visible.value = true
}

async function onSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateInterviewerConfig(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createInterviewerConfig(form)
      ElMessage.success('创建成功')
    }
    visible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确认删除该面试官配置？', '提示', { type: 'warning' })
  await deleteInterviewerConfig(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

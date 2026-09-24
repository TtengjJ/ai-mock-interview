<template>
  <div class="page-card">
    <h3 class="title">上传简历</h3>
    <p class="desc">支持 PDF / Word / 文本文件，上传后将异步进行 AI 评分分析。</p>

    <el-upload
      drag
      :auto-upload="false"
      :limit="1"
      :on-change="onFileChange"
      :on-remove="onRemove"
      :file-list="fileList"
      accept=".pdf,.doc,.docx,.txt"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
      <template #tip>
        <div class="el-upload__tip">单次上传一个文件</div>
      </template>
    </el-upload>

    <div class="actions">
      <el-button type="primary" :loading="uploading" :disabled="!selectedFile" @click="onUpload">
        开始上传
      </el-button>
      <el-button @click="router.push('/resumes')">查看列表</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { UploadFile, UploadUserFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadResume } from '@/api'

const router = useRouter()
const uploading = ref(false)
const selectedFile = ref<File | null>(null)
const fileList = ref<UploadUserFile[]>([])

function onFileChange(file: UploadFile) {
  selectedFile.value = (file.raw as File) || null
  fileList.value = file.raw ? [file] : []
}

function onRemove() {
  selectedFile.value = null
  fileList.value = []
}

async function onUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const res = await uploadResume(selectedFile.value)
    ElMessage.success(res.duplicate ? '文件已存在，已跳转详情' : '上传成功，正在分析中')
    router.push(`/resumes/${res.resumeId}`)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.title {
  margin: 0 0 8px;
}

.desc {
  margin: 0 0 20px;
  color: #6c757d;
  font-size: 14px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}
</style>

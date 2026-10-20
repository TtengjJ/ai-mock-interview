<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 180px" />
      <el-input v-model="query.mobile" placeholder="手机号" clearable style="width: 180px" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button @click="router.push('/candidates/create')">创建候选人</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无候选人">
      <el-table-column type="index" width="55" align="center" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column label="性别" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.sex === 1" type="success">男</el-tag>
          <el-tag v-else-if="row.sex === 0" type="danger">女</el-tag>
          <el-tag v-else type="info">保密</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="jobName" label="面试岗位" min-width="140">
        <template #default="{ row }">{{ row.jobName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="创建时间" min-width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/candidates/${row.id}/edit`)">
            编辑
          </el-button>
          <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteCandidate, fetchCandidates } from '@/api'
import type { CandidateItem } from '@/types'
import { formatDateTime } from '@/utils/datetime'

const router = useRouter()
const loading = ref(false)
const list = ref<CandidateItem[]>([])
const query = reactive({ realName: '', mobile: '' })

async function loadData() {
  loading.value = true
  try {
    list.value = await fetchCandidates({
      realName: query.realName || undefined,
      mobile: query.mobile || undefined,
    })
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确认删除该候选人？', '提示', { type: 'warning' })
  await deleteCandidate(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

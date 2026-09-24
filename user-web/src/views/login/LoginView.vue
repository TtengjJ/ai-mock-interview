<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2 class="title">候选人登录</h2>
      <p class="hint">使用管理员为你创建的手机号与密码登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent>
        <el-form-item prop="mobile">
          <el-input v-model="form.mobile" placeholder="手机号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { candidateLogin } from '@/api'
import { setCandidateName, setToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  mobile: '',
  password: '',
})

const rules: FormRules = {
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await candidateLogin(form.mobile, form.password)
    setToken(res.token)
    setCandidateName(res.realName || res.mobile || form.mobile)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/resumes'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #1b4332 0%, #40916c 45%, #f4f6f5 45%);
}

.login-card {
  width: 380px;
  padding: 8px;
}

.title {
  margin: 0 0 8px;
  text-align: center;
}

.hint {
  margin: 0 0 20px;
  text-align: center;
  color: #6c757d;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
}
</style>

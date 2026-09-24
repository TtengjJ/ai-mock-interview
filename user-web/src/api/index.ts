import request, { unwrap } from '@/utils/request'
import type {
  JobOption,
  LoginResponse,
  ResumeDetail,
  ResumeListItem,
  ResumeUploadResponse,
  Result,
  SessionDetail,
  SessionSummary,
} from '@/types'

export async function candidateLogin(mobile: string, password: string) {
  const { data } = await request.post<Result<LoginResponse>>('/api/auth/candidate/login', {
    mobile,
    password,
  })
  return unwrap(data)
}

export async function uploadResume(file: File) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await request.post<Result<ResumeUploadResponse>>('/api/resumes/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrap(data)
}

export async function fetchResumes() {
  const { data } = await request.get<Result<ResumeListItem[]>>('/api/resumes')
  const payload = unwrap(data)
  return Array.isArray(payload) ? payload : []
}

export async function fetchResumeDetail(id: number) {
  const { data } = await request.get<Result<ResumeDetail>>(`/api/resumes/${id}/detail`)
  return unwrap(data)
}

export async function deleteResume(id: number) {
  const { data } = await request.delete<Result<void>>(`/api/resumes/${id}`)
  return unwrap(data)
}

export async function fetchInterviewJobs() {
  const { data } = await request.get<Result<JobOption[]>>('/api/interview/jobs')
  const payload = unwrap(data)
  return Array.isArray(payload) ? payload : []
}

export async function createInterviewSession(jobId: number, resumeId?: number | null) {
  const { data } = await request.post<Result<SessionDetail>>('/api/interview/sessions', {
    jobId,
    resumeId: resumeId || undefined,
  })
  return unwrap(data)
}

export async function fetchMySessions() {
  const { data } = await request.get<Result<SessionSummary[]>>('/api/interview/sessions')
  const payload = unwrap(data)
  return Array.isArray(payload) ? payload : []
}

export async function fetchSessionDetail(id: number) {
  const { data } = await request.get<Result<SessionDetail>>(`/api/interview/sessions/${id}`)
  return unwrap(data)
}

export async function submitAnswer(id: number, answerText: string) {
  const { data } = await request.post<Result<SessionDetail>>(
    `/api/interview/sessions/${id}/answers`,
    { answerText },
  )
  return unwrap(data)
}

export async function generateFollowUp(id: number) {
  const { data } = await request.post<Result<SessionDetail>>(
    `/api/interview/sessions/${id}/follow-up`,
  )
  return unwrap(data)
}

export async function submitFollowUpAnswer(id: number, answerText: string) {
  const { data } = await request.post<Result<SessionDetail>>(
    `/api/interview/sessions/${id}/follow-up/answer`,
    { answerText },
  )
  return unwrap(data)
}

export async function nextQuestion(id: number) {
  const { data } = await request.post<Result<SessionDetail>>(
    `/api/interview/sessions/${id}/next`,
  )
  return unwrap(data)
}

export async function completeInterview(id: number) {
  const { data } = await request.post<Result<SessionDetail>>(
    `/api/interview/sessions/${id}/complete`,
  )
  return unwrap(data)
}

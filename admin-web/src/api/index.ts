import request, { unwrap } from '@/utils/request'
import type {
  CandidateForm,
  CandidateItem,
  InterviewerConfigForm,
  InterviewerConfigItem,
  InterviewRecordItem,
  JobForm,
  JobItem,
  LoginResponse,
  PageResult,
  QuestionForm,
  QuestionItem,
  Result,
} from '@/types'

function asList<T>(data: T[] | PageResult<T> | { items?: T[]; list?: T[] } | null | undefined): T[] {
  if (!data) return []
  if (Array.isArray(data)) return data
  if ('items' in data && Array.isArray(data.items)) return data.items
  if ('list' in data && Array.isArray(data.list)) return data.list
  return []
}

export async function adminLogin(username: string, password: string) {
  const { data } = await request.post<Result<LoginResponse>>('/api/auth/admin/login', {
    username,
    password,
  })
  return unwrap(data)
}

export async function fetchJobs() {
  const { data } = await request.get<Result<JobItem[] | PageResult<JobItem>>>('/api/admin/jobs')
  return asList(unwrap(data))
}

export async function createJob(body: JobForm) {
  const { data } = await request.post<Result<JobItem>>('/api/admin/jobs', body)
  return unwrap(data)
}

export async function updateJob(id: number, body: JobForm) {
  const { data } = await request.put<Result<JobItem>>(`/api/admin/jobs/${id}`, body)
  return unwrap(data)
}

export async function deleteJob(id: number) {
  await request.delete(`/api/admin/jobs/${id}`)
}

export async function toggleJobStatus(id: number, status: number) {
  const { data } = await request.patch<Result<JobItem>>(`/api/admin/jobs/${id}/status`, { status })
  return unwrap(data)
}

export async function fetchCandidates(params?: { realName?: string; mobile?: string }) {
  const { data } = await request.get<Result<CandidateItem[] | PageResult<CandidateItem>>>(
    '/api/admin/candidates',
    { params },
  )
  return asList(unwrap(data))
}

export async function createCandidate(body: CandidateForm) {
  const { data } = await request.post<Result<CandidateItem>>('/api/admin/candidates', body)
  return unwrap(data)
}

export async function updateCandidate(id: number, body: CandidateForm) {
  const { data } = await request.put<Result<CandidateItem>>(`/api/admin/candidates/${id}`, body)
  return unwrap(data)
}

export async function deleteCandidate(id: number) {
  await request.delete(`/api/admin/candidates/${id}`)
}

export async function fetchInterviewerConfigs() {
  const { data } = await request.get<
    Result<InterviewerConfigItem[] | PageResult<InterviewerConfigItem>>
  >('/api/admin/interviewer-configs')
  return asList(unwrap(data))
}

export async function createInterviewerConfig(body: InterviewerConfigForm) {
  const { data } = await request.post<Result<InterviewerConfigItem>>(
    '/api/admin/interviewer-configs',
    body,
  )
  return unwrap(data)
}

export async function updateInterviewerConfig(id: number, body: InterviewerConfigForm) {
  const { data } = await request.put<Result<InterviewerConfigItem>>(
    `/api/admin/interviewer-configs/${id}`,
    body,
  )
  return unwrap(data)
}

export async function deleteInterviewerConfig(id: number) {
  await request.delete(`/api/admin/interviewer-configs/${id}`)
}

export async function fetchQuestions(jobId?: number | null) {
  const { data } = await request.get<Result<QuestionItem[] | PageResult<QuestionItem>>>(
    '/api/admin/questions',
    { params: jobId ? { jobId } : undefined },
  )
  return asList(unwrap(data))
}

export async function createQuestion(body: QuestionForm) {
  const { data } = await request.post<Result<QuestionItem>>('/api/admin/questions', body)
  return unwrap(data)
}

export async function updateQuestion(id: number, body: QuestionForm) {
  const { data } = await request.put<Result<QuestionItem>>(`/api/admin/questions/${id}`, body)
  return unwrap(data)
}

export async function deleteQuestion(id: number) {
  await request.delete(`/api/admin/questions/${id}`)
}

export async function toggleQuestion(id: number, isOn: number) {
  const { data } = await request.patch<Result<QuestionItem>>(`/api/admin/questions/${id}/isOn`, {
    isOn,
  })
  return unwrap(data)
}

export async function fetchInterviewRecords() {
  const { data } = await request.get<
    Result<InterviewRecordItem[] | PageResult<InterviewRecordItem> | { items: InterviewRecordItem[] }>
  >('/api/admin/interview-records')
  return asList(unwrap(data))
}

export async function fetchInterviewRecordDetail(id: number) {
  const { data } = await request.get<Result<InterviewRecordItem>>(
    `/api/admin/interview-records/${id}`,
  )
  return unwrap(data)
}

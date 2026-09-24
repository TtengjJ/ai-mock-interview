/** 统一后端 Result<T> 响应 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page?: number
  pageSize?: number
}

export interface LoginResponse {
  token: string
  realName?: string
  username?: string
}

export interface JobItem {
  id: number
  jobName: string
  jobDesc?: string
  status: number
  interviewerConfigId?: number
  interviewerName?: string
  prompt?: string
  createdAt?: string
  updatedAt?: string
}

export interface JobForm {
  jobName: string
  jobDesc?: string
  status?: number
  interviewerConfigId?: number | null
  prompt?: string
}

export interface CandidateItem {
  id: number
  realName: string
  mobile: string
  email?: string
  sex?: number
  jobId?: number
  jobName?: string
  remark?: string
  createdAt?: string
}

export interface CandidateForm {
  realName: string
  mobile: string
  email?: string
  sex?: number
  password?: string
  jobId?: number | null
  remark?: string
}

export interface InterviewerConfigItem {
  id: number
  aiName: string
  systemPrompt: string
  createdAt?: string
  updatedAt?: string
}

export interface InterviewerConfigForm {
  aiName: string
  systemPrompt: string
}

export interface QuestionItem {
  id: number
  jobId: number
  jobName?: string
  question: string
  referenceAnswer?: string
  isOn: number
  sortOrder?: number
  createdAt?: string
}

export interface QuestionForm {
  question: string
  referenceAnswer?: string
  jobId: number | null
  isOn?: number
  sortOrder?: number
}

export interface InterviewRecordItem {
  id: number
  candidateName?: string
  jobName?: string
  status?: string
  evaluateStatus?: string
  overallScore?: number
  overallFeedback?: string
  strengths?: string[]
  improvements?: string[]
  totalQuestions?: number
  createdAt?: string
  completedAt?: string
  questions?: Array<{
    questionIndex: number
    questionText: string
    source: string
    answerText?: string
    followUpQuestion?: string
    followUpAnswer?: string
  }>
}

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface LoginResponse {
  token: string
  realName?: string
  mobile?: string
  candidateId?: number
}

export type AnalyzeStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | string

export interface ResumeListItem {
  id: number
  fileName?: string
  analyzeStatus: AnalyzeStatus
  overallScore?: number
  createdAt?: string
  updatedAt?: string
}

export interface ResumeUploadResponse {
  resumeId: number
  fileName?: string
  analyzeStatus: AnalyzeStatus
  duplicate: boolean
}

export interface SuggestionItem {
  category?: string
  priority?: string
  issue?: string
  recommendation?: string
}

export interface ResumeAnalysis {
  overallScore?: number
  summary?: string
  strengths?: string[]
  suggestions?: SuggestionItem[] | string[]
  analyzedAt?: string
}

export interface ResumeDetail {
  id: number
  fileName?: string
  analyzeStatus: AnalyzeStatus
  analyzeError?: string
  createdAt?: string
  analysis?: ResumeAnalysis | null
}

export interface JobOption {
  id: number
  jobName: string
  jobDesc?: string
}

export interface QuestionView {
  questionIndex: number
  questionText: string
  source: string
  answerText?: string
  followUpQuestion?: string
  followUpAnswer?: string
  answered: boolean
  followUpGenerated: boolean
  followUpAnswered: boolean
}

export interface SessionSummary {
  id: number
  jobId: number
  jobName?: string
  resumeId?: number
  status: string
  evaluateStatus?: string
  overallScore?: number
  totalQuestions: number
  currentIndex: number
  createdAt?: string
  completedAt?: string
}

export interface SessionDetail {
  id: number
  candidateId?: number
  candidateName?: string
  jobId: number
  jobName?: string
  resumeId?: number
  status: string
  evaluateStatus?: string
  evaluateError?: string
  overallScore?: number
  overallFeedback?: string
  strengths?: string[]
  improvements?: string[]
  totalQuestions: number
  currentIndex: number
  questions: QuestionView[]
  createdAt?: string
  completedAt?: string
}

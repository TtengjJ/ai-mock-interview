const SESSION_STATUS: Record<string, string> = {
  IN_PROGRESS: '进行中',
  COMPLETED: '已结束',
  EVALUATING: '评估中',
  EVALUATED: '已评估',
  EVAL_FAILED: '评估失败',
}

const EVALUATE_STATUS: Record<string, string> = {
  PENDING: '待评估',
  PROCESSING: '评估中',
  COMPLETED: '已完成',
  FAILED: '失败',
}

export function sessionStatusLabel(status?: string | null): string {
  if (!status) return '-'
  return SESSION_STATUS[status] ?? status
}

export function evaluateStatusLabel(status?: string | null): string {
  if (!status) return '-'
  return EVALUATE_STATUS[status] ?? status
}

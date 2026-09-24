package interview.guide.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举（无限流 8xxx）
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

  // ========== 通用错误 1xxx ==========
  BAD_REQUEST(400, "请求参数错误"),
  UNAUTHORIZED(401, "未授权"),
  FORBIDDEN(403, "禁止访问"),
  NOT_FOUND(404, "资源不存在"),
  METHOD_NOT_ALLOWED(405, "请求方法不支持"),
  INTERNAL_ERROR(500, "服务器内部错误"),

  // ========== 简历模块错误 2xxx ==========
  RESUME_NOT_FOUND(2001, "简历不存在"),
  RESUME_PARSE_FAILED(2002, "简历解析失败"),
  RESUME_UPLOAD_FAILED(2003, "简历上传失败"),
  RESUME_DUPLICATE(2004, "简历已存在"),
  RESUME_FILE_TYPE_NOT_SUPPORTED(2006, "不支持的文件类型"),
  RESUME_ANALYSIS_FAILED(2007, "简历分析失败"),
  RESUME_ANALYSIS_NOT_FOUND(2008, "简历分析结果不存在"),

  // ========== 面试模块错误 3xxx ==========
  INTERVIEW_SESSION_NOT_FOUND(3001, "面试会话不存在"),
  INTERVIEW_SESSION_EXPIRED(3002, "面试会话已过期"),
  INTERVIEW_QUESTION_NOT_FOUND(3003, "面试问题不存在"),
  INTERVIEW_ALREADY_COMPLETED(3004, "面试已完成"),
  INTERVIEW_EVALUATION_FAILED(3005, "面试评估失败"),
  INTERVIEW_QUESTION_GENERATION_FAILED(3006, "面试问题生成失败"),
  INTERVIEW_NOT_COMPLETED(3007, "面试尚未完成"),
  INTERVIEW_FOLLOW_UP_EXISTS(3008, "该题已生成过追问"),
  INTERVIEW_ANSWER_REQUIRED(3009, "请先提交本题作答"),
  INTERVIEW_INVALID_STATE(3010, "当前面试状态不允许此操作"),

  // ========== 存储模块错误 4xxx ==========
  STORAGE_UPLOAD_FAILED(4001, "文件上传失败"),
  STORAGE_DOWNLOAD_FAILED(4002, "文件下载失败"),
  STORAGE_DELETE_FAILED(4003, "文件删除失败"),

  // ========== AI服务错误 7xxx ==========
  AI_SERVICE_UNAVAILABLE(7001, "AI服务暂时不可用，请稍后重试"),
  AI_SERVICE_TIMEOUT(7002, "AI服务响应超时"),
  AI_SERVICE_ERROR(7003, "AI服务调用失败"),
  AI_API_KEY_INVALID(7004, "AI服务密钥无效"),
  AI_RATE_LIMIT_EXCEEDED(7005, "AI服务调用频率超限"),

  // ========== 管理域错误 9xxx ==========
  AUTH_FAILED(9001, "用户名或密码错误"),
  TOKEN_INVALID(9002, "登录已失效，请重新登录"),
  ADMIN_NOT_FOUND(9003, "管理员不存在"),
  CANDIDATE_NOT_FOUND(9004, "候选人不存在"),
  JOB_NOT_FOUND(9005, "岗位不存在"),
  QUESTION_NOT_FOUND(9006, "题目不存在"),
  INTERVIEWER_CONFIG_NOT_FOUND(9007, "评估人设不存在"),
  MOBILE_ALREADY_EXISTS(9008, "手机号已存在"),
  USERNAME_ALREADY_EXISTS(9009, "用户名已存在");

  private final Integer code;
  private final String message;
}

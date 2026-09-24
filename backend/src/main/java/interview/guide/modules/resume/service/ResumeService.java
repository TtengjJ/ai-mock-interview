package interview.guide.modules.resume.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.infrastructure.file.DocumentParseService;
import interview.guide.infrastructure.file.FileHashService;
import interview.guide.infrastructure.file.FileStorageService;
import interview.guide.infrastructure.file.FileValidationService;
import interview.guide.modules.resume.listener.AnalyzeStreamProducer;
import interview.guide.modules.resume.model.ResumeAnalysisEntity;
import interview.guide.modules.resume.model.ResumeDtos.ResumeAnalysisDTO;
import interview.guide.modules.resume.model.ResumeDtos.ResumeDetailDTO;
import interview.guide.modules.resume.model.ResumeDtos.ResumeListItemDTO;
import interview.guide.modules.resume.model.ResumeDtos.UploadResponse;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeAnalysisRepository;
import interview.guide.modules.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

  private final FileValidationService fileValidationService;
  private final FileHashService fileHashService;
  private final DocumentParseService documentParseService;
  private final FileStorageService fileStorageService;
  private final ResumeRepository resumeRepository;
  private final ResumeAnalysisRepository analysisRepository;
  private final ResumePersistenceService persistenceService;
  private final AnalyzeStreamProducer analyzeStreamProducer;
  private final ResumeGradingService gradingService;

  /**
   * 上传：校验 → 哈希去重 → 解析 → S3 → 落库 → Stream（S3 在事务外）
   */
  public UploadResponse upload(Long candidateId, MultipartFile file) {
    fileValidationService.validateResumeFile(file);
    String hash = fileHashService.sha256(file);

    Optional<ResumeEntity> existing =
        resumeRepository.findByCandidateIdAndContentHash(candidateId, hash);
    if (existing.isPresent()) {
      ResumeEntity resume = existing.get();
      // 上次分析失败时重新入队，避免重复上传被静默挡住
      if (resume.getAnalyzeStatus() == AsyncTaskStatus.FAILED) {
        String rawText = resume.getRawText();
        if (rawText == null || rawText.isBlank()) {
          rawText = documentParseService.parseContent(file);
          if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(ErrorCode.RESUME_PARSE_FAILED, "未能从文件中提取有效文本");
          }
          resume.setRawText(rawText);
        }
        resume.setAnalyzeStatus(AsyncTaskStatus.PENDING);
        resume.setAnalyzeError(null);
        ResumeEntity saved = persistenceService.save(resume);
        analyzeStreamProducer.sendAnalyzeTask(saved.getId(), rawText);
        log.info("Resume analyze retried: resumeId={}, candidateId={}", saved.getId(), candidateId);
        return new UploadResponse(saved.getId(), saved.getFileName(), saved.getAnalyzeStatus(), true);
      }
      return new UploadResponse(resume.getId(), resume.getFileName(), resume.getAnalyzeStatus(), true);
    }

    String rawText = documentParseService.parseContent(file);
    if (rawText == null || rawText.isBlank()) {
      throw new BusinessException(ErrorCode.RESUME_PARSE_FAILED, "未能从文件中提取有效文本");
    }

    String storageKey = fileStorageService.uploadResume(file);

    ResumeEntity entity = ResumeEntity.builder()
        .candidateId(candidateId)
        .fileName(file.getOriginalFilename())
        .contentHash(hash)
        .storageKey(storageKey)
        .rawText(rawText)
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .analyzeStatus(AsyncTaskStatus.PENDING)
        .build();
    ResumeEntity saved = persistenceService.save(entity);

    analyzeStreamProducer.sendAnalyzeTask(saved.getId(), rawText);
    log.info("Resume uploaded: resumeId={}, candidateId={}", saved.getId(), candidateId);
    return new UploadResponse(saved.getId(), saved.getFileName(), saved.getAnalyzeStatus(), false);
  }

  public List<ResumeListItemDTO> list(Long candidateId) {
    return resumeRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream()
        .map(r -> {
          Integer score = analysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(r.getId())
              .map(ResumeAnalysisEntity::getOverallScore)
              .orElse(null);
          return new ResumeListItemDTO(
              r.getId(), r.getFileName(), r.getAnalyzeStatus(), score, r.getCreatedAt()
          );
        })
        .toList();
  }

  public ResumeDetailDTO detail(Long candidateId, Long resumeId) {
    ResumeEntity resume = resumeRepository.findByIdAndCandidateId(resumeId, candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

    ResumeAnalysisDTO analysisDto = analysisRepository
        .findFirstByResumeIdOrderByAnalyzedAtDesc(resumeId)
        .map(a -> new ResumeAnalysisDTO(
            a.getOverallScore(),
            a.getSummary(),
            gradingService.fromJsonArray(a.getStrengths()),
            gradingService.fromJsonArray(a.getSuggestions()),
            a.getAnalyzedAt()
        ))
        .orElse(null);

    return new ResumeDetailDTO(
        resume.getId(),
        resume.getFileName(),
        resume.getAnalyzeStatus(),
        resume.getAnalyzeError(),
        resume.getCreatedAt(),
        analysisDto
    );
  }

  public void delete(Long candidateId, Long resumeId) {
    String storageKey = persistenceService.deleteOwned(candidateId, resumeId);
    try {
      fileStorageService.deleteResume(storageKey);
    } catch (Exception e) {
      log.warn("删除对象存储文件失败（已忽略）: key={}, error={}", storageKey, e.getMessage());
    }
  }
}

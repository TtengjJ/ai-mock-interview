package interview.guide.infrastructure.file;

import interview.guide.common.config.ResumeProperties;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileValidationService {

  private final ResumeProperties resumeProperties;
  private final DocumentParseService documentParseService;

  public void validateResumeFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传简历文件");
    }
    long maxBytes = resumeProperties.getMaxFileSizeMb() * 1024 * 1024;
    if (file.getSize() > maxBytes) {
      throw new BusinessException(ErrorCode.BAD_REQUEST,
          "文件大小不能超过 " + resumeProperties.getMaxFileSizeMb() + "MB");
    }

    String filename = file.getOriginalFilename();
    String extension = extractExtension(filename);
    if (extension == null || !resumeProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
      throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_NOT_SUPPORTED,
          "仅支持 pdf/doc/docx/txt 格式");
    }

    String contentType = documentParseService.detectContentType(file);
    boolean typeOk = contentType != null && resumeProperties.getAllowedTypes().stream()
        .anyMatch(allowed -> contentType.toLowerCase().startsWith(allowed.toLowerCase())
            || allowed.equalsIgnoreCase(contentType));
    // txt 有时被检测为 text/plain; charset=...
    if (!typeOk && "txt".equalsIgnoreCase(extension)
        && contentType != null && contentType.toLowerCase().startsWith("text/")) {
      typeOk = true;
    }
    if (!typeOk) {
      throw new BusinessException(ErrorCode.RESUME_FILE_TYPE_NOT_SUPPORTED,
          "不支持的文件类型: " + contentType);
    }
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return null;
    }
    return filename.substring(filename.lastIndexOf('.') + 1);
  }
}

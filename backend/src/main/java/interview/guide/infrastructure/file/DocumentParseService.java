package interview.guide.infrastructure.file;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseService {

  private final Tika tika = new Tika();
  private final TextCleaningService textCleaningService;

  public String parseContent(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      String raw = tika.parseToString(inputStream);
      return textCleaningService.clean(raw);
    } catch (IOException | TikaException e) {
      log.error("文档解析失败: filename={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
      throw new BusinessException(ErrorCode.RESUME_PARSE_FAILED, "文档解析失败: " + e.getMessage());
    }
  }

  public String detectContentType(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return tika.detect(inputStream, file.getOriginalFilename());
    } catch (IOException e) {
      log.warn("内容类型检测失败，回退到文件声明类型: {}", e.getMessage());
      return file.getContentType();
    }
  }
}

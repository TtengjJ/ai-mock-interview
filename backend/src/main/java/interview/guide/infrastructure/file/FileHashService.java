package interview.guide.infrastructure.file;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class FileHashService {

  public String sha256(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return sha256(inputStream);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.RESUME_UPLOAD_FAILED, "读取文件失败");
    }
  }

  public String sha256(InputStream inputStream) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new BusinessException(ErrorCode.RESUME_UPLOAD_FAILED, "计算文件哈希失败");
    }
  }
}

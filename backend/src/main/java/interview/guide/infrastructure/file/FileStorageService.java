package interview.guide.infrastructure.file;

import interview.guide.common.config.StorageConfigProperties;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

  private final S3Client s3Client;
  private final StorageConfigProperties storageConfig;

  public String uploadResume(MultipartFile file) {
    return uploadFile(file, "resumes");
  }

  public void deleteResume(String fileKey) {
    deleteFile(fileKey);
  }

  public byte[] downloadFile(String fileKey) {
    if (!fileExists(fileKey)) {
      throw new BusinessException(ErrorCode.STORAGE_DOWNLOAD_FAILED, "文件不存在: " + fileKey);
    }
    try {
      GetObjectRequest getRequest = GetObjectRequest.builder()
          .bucket(storageConfig.getBucket())
          .key(fileKey)
          .build();
      return s3Client.getObjectAsBytes(getRequest).asByteArray();
    } catch (S3Exception e) {
      log.error("下载文件失败: {} - {}", fileKey, e.getMessage(), e);
      throw new BusinessException(ErrorCode.STORAGE_DOWNLOAD_FAILED, "文件下载失败: " + e.getMessage());
    }
  }

  private String uploadFile(MultipartFile file, String prefix) {
    ensureBucket();
    String originalFilename = file.getOriginalFilename();
    String fileKey = generateFileKey(originalFilename, prefix);
    try {
      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(storageConfig.getBucket())
          .key(fileKey)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();
      s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      log.info("文件上传成功: {} -> {}", originalFilename, fileKey);
      return fileKey;
    } catch (IOException e) {
      log.error("读取上传文件失败: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.STORAGE_UPLOAD_FAILED, "文件读取失败");
    } catch (S3Exception e) {
      log.error("上传文件到对象存储失败: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.STORAGE_UPLOAD_FAILED, "文件存储失败: " + e.getMessage());
    }
  }

  public boolean fileExists(String fileKey) {
    try {
      HeadObjectRequest headRequest = HeadObjectRequest.builder()
          .bucket(storageConfig.getBucket())
          .key(fileKey)
          .build();
      s3Client.headObject(headRequest);
      return true;
    } catch (S3Exception e) {
      return false;
    }
  }

  public void deleteFile(String fileKey) {
    if (fileKey == null || fileKey.isBlank()) {
      return;
    }
    try {
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
          .bucket(storageConfig.getBucket())
          .key(fileKey)
          .build();
      s3Client.deleteObject(deleteRequest);
      log.info("文件删除成功: {}", fileKey);
    } catch (S3Exception e) {
      log.error("删除文件失败: {} - {}", fileKey, e.getMessage(), e);
      throw new BusinessException(ErrorCode.STORAGE_DELETE_FAILED, "文件删除失败: " + e.getMessage());
    }
  }

  public void ensureBucket() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(storageConfig.getBucket()).build());
    } catch (NoSuchBucketException e) {
      s3Client.createBucket(CreateBucketRequest.builder().bucket(storageConfig.getBucket()).build());
      log.info("已创建存储桶: {}", storageConfig.getBucket());
    } catch (S3Exception e) {
      if (e.statusCode() == 404) {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(storageConfig.getBucket()).build());
        log.info("已创建存储桶: {}", storageConfig.getBucket());
      } else {
        log.warn("检查存储桶失败（可能已存在）: {}", e.getMessage());
      }
    }
  }

  private String generateFileKey(String originalFilename, String prefix) {
    String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    String safeName = sanitizeFilename(originalFilename);
    return prefix + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "_" + safeName;
  }

  private String sanitizeFilename(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "file";
    }
    String name = originalFilename.replaceAll("[\\\\/:*?\"<>|]", "_");
    return name.length() > 100 ? name.substring(name.length() - 100) : name;
  }
}

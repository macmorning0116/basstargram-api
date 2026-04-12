package com.yechan.fishing.fishing_api.domain.community.storage;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ImageStorageService implements ImageStorageService {

  private static final int MAX_IMAGE_COUNT = 5;
  private static final DateTimeFormatter DATE_PATH_FORMAT =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");

  private final S3Client s3Client;
  private final String bucket;
  private final String baseUrl;

  public S3ImageStorageService(S3Client s3Client, String bucket, String region) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com";
  }

  @Override
  public List<StoredCommunityImage> storeCommunityImages(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of();
    }
    if (files.size() > MAX_IMAGE_COUNT) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_COUNT_EXCEEDED);
    }

    List<StoredCommunityImage> stored = new ArrayList<>();
    for (int index = 0; index < files.size(); index++) {
      MultipartFile file = files.get(index);
      validateImageFile(file);
      String key = "community/" + datePath() + "/" + uuid() + extension(file);
      upload(file, key);
      stored.add(
          new StoredCommunityImage(
              baseUrl + "/" + key, index, file.getContentType(), file.getSize(), null, null));
    }
    return stored;
  }

  @Override
  public String storeProfileImage(MultipartFile file) {
    validateImageFile(file);
    String key = "profiles/" + datePath() + "/" + uuid() + extension(file);
    upload(file, key);
    return baseUrl + "/" + key;
  }

  private void upload(MultipartFile file, String key) {
    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType())
              .build();
      s3Client.putObject(
          request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_UPLOAD_ERROR);
    }
  }

  private void validateImageFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }
  }

  private String datePath() {
    return LocalDate.now().format(DATE_PATH_FORMAT);
  }

  private String uuid() {
    return UUID.randomUUID().toString();
  }

  private String extension(MultipartFile file) {
    String original = file.getOriginalFilename();
    if (original != null && original.contains(".")) {
      return original.substring(original.lastIndexOf("."));
    }
    return ".jpg";
  }
}

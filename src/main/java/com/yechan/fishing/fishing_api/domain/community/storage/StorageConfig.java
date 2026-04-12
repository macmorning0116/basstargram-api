package com.yechan.fishing.fishing_api.domain.community.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(CommunityStorageProperties.class)
public class StorageConfig {

  @Value("${storage.type:local}")
  private String storageType;

  @Value("${storage.s3.bucket:fishing-media}")
  private String s3Bucket;

  @Value("${storage.s3.region:ap-northeast-2}")
  private String s3Region;

  @Bean
  public ImageStorageService imageStorageService(
      CommunityStorageProperties communityStorageProperties) {
    if ("s3".equalsIgnoreCase(storageType)) {
      S3Client s3Client = S3Client.builder().region(Region.of(s3Region)).build();
      return new S3ImageStorageService(s3Client, s3Bucket, s3Region);
    }
    return new LocalImageStorageService(communityStorageProperties);
  }
}

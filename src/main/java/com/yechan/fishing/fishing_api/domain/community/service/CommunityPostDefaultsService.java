package com.yechan.fishing.fishing_api.domain.community.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDefaultsResponse;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.domain.map.service.MapService;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CommunityPostDefaultsService {

  private static final List<String> CAPITAL_REGION = List.of("서울특별시", "인천광역시", "경기도");
  private static final List<String> GANGWON_REGION = List.of("강원도", "강원특별자치도");
  private static final List<String> CHUNGCHEONG_REGION =
      List.of("대전광역시", "세종특별자치시", "충청북도", "충청남도");
  private static final List<String> GYEONGSANG_REGION =
      List.of("부산광역시", "대구광역시", "울산광역시", "경상북도", "경상남도");
  private static final List<String> JEOLLA_REGION = List.of("광주광역시", "전라북도", "전북특별자치도", "전라남도");
  private static final List<String> JEJU_REGION = List.of("제주도", "제주특별자치도");

  private final MapService mapService;

  public CommunityPostDefaultsService(MapService mapService) {
    this.mapService = mapService;
  }

  public CommunityPostDefaultsResponse extractDefaults(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }
    validateImageFile(image);

    try (InputStream inputStream = image.getInputStream()) {
      Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

      LocalDateTime capturedAt = extractCapturedAt(metadata);
      Double latitude = null;
      Double longitude = null;
      LocationSource locationSource = null;
      String region = null;
      String placeName = null;

      GeoLocation geoLocation = extractGeoLocation(metadata);
      if (geoLocation != null && !geoLocation.isZero()) {
        latitude = geoLocation.getLatitude();
        longitude = geoLocation.getLongitude();
        locationSource = LocationSource.EXIF;

        try {
          ReverseGeocodeResponse address = mapService.getAddress(latitude, longitude);
          region = resolveRegion(address.sido());
          placeName = joinPlaceName(address);
        } catch (FishingException ignored) {
          // 역지오코딩 실패 시에도 EXIF 좌표 자체는 유지합니다.
        }
      }

      return new CommunityPostDefaultsResponse(
          capturedAt,
          capturedAt == null ? null : FishedAtSource.EXIF,
          latitude,
          longitude,
          locationSource,
          region,
          placeName);
    } catch (IOException | ImageProcessingException e) {
      throw new FishingException(ErrorCode.COMMUNITY_IMAGE_METADATA_ERROR);
    }
  }

  private void validateImageFile(MultipartFile image) {
    String contentType = image.getContentType();
    if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
      throw new FishingException(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE);
    }
  }

  private LocalDateTime extractCapturedAt(Metadata metadata) {
    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (directory == null) {
      return null;
    }

    Date date =
        firstNonNull(
            directory.getDateOriginal(),
            directory.getDateDigitized(),
            directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL),
            directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED));
    if (date == null) {
      return null;
    }

    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  private GeoLocation extractGeoLocation(Metadata metadata) {
    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
    return gpsDirectory == null ? null : gpsDirectory.getGeoLocation();
  }

  private String resolveRegion(String sido) {
    if (sido == null || sido.isBlank()) {
      return null;
    }
    if (CAPITAL_REGION.contains(sido)) {
      return "서울/경기권";
    }
    if (GANGWON_REGION.contains(sido)) {
      return "강원권";
    }
    if (CHUNGCHEONG_REGION.contains(sido)) {
      return "충청권";
    }
    if (GYEONGSANG_REGION.contains(sido)) {
      return "경상권";
    }
    if (JEOLLA_REGION.contains(sido)) {
      return "전라권";
    }
    if (JEJU_REGION.contains(sido)) {
      return "제주권";
    }
    return sido;
  }

  private String joinPlaceName(ReverseGeocodeResponse address) {
    if (address == null) {
      return null;
    }

    StringJoiner joiner = new StringJoiner(" ");
    addIfPresent(joiner, address.sido());
    addIfPresent(joiner, address.sigungu());
    addIfPresent(joiner, address.dong());
    String placeName = joiner.toString();
    return placeName.isBlank() ? null : placeName;
  }

  private void addIfPresent(StringJoiner joiner, String value) {
    if (value != null && !value.isBlank()) {
      joiner.add(value);
    }
  }

  @SafeVarargs
  private final <T> T firstNonNull(T... values) {
    for (T value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }
}

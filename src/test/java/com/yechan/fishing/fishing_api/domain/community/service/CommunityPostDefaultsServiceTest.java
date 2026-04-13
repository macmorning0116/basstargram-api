package com.yechan.fishing.fishing_api.domain.community.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;

import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDefaultsResponse;
import com.yechan.fishing.fishing_api.domain.map.service.MapService;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CommunityPostDefaultsServiceTest {

  @Mock private MapService mapService;

  @InjectMocks private CommunityPostDefaultsService communityPostDefaultsService;

  @Test
  void extractDefaults_whenImageHasNoExif_returnsEmptyDefaults() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile("image", "sample.png", "image/png", createPngBytes());

    CommunityPostDefaultsResponse response = communityPostDefaultsService.extractDefaults(image);

    assertNull(response.fishedAt());
    assertNull(response.fishedAtSource());
    assertNull(response.latitude());
    assertNull(response.longitude());
    assertNull(response.locationSource());
    assertNull(response.region());
    assertNull(response.placeName());
    then(mapService).shouldHaveNoInteractions();
  }

  @Test
  void extractDefaults_whenImageIsEmpty_throwsInvalidImageFile() {
    MockMultipartFile image =
        new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

    FishingException exception =
        assertThrows(
            FishingException.class, () -> communityPostDefaultsService.extractDefaults(image));

    assertEquals(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE, exception.getErrorCode());
  }

  @Test
  void extractDefaults_whenContentTypeIsNotImage_throwsInvalidImageFile() {
    MockMultipartFile image =
        new MockMultipartFile("image", "note.txt", "text/plain", "hello".getBytes());

    FishingException exception =
        assertThrows(
            FishingException.class, () -> communityPostDefaultsService.extractDefaults(image));

    assertEquals(ErrorCode.COMMUNITY_INVALID_IMAGE_FILE, exception.getErrorCode());
  }

  @Test
  void extractDefaults_whenMetadataCannotBeRead_throwsMetadataError() {
    MockMultipartFile image =
        new MockMultipartFile("image", "broken.jpg", "image/jpeg", "broken-image".getBytes());

    FishingException exception =
        assertThrows(
            FishingException.class, () -> communityPostDefaultsService.extractDefaults(image));

    assertEquals(ErrorCode.COMMUNITY_IMAGE_METADATA_ERROR, exception.getErrorCode());
  }

  private byte[] createPngBytes() throws Exception {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }
}

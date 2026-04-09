package com.yechan.fishing.fishing_api.domain.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDefaultsResponse;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityPostDefaultsService;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityController.class)
class CommunityControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CommunityService communityService;

  @MockBean private CommunityPostDefaultsService communityPostDefaultsService;

  @Test
  void getPostDefaults_returnsWrappedSuccessResponse() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile("image", "spot.jpg", "image/jpeg", "image".getBytes());
    CommunityPostDefaultsResponse response =
        new CommunityPostDefaultsResponse(
            LocalDateTime.of(2026, 4, 9, 6, 30),
            FishedAtSource.EXIF,
            37.5665,
            126.9780,
            LocationSource.EXIF,
            "서울/경기권",
            "서울특별시 중구 태평로1가");

    given(communityPostDefaultsService.extractDefaults(any())).willReturn(response);

    mockMvc
        .perform(multipart("/v1/community/posts/defaults").file(image))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fishedAt").value("2026-04-09T06:30:00"))
        .andExpect(jsonPath("$.data.fishedAtSource").value("EXIF"))
        .andExpect(jsonPath("$.data.latitude").value(37.5665))
        .andExpect(jsonPath("$.data.longitude").value(126.978))
        .andExpect(jsonPath("$.data.locationSource").value("EXIF"))
        .andExpect(jsonPath("$.data.region").value("서울/경기권"))
        .andExpect(jsonPath("$.data.placeName").value("서울특별시 중구 태평로1가"));
  }

  @Test
  void getPostDefaults_whenImageIsMissing_returnsBadRequest() throws Exception {
    mockMvc.perform(multipart("/v1/community/posts/defaults")).andExpect(status().isBadRequest());
  }
}

package com.yechan.fishing.fishing_api.domain.analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisPoint;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisUsageResponse;
import com.yechan.fishing.fishing_api.domain.analysis.service.AnalysisService;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.jwt.AccessTokenPayload;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUserArgumentResolver;
import com.yechan.fishing.fishing_api.domain.auth.security.JwtAuthenticationFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(AnalysisController.class)
@Import(AnalysisControllerTest.AuthTestConfig.class)
class AnalysisControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AnalysisService analysisService;

  @MockBean private UserRepository userRepository;

  @TestConfiguration
  static class AuthTestConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    AuthTestConfig(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
    public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
      resolvers.add(new CurrentUserArgumentResolver(userRepository));
    }
  }

  @Test
  void analyze_returnsWrappedSuccessResponse() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile("image", "spot.jpg", "image/jpeg", "image".getBytes());
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 1L);

    AnalysisResponse response =
        new AnalysisResponse(
            "요약",
            List.of(
                new AnalysisPoint(0.2, 0.3, 0.1, "첫 번째 이유"),
                new AnalysisPoint(0.6, 0.7, 0.12, "두 번째 이유")),
            "채비",
            "전략");

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(analysisService.analyze(eq(1L), any(), eq(37.5), eq(127.0))).willReturn(response);

    mockMvc
        .perform(
            multipart("/v1/analysis/photo")
                .file(image)
                .param("lat", "37.5")
                .param("lng", "127.0")
                .requestAttr(
                    JwtAuthenticationFilter.AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE,
                    new AccessTokenPayload(
                        1L,
                        user.getRole(),
                        user.getProvider(),
                        LocalDateTime.now().plusMinutes(30))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.summary").value("요약"))
        .andExpect(jsonPath("$.data.points.length()").value(2))
        .andExpect(jsonPath("$.data.tackle").value("채비"))
        .andExpect(jsonPath("$.data.strategy").value("전략"));

    verify(analysisService).analyze(eq(1L), any(), eq(37.5), eq(127.0));
  }

  @Test
  void analyze_withoutAccessToken_returnsLoginRequired() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile("image", "spot.jpg", "image/jpeg", "image".getBytes());

    mockMvc
        .perform(
            multipart("/v1/analysis/photo").file(image).param("lat", "37.5").param("lng", "127.0"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_REQUIRED"));
  }

  @Test
  void getUsage_withAuthenticatedUser_returnsDailyUsage() throws Exception {
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(analysisService.getDailyUsage(1L))
        .willReturn(new AnalysisUsageResponse(LocalDate.of(2026, 5, 27), 5, 2, 3));

    mockMvc
        .perform(
            get("/v1/analysis/usage")
                .requestAttr(
                    JwtAuthenticationFilter.AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE,
                    new AccessTokenPayload(
                        1L,
                        user.getRole(),
                        user.getProvider(),
                        LocalDateTime.now().plusMinutes(30))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.dailyLimit").value(5))
        .andExpect(jsonPath("$.data.usedCount").value(2))
        .andExpect(jsonPath("$.data.remainingCount").value(3));
  }

  @Test
  void getUsage_withoutAccessToken_returnsLoginRequired() throws Exception {
    mockMvc
        .perform(get("/v1/analysis/usage"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_REQUIRED"));
  }

  @Test
  void analyze_whenImageIsMissing_returnsBadRequest() throws Exception {
    mockMvc
        .perform(multipart("/v1/analysis/photo").param("lat", "37.5").param("lng", "127.0"))
        .andExpect(status().isBadRequest());
  }
}

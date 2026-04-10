package com.yechan.fishing.fishing_api.domain.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GoogleSocialUserInfoClient implements SocialUserInfoClient {

  private final WebClient webClient;

  public GoogleSocialUserInfoClient(@Qualifier("googleAuthWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public AuthProvider provider() {
    return AuthProvider.GOOGLE;
  }

  @Override
  public SocialUserInfo getUserInfo(String accessToken) {
    try {
      JsonNode response =
          webClient
              .get()
              .uri("/oauth2/v3/userinfo")
              .headers(headers -> headers.setBearerAuth(accessToken))
              .retrieve()
              .bodyToMono(JsonNode.class)
              .block();

      if (response == null || response.path("sub").isMissingNode()) {
        throw new FishingException(ErrorCode.AUTH_SOCIAL_USER_INFO_ERROR);
      }

      String providerUserId = response.path("sub").asText(null);
      String email = textOrNull(response.path("email"));
      String nickname = textOrNull(response.path("name"));
      String profileImageUrl = textOrNull(response.path("picture"));

      return new SocialUserInfo(providerUserId, email, nickname, profileImageUrl);
    } catch (FishingException e) {
      throw e;
    } catch (Exception e) {
      throw new FishingException(ErrorCode.AUTH_SOCIAL_USER_INFO_ERROR);
    }
  }

  private String textOrNull(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String value = node.asText();
    return value == null || value.isBlank() ? null : value;
  }
}

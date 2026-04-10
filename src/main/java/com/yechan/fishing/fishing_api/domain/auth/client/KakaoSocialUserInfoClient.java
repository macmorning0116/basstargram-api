package com.yechan.fishing.fishing_api.domain.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoSocialUserInfoClient implements SocialUserInfoClient {

  private final KakaoApiProperties props;
  private final WebClient webClient;

  public KakaoSocialUserInfoClient(
      KakaoApiProperties props, @Qualifier("kakaoAuthWebClient") WebClient webClient) {
    this.props = props;
    this.webClient = webClient;
  }

  @Override
  public AuthProvider provider() {
    return AuthProvider.KAKAO;
  }

  @Override
  public String buildAuthorizationUrl(String state) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(requireConfigured(props.getAuthorizeUrl()))
            .queryParam("response_type", "code")
            .queryParam("client_id", requireConfigured(props.getClientId()))
            .queryParam("redirect_uri", requireConfigured(props.getRedirectUri()))
            .queryParam("state", state);

    if (props.getScope() != null && !props.getScope().isBlank()) {
      builder.queryParam("scope", props.getScope());
    }

    return builder.build(true).toUriString();
  }

  @Override
  public String exchangeCode(String code) {
    try {
      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("grant_type", "authorization_code");
      body.add("client_id", requireConfigured(props.getClientId()));
      body.add("redirect_uri", requireConfigured(props.getRedirectUri()));
      body.add("code", code);

      if (props.getClientSecret() != null && !props.getClientSecret().isBlank()) {
        body.add("client_secret", props.getClientSecret());
      }

      JsonNode response =
          webClient
              .post()
              .uri(requireConfigured(props.getTokenUrl()))
              .headers(
                  headers ->
                      headers.set(
                          "Content-Type", "application/x-www-form-urlencoded;charset=utf-8"))
              .body(BodyInserters.fromFormData(body))
              .retrieve()
              .bodyToMono(JsonNode.class)
              .block();

      String accessToken = textOrNull(response == null ? null : response.path("access_token"));
      if (accessToken == null) {
        throw new FishingException(ErrorCode.AUTH_SOCIAL_TOKEN_EXCHANGE_ERROR);
      }
      return accessToken;
    } catch (FishingException e) {
      throw e;
    } catch (Exception e) {
      throw new FishingException(ErrorCode.AUTH_SOCIAL_TOKEN_EXCHANGE_ERROR);
    }
  }

  @Override
  public SocialUserInfo getUserInfo(String accessToken) {
    try {
      JsonNode response =
          webClient
              .get()
              .uri(requireConfigured(props.getUserInfoUrl()))
              .headers(headers -> headers.setBearerAuth(accessToken))
              .retrieve()
              .bodyToMono(JsonNode.class)
              .block();

      if (response == null || response.path("id").isMissingNode()) {
        throw new FishingException(ErrorCode.AUTH_SOCIAL_USER_INFO_ERROR);
      }

      JsonNode kakaoAccount = response.path("kakao_account");
      JsonNode profile = kakaoAccount.path("profile");

      String providerUserId = response.path("id").asText(null);
      String email = textOrNull(kakaoAccount.path("email"));
      String nickname = textOrNull(profile.path("nickname"));
      String profileImageUrl = textOrNull(profile.path("profile_image_url"));

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

  private String requireConfigured(String value) {
    if (value == null || value.isBlank()) {
      throw new FishingException(ErrorCode.AUTH_PROVIDER_CONFIGURATION_ERROR);
    }
    return value;
  }
}

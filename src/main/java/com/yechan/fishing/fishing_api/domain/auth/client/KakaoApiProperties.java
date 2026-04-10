package com.yechan.fishing.fishing_api.domain.auth.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.kakao")
public class KakaoApiProperties {

  private String authorizeUrl;
  private String tokenUrl;
  private String userInfoUrl;
  private String clientId;
  private String clientSecret;
  private String redirectUri;
  private String scope;

  public String getAuthorizeUrl() {
    return authorizeUrl;
  }

  public void setAuthorizeUrl(String authorizeUrl) {
    this.authorizeUrl = authorizeUrl;
  }

  public String getTokenUrl() {
    return tokenUrl;
  }

  public void setTokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
  }

  public String getUserInfoUrl() {
    return userInfoUrl;
  }

  public void setUserInfoUrl(String userInfoUrl) {
    this.userInfoUrl = userInfoUrl;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}

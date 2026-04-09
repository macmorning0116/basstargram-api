package com.yechan.fishing.fishing_api.global.logging;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WebClientLoggingFilter {

  private static final String MDC_CONTEXT_KEY = "mdc";

  public ExchangeFilterFunction externalApiLoggingFilter(String clientName) {
    return (request, next) ->
        Mono.deferContextual(
                contextView -> {
                  Map<String, String> mdcContext = contextView.getOrDefault(MDC_CONTEXT_KEY, null);

                  long start = System.currentTimeMillis();

                  return next.exchange(request)
                      .doOnSuccess(
                          response -> {
                            withMdc(
                                mdcContext,
                                () -> {
                                  long duration = System.currentTimeMillis() - start;
                                  log.info(
                                      "{} API call success method={} uri={} status={} duration={}ms",
                                      clientName,
                                      request.method(),
                                      request.url().getPath(),
                                      response.statusCode(),
                                      duration);
                                });
                          })
                      .doOnError(
                          e -> {
                            withMdc(
                                mdcContext,
                                () -> {
                                  long duration = System.currentTimeMillis() - start;
                                  log.error(
                                      "{} API call failed method={} uri={} duration={}ms",
                                      clientName,
                                      request.method(),
                                      request.url().getPath(),
                                      duration,
                                      e);
                                });
                          });
                })
            .contextWrite(
                ctx -> {
                  Map<String, String> servletMdc = MDC.getCopyOfContextMap();
                  if (servletMdc == null) return ctx;
                  return ctx.put(MDC_CONTEXT_KEY, servletMdc);
                });
  }

  private void withMdc(Map<String, String> context, Runnable runnable) {
    if (context == null) {
      runnable.run();
      return;
    }
    try {
      MDC.setContextMap(context);
      runnable.run();
    } finally {
      MDC.clear();
    }
  }
}

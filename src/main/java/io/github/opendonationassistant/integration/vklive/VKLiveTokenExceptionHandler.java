package io.github.opendonationassistant.integration.vklive;

import io.github.opendonationassistant.integration.vklive.VKLiveClient.VKLiveTokenException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Singleton
public class VKLiveTokenExceptionHandler
  implements ExceptionHandler<VKLiveTokenException, HttpResponse<?>> {

  @Override
  public HttpResponse<?> handle(
    @SuppressWarnings("rawtypes") HttpRequest request,
    VKLiveTokenException exception
  ) {
    return HttpResponse.status(HttpStatus.BAD_GATEWAY);
  }
}

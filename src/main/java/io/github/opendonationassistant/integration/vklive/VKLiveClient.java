package io.github.opendonationassistant.integration.vklive;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jspecify.annotations.Nullable;

@Singleton
public class VKLiveClient implements OauthClient {

  private final ODALogger log = new ODALogger(this);
  private final VKLiveClientAuthApi auth;
  private final VKLiveClientDataApi data;
  private final String redirect;
  private final String credentials;

  @Inject
  public VKLiveClient(
    VKLiveClientAuthApi auth,
    VKLiveClientDataApi data,
    @Value("${vklive-auth.redirect}") String redirect,
    @Value("${vklive-auth.client.id}") String clientId,
    @Value("${vklive-auth.client.secret}") String clientSecret
  ) {
    this.auth = auth;
    this.data = data;
    this.redirect = redirect;
    this.credentials =
      "Basic " +
      Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes());
  }

  public CompletableFuture<GetAccessRecordResponse> link(
    String authorizationCode
  ) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("code", authorizationCode);
    params.put("redirect_uri", redirect);
    return auth
      .getToken(credentials, params)
      .handle((response, throwable) ->
        validate("link", response, throwable)
      );
  }

  @Override
  public CompletableFuture<RefreshedTokens> obtainAccessToken(
    String refreshToken
  ) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    params.put("redirect_uri", redirect);
    return auth
      .getToken(credentials, params)
      .handle((response, throwable) -> {
        var tokens = validate("refresh", response, throwable);
        return new RefreshedTokens(
          tokens.accessToken(),
          tokens.refreshToken()
        );
      });
  }

  public CompletableFuture<UserWrapper> getUser(String accessToken) {
    return data
      .getUser("Bearer %s".formatted(accessToken))
      .thenApply(DataWrapper::data);
  }

  private GetAccessRecordResponse validate(
    String operation,
    @Nullable HttpResponse<VKLiveTokenResponse> response,
    @Nullable Throwable throwable
  ) {
    if (throwable != null) {
      throw failure(operation, unwrap(throwable));
    }
    if (response == null) {
      throw failure(operation, null, null, null);
    }
    var status = response.getStatus();
    var body = response.body();
    if (status.getCode() < 200 || status.getCode() >= 300) {
      throw failure(operation, status, body, null);
    }
    if (body == null) {
      throw failure(operation, status, null, null);
    }
    var accessToken = body.accessToken();
    var refreshToken = body.refreshToken();
    if (accessToken == null || refreshToken == null) {
      throw failure(operation, status, body, null);
    }
    return new GetAccessRecordResponse(accessToken, refreshToken);
  }

  private static Throwable unwrap(Throwable throwable) {
    var cause = throwable;
    while (cause instanceof CompletionException) {
      var nested = cause.getCause();
      if (nested == null) {
        break;
      }
      cause = nested;
    }
    return cause;
  }

  private VKLiveTokenException failure(String operation, Throwable cause) {
    if (cause instanceof HttpClientResponseException exception) {
      var body = exception.getResponse().body();
      return failure(
        operation,
        exception.getStatus(),
        body instanceof VKLiveTokenResponse tokenResponse
          ? tokenResponse
          : null,
        null
      );
    }
    return failure(operation, null, null, cause);
  }

  private VKLiveTokenException failure(
    String operation,
    @Nullable HttpStatus status,
    @Nullable VKLiveTokenResponse body,
    @Nullable Throwable cause
  ) {
    var httpStatus = status == null
      ? "no response"
      : "%d %s".formatted(status.getCode(), status.getReason());
    String error;
    if (body != null) {
      error = valueOrNone(body.error());
    } else if (cause != null) {
      error = valueOrNone(cause.getMessage());
    } else {
      error = "empty response";
    }
    var description = body == null
      ? "<none>"
      : valueOrNone(body.errorDescription());
    log.error(
      "Failed to obtain VK Live tokens",
      Map.of(
        "operation",
        operation,
        "httpStatus",
        httpStatus,
        "error",
        error,
        "errorDescription",
        description
      )
    );
    return new VKLiveTokenException(
      "Failed to obtain VK Live tokens (%s): httpStatus=%s, error=%s, errorDescription=%s"
        .formatted(operation, httpStatus, error, description)
    );
  }

  private static String valueOrNone(@Nullable String value) {
    return value == null ? "<none>" : value;
  }

  @Client(value = "vklive-auth", errorType = VKLiveTokenResponse.class)
  public interface VKLiveClientAuthApi {
    @Post(
      value = "/oauth/server/token",
      produces = MediaType.APPLICATION_FORM_URLENCODED,
      consumes = MediaType.APPLICATION_JSON
    )
    CompletableFuture<HttpResponse<VKLiveTokenResponse>> getToken(
      @Header("Authorization") String auth,
      @Body Map<String, String> params
    );
  }

  @Client("vklive-data")
  public interface VKLiveClientDataApi {
    @Get("/v1/current_user")
    CompletableFuture<DataWrapper<UserWrapper>> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record Channel(String url) {}

  @Serdeable
  public static record UserWrapper(VKLiveUser user, Channel channel) {}

  @Serdeable
  public static record VKLiveUser(
    String id,
    String nick,
    @JsonProperty("avatar_url") String avatarUrl
  ) {}

  @Serdeable
  public static record DataWrapper<T>(T data) {}

  @Serdeable
  public static record VKLiveTokenResponse(
    @Nullable @JsonProperty("access_token") String accessToken,
    @Nullable @JsonProperty("refresh_token") String refreshToken,
    @Nullable @JsonProperty("error") String error,
    @Nullable @JsonProperty("error_description") String errorDescription
  ) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}

  public static class VKLiveTokenException extends RuntimeException {

    public VKLiveTokenException(String message) {
      super(message);
    }
  }
}

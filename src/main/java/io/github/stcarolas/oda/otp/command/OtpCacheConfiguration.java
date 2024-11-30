package io.github.stcarolas.oda.otp.command;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import java.util.Map;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.marshall.UTF8StringMarshaller;

@Factory
public class OtpCacheConfiguration {

  private static final String CACHE_NAME = "otp";

  @Bean
  public Map<String, String> donatersCache(RemoteCacheManager cacheManager) {
    return cacheManager
      .getCache(CACHE_NAME)
      .withDataFormat(
        DataFormat
          .builder()
          .keyMarshaller(new UTF8StringMarshaller())
          .valueMarshaller(new UTF8StringMarshaller())
          .build()
      );
  }
}

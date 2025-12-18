package io.github.opendonationassistant.otp.command;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import java.util.HashMap;
import java.util.Map;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.marshall.UTF8StringMarshaller;

@Factory
public class OtpCacheConfiguration {

  private static final String CACHE_NAME = "otp";

  @Bean
  @Requires(env = "standalone")
  public Map<String, String> otpCache(RemoteCacheManager cacheManager) {
    return new HashMap<>();
    // return cacheManager
    //   .getCache(CACHE_NAME)
    //   .withDataFormat(
    //     DataFormat.builder()
    //       .keyMarshaller(new UTF8StringMarshaller())
    //       .valueMarshaller(new UTF8StringMarshaller())
    //       .build()
    //   );
  }

  @Bean
  @Requires(env = "allinone")
  public Map<String, String> otpCache() {
    return new HashMap<>();
  }
}

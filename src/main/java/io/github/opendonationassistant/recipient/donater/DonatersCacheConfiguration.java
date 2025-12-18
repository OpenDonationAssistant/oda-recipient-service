package io.github.opendonationassistant.recipient.donater;

import io.github.opendonationassistant.SerdeableEntryMarshaller;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import java.util.HashMap;
import java.util.Map;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.marshall.UTF8StringMarshaller;

@Factory
public class DonatersCacheConfiguration {

  private static final String DONATERS_CACHE_NAME = "donaters";

  @Bean
  @Requires(env = "standalone")
  public Map<String, DailyContribution> donatersCache(
    RemoteCacheManager cacheManager
  ) {
    return new HashMap<>();
    // return cacheManager
    //   .getCache(DONATERS_CACHE_NAME)
    //   .withDataFormat(
    //     DataFormat
    //       .builder()
    //       .keyMarshaller(new UTF8StringMarshaller())
    //       .valueMarshaller(
    //         new SerdeableEntryMarshaller(DailyContribution.class)
    //       )
    //       .build()
    //   );
  }

  @Bean
  @Requires(env = "allinone")
  public Map<String, DailyContribution> donatersCache(){
    return new HashMap<>();
  }
}

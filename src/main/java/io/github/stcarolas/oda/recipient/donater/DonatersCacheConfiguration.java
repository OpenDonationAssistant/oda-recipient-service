package io.github.stcarolas.oda.recipient.donater;

import io.github.stcarolas.oda.SerdeableEntryMarshaller;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import java.util.Map;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.marshall.UTF8StringMarshaller;

@Factory
public class DonatersCacheConfiguration {

  private static final String DONATERS_CACHE_NAME = "donaters";

  @Bean
  public Map<String, DailyContribution> donatersCache(
    RemoteCacheManager cacheManager
  ) {
    return cacheManager
      .getCache(DONATERS_CACHE_NAME)
      .withDataFormat(
        DataFormat
          .builder()
          .keyMarshaller(new UTF8StringMarshaller())
          .valueMarshaller(
            new SerdeableEntryMarshaller(DailyContribution.class)
          )
          .build()
      );
  }
}

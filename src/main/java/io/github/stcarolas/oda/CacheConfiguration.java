package io.github.stcarolas.oda;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

@Factory
public class CacheConfiguration {

  @Value("${infinispan.url}")
  public String url;

  @Bean
  public RemoteCacheManager cacheManager() {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder
      .addServer()
      .host(url)
      .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
      .security()
      .authentication()
      .username("admin")
      .password("password")
      .realm("default")
      .saslMechanism("SCRAM-SHA-512")
      .remoteCache("donaters")
      .configuration(
        """
        <distributed-cache name="donaters" owners="2" mode="SYNC" statistics="true">
        	<encoding media-type="application/x-protostream"/>
        </distributed-cache>
        """
      );
    builder.remoteCache("otp")
      .configuration(
        """
        <distributed-cache name="otp" owners="2" mode="SYNC" statistics="true">
        	<encoding media-type="application/x-protostream"/>
        </distributed-cache>
        """
      );
    RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
    return cacheManager;
  }
}

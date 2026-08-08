package io.github.opendonationassistant;

import io.github.opendonationassistant.rabbit.AMQPConfiguration;
import io.github.opendonationassistant.rabbit.Exchange;
import io.github.opendonationassistant.rabbit.Queue;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.listener.handlers.TokenRequestHandler;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import io.micronaut.rabbitmq.connect.ChannelPool;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

@OpenAPIDefinition(
  info = @Info(
    title = "oda-recipient-service",
    version = "0.13.0",
    license = @License(
      name = "AGPL-3.0",
      url = "https://www.gnu.org/licenses/agpl-3.0.en.html"
    )
  )
)
@Factory
public class Application {

  public static void main(String[] args) {
    Micronaut.build(args).banner(false).start();
  }

  @ContextConfigurer
  public static class Configurer implements ApplicationContextConfigurer {

    @Override
    public void configure(@NonNull ApplicationContextBuilder builder) {
      builder.defaultEnvironments("allinone");
    }
  }

  @Singleton
  public ChannelInitializer rabbitConfiguration() {
    var contributions = new Queue("recipient.events");
    return new AMQPConfiguration(
      List.of(
        // Exchange.Exchange(
        //   "history",
        //   Map.of("event.HistoryItemEvent", contributions)
        // ),
        Exchange.Exchange("history", Map.of("recipient", contributions)),
        Exchange.Exchange("commands", Map.of()),
        Exchange.Exchange("rpc", Map.of("token", TokenRequestHandler.QUEUE))
      )
    );
  }

  @Singleton
  public RemoteCacheManager remoteCacheManager(
    @Value("${infinispan.client.hotrod.server.host}") String host,
    @Value("${infinispan.client.hotrod.server.port}") int port,
    @Value(
      "${infinispan.client.hotrod.security.authentication.username}"
    ) String username,
    @Value(
      "${infinispan.client.hotrod.security.authentication.password}"
    ) String password
  ) {
    var conf = new ConfigurationBuilder()
      .addServer()
      .host(host)
      .port(port)
      .security()
      .authentication()
      .username(username)
      .password(password)
      .build();
    var manager = new RemoteCacheManager(conf);
    manager.start();
    return manager;
  }

  @Singleton
  @Named("commands")
  public RabbitClient commandsFacade(ChannelPool pool, ObjectMapper mapper) {
    return new RabbitClient(pool, mapper, "commands");
  }

  @Singleton
  @Named("events")
  public RabbitClient eventsFacade(ChannelPool pool, ObjectMapper mapper) {
    return new RabbitClient(pool, mapper, "recipient");
  }
}

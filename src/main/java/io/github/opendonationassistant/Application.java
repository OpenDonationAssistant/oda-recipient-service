package io.github.opendonationassistant;

import io.github.opendonationassistant.rabbit.RabbitConfiguration;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.inject.Singleton;

@OpenAPIDefinition(info = @Info(title = "oda-recipient-service"))
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
    return new RabbitConfiguration();
  }
}

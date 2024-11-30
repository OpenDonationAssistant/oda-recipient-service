package io.github.stcarolas.oda.recipient.donater;

import com.rabbitmq.client.Channel;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;

@Singleton
public class ContributionRabbitConfiguration extends ChannelInitializer {

  @Override
  public void initialize(Channel channel, String name) throws IOException {
    channel.queueDeclare("payments_for_contributions", true, false, false, new HashMap<>()); // (4)
    channel.queueBind("payments_for_contributions", "amq.topic", "payments");
  }
}

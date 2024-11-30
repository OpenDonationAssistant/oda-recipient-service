package io.github.stcarolas.oda;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class KeycloakConfiguration {

  private Logger log = LoggerFactory.getLogger(KeycloakConfiguration.class);

  @Bean
  public RealmResource realm(
    @Value("${keycloak.url}") String url,
    @Value("${keycloak.user}") String user,
    @Value("${keycloak.password}") String password,
    @Value("${keycloak.client}") String clientId,
    @Value("${keycloak.realm}") String realm
  ) {
    var instance = Keycloak.getInstance(
      url,
      "master",
      user,
      password,
      clientId
    );
    log.info("instance: {}", instance);
    var targetRealm = instance.realm(realm);
    log.info("realm: {}", targetRealm);
    return targetRealm;
  }

}

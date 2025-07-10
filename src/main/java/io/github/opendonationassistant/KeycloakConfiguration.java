package io.github.opendonationassistant;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

@Factory
public class KeycloakConfiguration {

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
    var targetRealm = instance.realm(realm);
    return targetRealm;
  }

}

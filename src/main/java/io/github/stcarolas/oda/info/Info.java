package io.github.stcarolas.oda.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Info {

  private final boolean logged;
  private final String id;

  @JsonCreator
  public Info(boolean logged, String id) {
    this.logged = logged;
    this.id = id;
  }

  public boolean isLogged() {
    return logged;
  }

  public String getId() {
    return id;
  }
}

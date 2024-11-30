package io.github.stcarolas.oda;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class Application {

  public static void main(String[] args) {
    ApplicationContext context = Micronaut.run(Application.class, args);
    Beans.context = context;
  }
}

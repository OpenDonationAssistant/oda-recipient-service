package io.github.stcarolas.oda;

import io.micronaut.context.ApplicationContext;

public class Beans {

  public static ApplicationContext context;

  public static <T> T get(Class<T> clazz) {
    return context.getBean(clazz);
  }
}

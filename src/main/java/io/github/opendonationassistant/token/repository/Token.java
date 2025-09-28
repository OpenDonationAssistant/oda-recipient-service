package io.github.opendonationassistant.token.repository;

public interface Token {
  public TokenData data();
  public void save();
  public void toggle();
}

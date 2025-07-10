package io.github.opendonationassistant.info;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Info(boolean logged, String id) {}

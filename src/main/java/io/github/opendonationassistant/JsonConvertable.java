package io.github.opendonationassistant;

import java.util.Map;

public interface JsonConvertable {
  Map<String, Object> asJsonMap();
}

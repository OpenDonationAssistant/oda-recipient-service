package io.github.opendonationassistant.recipient.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.opendonationassistant.rabbit.RabbitClient;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SettingsTest {

  SettingsDataRepository repository = mock(SettingsDataRepository.class);

  @Test
  public void testAddingFeature() {
    var eventsFacade = mock(RabbitClient.class);
    SettingsData data = new SettingsData(
      "id",
      "testuser",
      List.of(),
      List.of()
    );
    Settings settings = new Settings(data, repository, eventsFacade);
    settings.setFeatureStatus("feature", SettingsData.FeatureStatus.ENABLED);
    assertTrue(settings.data().features().size() == 1);
  }
}

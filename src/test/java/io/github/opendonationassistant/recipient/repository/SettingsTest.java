package io.github.opendonationassistant.recipient.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.recipient.repository.SettingsData.Feature;
import io.github.opendonationassistant.recipient.repository.SettingsData.FeatureStatus;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class SettingsTest {

  SettingsDataRepository repository = mock(SettingsDataRepository.class);
  RabbitClient eventsFacade = mock(RabbitClient.class);

  private Settings settingsWith(List<Feature> features) {
    return new Settings(
      new SettingsData("id", "testuser", features, List.of()),
      repository,
      eventsFacade
    );
  }

  @Test
  public void testAddingFeature() {
    Settings settings = settingsWith(List.of());
    settings.setFeatureStatus("feature", FeatureStatus.ENABLED);
    assertTrue(settings.data().features().size() == 1);
  }

  @Test
  public void testUpdatingExistingFeatureStatusDoesNotDuplicate() {
    Settings settings = settingsWith(
      List.of(new Feature("feature", FeatureStatus.ENABLED))
    );

    settings.setFeatureStatus("feature", FeatureStatus.DISABLED);

    assertEquals(1, settings.data().features().size());
    assertEquals(
      FeatureStatus.DISABLED,
      settings.data().features().get(0).status()
    );
  }

  @Test
  public void testAddingSecondFeatureKeepsBoth() {
    Settings settings = settingsWith(
      List.of(new Feature("first", FeatureStatus.ENABLED))
    );

    settings.setFeatureStatus("second", FeatureStatus.DISABLED);

    assertEquals(2, settings.data().features().size());
  }

  @Test
  public void testSaveInsertsWhenSettingsDoNotExist() {
    when(repository.existsById("id")).thenReturn(false);
    var settings = settingsWith(List.of());
    var data = settings.data();

    settings.save();

    verify(repository).save(data);
    verify(repository, never()).update(any(SettingsData.class));
  }

  @Test
  public void testSaveUpdatesWhenSettingsExist() {
    when(repository.existsById("id")).thenReturn(true);
    var settings = settingsWith(List.of());
    var data = settings.data();

    settings.save();

    verify(repository).update(data);
    verify(repository, never()).save(any(SettingsData.class));
  }

  @Test
  public void testSetFeatureStatusEmitsFeaturesUpdatedEvent()
    throws IOException {
    Settings settings = settingsWith(List.of());

    settings.setFeatureStatus("feature", FeatureStatus.ENABLED);

    var captor = ArgumentCaptor.forClass(FeaturesUpdatedEvent.class);
    verify(eventsFacade).sendEvent(captor.capture());
    assertEquals("testuser", captor.getValue().recipientId());
    assertEquals(1, captor.getValue().features().size());
    assertEquals(
      FeaturesUpdatedEvent.FeatureStatus.ENABLED,
      captor.getValue().features().get(0).status()
    );
  }

  @Test
  public void testSetFeatureStatusWrapsIoException() throws IOException {
    Settings settings = settingsWith(List.of());
    doThrow(new IOException("broker unavailable"))
      .when(eventsFacade)
      .sendEvent(any());

    assertThrows(RuntimeException.class, () ->
      settings.setFeatureStatus("feature", FeatureStatus.ENABLED)
    );
  }
}

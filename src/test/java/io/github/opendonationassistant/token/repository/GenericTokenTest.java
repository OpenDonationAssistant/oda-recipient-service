package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.events.TokenSettingsChanged;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class GenericTokenTest {

  private final TokenDataRepository repository = mock(
    TokenDataRepository.class
  );
  private final RabbitClient events = mock(RabbitClient.class);

  private final TokenData data = new TokenData(
    "id",
    "token",
    "refreshToken",
    "recipientId",
    "Twitch",
    true,
    false,
    Map.of("stored", "value"),
    List.of()
  );

  private GenericToken token;

  @BeforeEach
  public void setUp() {
    token = new GenericToken(data, repository, events);
  }

  @Test
  public void testConstructorMergesDefaultSettingsWithStoredSettings() {
    var withDefaults = new GenericToken(data, repository, events) {
      @Override
      protected Map<String, Object> defaultSettings() {
        return new HashMap<>(
          Map.of("default", true, "stored", "overridden")
        );
      }
    };

    assertEquals(true, withDefaults.data().settings().get("default"));
    assertEquals("value", withDefaults.data().settings().get("stored"));
  }

  @Test
  public void testSaveInsertsNewToken() {
    when(repository.existsById("id")).thenReturn(false);

    token.save();

    verify(repository).insert(any(TokenData.class));
    verify(repository, never()).update(any(TokenData.class));
  }

  @Test
  public void testSaveEmitsSingleTokenUpdatedEventOnInsert()
    throws IOException {
    when(repository.existsById("id")).thenReturn(false);

    token.save();

    var captor = ArgumentCaptor.forClass(TokenSettingsChanged.class);
    verify(events).sendEvent(captor.capture());
    assertEquals(
      TokenSettingsChanged.Event.TOKEN_UPDATED,
      captor.getValue().event()
    );
    assertEquals("id", captor.getValue().id());
    assertEquals("recipientId", captor.getValue().recipientId());
  }

  @Test
  public void testSaveUpdatesExistingTokenWithoutEmittingEvent()
    throws IOException {
    when(repository.existsById("id")).thenReturn(true);

    token.save();

    verify(repository).update(any(TokenData.class));
    verify(repository, never()).insert(any(TokenData.class));
    verify(events, never()).sendEvent(any());
  }

  @Test
  public void testUpdateTokenPersistsNewTokenAndEmitsTokenUpdated()
    throws IOException {
    when(repository.existsById("id")).thenReturn(true);

    token.update("new-token");

    assertEquals("new-token", token.data().token());
    verify(repository).update(any(TokenData.class));
    var captor = ArgumentCaptor.forClass(TokenSettingsChanged.class);
    verify(events).sendEvent(captor.capture());
    assertEquals(
      TokenSettingsChanged.Event.TOKEN_UPDATED,
      captor.getValue().event()
    );
  }

  @Test
  public void testUpdateSettingsMergesWithExistingAndEmitsSettingsChanged()
    throws IOException {
    when(repository.existsById("id")).thenReturn(true);

    token.update(Map.of("added", "new"));

    assertEquals("new", token.data().settings().get("added"));
    assertEquals("value", token.data().settings().get("stored"));
    var captor = ArgumentCaptor.forClass(TokenSettingsChanged.class);
    verify(events).sendEvent(captor.capture());
    assertEquals(
      TokenSettingsChanged.Event.SETTINGS_CHANGED,
      captor.getValue().event()
    );
  }

  @Test
  public void testToggleFlipsEnabledAndEmitsTokenToggled()
    throws IOException {
    when(repository.existsById("id")).thenReturn(true);

    token.toggle();

    assertFalse(token.data().enabled());
    var captor = ArgumentCaptor.forClass(TokenSettingsChanged.class);
    verify(events).sendEvent(captor.capture());
    assertEquals(
      TokenSettingsChanged.Event.TOKEN_TOGGLED,
      captor.getValue().event()
    );
  }

  @Test
  public void testDeleteSoftDeletesTokenAndEmitsTokenDeleted()
    throws IOException {
    when(repository.existsById("id")).thenReturn(true);

    token.delete().join();

    assertTrue(token.data().deleted());
    var captor = ArgumentCaptor.forClass(TokenSettingsChanged.class);
    verify(events).sendEvent(captor.capture());
    assertEquals(
      TokenSettingsChanged.Event.TOKEN_DELETED,
      captor.getValue().event()
    );
  }

  @Test
  public void testSaveWrapsIoExceptionFromEventPublisher() throws IOException {
    when(repository.existsById("id")).thenReturn(false);
    doThrow(new IOException("broker unavailable"))
      .when(events)
      .sendEvent(any());

    assertThrows(RuntimeException.class, () -> token.save());
  }
}

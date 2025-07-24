package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class TokenDataRepositoryTest {

  @Inject
  TokenDataRepository repository;

  @Test
  public void testPutAndFindByRecipientId(@Given TokenData data) {
    repository.save(data);
    var read = repository.findById(data.id());
    assertFalse(read.isEmpty());
    assertEquals(data, read.get());
  }
}

package com.coms4156.mealplanningservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Local file storage implementation. */
@Service
@Profile("local")
public final class LocalFileStorageService implements StorageService {

  /** The data directory path. */
  private final Path dataDirectory;

  /**
   * Allows local exercises to use a private directory instead of seed files.
   *
   * @param directory directory containing the shared JSON storage files
   */
  public LocalFileStorageService(
      @Value("${storage.local-directory:src/main/resources/data}")
          final String directory) {
    dataDirectory = Path.of(directory);
  }

  /** {@inheritDoc} */
  @Override
  public String readFile(final String fileName) throws IOException {
    return Files.readString(dataDirectory.resolve(fileName));
  }

  /** {@inheritDoc} */
  @Override
  public void writeFile(final String fileName, final String content)
      throws IOException {
    Path destination = dataDirectory.resolve(fileName).toAbsolutePath();
    Path temporary = Files.createTempFile(
        destination.getParent(), "storage-", ".tmp");
    try {
      Files.writeString(temporary, content);
      Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
  }
}

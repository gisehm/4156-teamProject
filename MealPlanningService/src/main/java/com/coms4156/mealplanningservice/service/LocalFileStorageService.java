package com.coms4156.mealplanningservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Local file storage implementation. */
@Service
@Profile("local")
public final class LocalFileStorageService implements StorageService {

  /** The data directory path. */
  private static final String DATA_DIR = "src/main/resources/data/";

  /** {@inheritDoc} */
  @Override
  public String readFile(final String fileName) throws IOException {
    return Files.readString(new File(DATA_DIR + fileName).toPath());
  }

  /** {@inheritDoc} */
  @Override
  public void writeFile(final String fileName, final String content)
      throws IOException {
    Files.writeString(new File(DATA_DIR + fileName).toPath(), content);
  }
}
